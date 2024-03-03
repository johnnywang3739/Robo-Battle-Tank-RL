import robocode.*;
import java.awt.Color;
import java.io.*;
import ReplayMemory.*;
public class MyAgent_NN extends AdvancedRobot {
    private static RobotState current_state = new RobotState(); // initialise state object to store all
    // the current state values
    private static RobotState previous_state = new RobotState(); // to store previous state values
    private enum opMode { onScan, onAction };
    private static opMode my_opMode = opMode.onScan; // a flag to determine a state-action cycle
    public enum Policy {onPolicy, offPolicy};
    private final Policy policy = Policy.offPolicy; // define policy
    private static double reward = 0.0;
    private static double total_reward = 0.0;
    private final boolean enable_immediate_rewards = true; // flag for immediate rewards
    private final double gamma = 0.75;     // discount factor
    private final double alpha = 0.3;    // learning rate
    private final double immediate_bonus = 0.8; // rewards
    private final double terminal_bonus = 1.2;
    private final double immediate_penalty = -0.3;
    private final double terminal_penalty = -0.45;

//    private final double decay_rate = 0.9988;
    private final double initial_epsilon = 0.9;
    private final double final_epsilon = 0.01;
    private double battlefield_Width = 800.0; // battleground size
    private double battlefield_height = 600.0;
    private static int total_round = 0;
    private static int round = 0;
    private static int winRound = 0;
    private static double win_percentage = 0.0;
    private static double enemyBearing;
    private static double epsilon;
    static double lr = 0.007;
    static double momentum = 0.85;
    static int num_of_hidden = 12; // obtained from pretrained values
    public static NeuralNet nn = new NeuralNet(5, num_of_hidden,1, lr,
            momentum, "bipolar" );
    private static final int MEMORY_SIZE = 50; // Total memory size
    private static final int MINI_BATCH_SIZE = 25; // size of each training batch

    static ReplayMemory<Experience> replayMemory = new ReplayMemory<Experience>(MEMORY_SIZE);

    boolean pretrained_weights = true;
    private static final String RMS_ERROR_FILE = "rms_error.txt"; // File to save RMS errors


    private final double start_decay_round = 0;
    private final double end_decay_round = 2000;
    @Override
    public void run() {
        if (getRoundNum() == 0) {
            if (pretrained_weights) {
                try {
                    File pretrainedFile = getDataFile("pretrained_model.txt");
                    nn.load(pretrainedFile.getAbsolutePath());
                    System.out.println("Loading pretrained weights.");
                } catch (IOException e) {
                    System.out.println("Loading failed: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                nn.initializeWeights();
            }
        }
        setBodyColor(Color.red);
        setBulletColor(Color.red);
        this.battlefield_Width = getBattleFieldWidth();
        this.battlefield_height= getBattleFieldHeight();
        // Main loop
        while (true) {
//            System.out.println(winRound);
            switch (my_opMode) {
                case onScan:
                    reward = 0.0;
                    turnRadarRight(180.0); // Scan for enemy
                    break;
                case onAction:
                    int current_action_index = (Math.random() <= epsilon)
                            ? nn.get_random_action()
                            : nn.calculate_best_action(current_state);
                    current_state.set_action(RobotState.Action.values()[current_action_index]);
//                    System.out.println(current_state.getMy_action());
                    perform_actions(current_state.get_my_action());
                    nn.train_online(alpha, gamma, reward, policy, previous_state, current_state,
                            replayMemory, MINI_BATCH_SIZE);
                    replayMemory.printMemory();
                    reward = 0.0;
                    my_opMode = opMode.onScan;
                    break;
            }
            execute(); // Ensure all pending actions are executed
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        // Copy current state to previous state
        enemyBearing = e.getBearing();
        previous_state = new RobotState(current_state);
        current_state.update_robotState(this.battlefield_Width, this.battlefield_height,
                getX(), getY(), getEnergy(),
                e.getEnergy(), e.getDistance());

        // Set operation mode to onAction
        my_opMode = opMode.onAction;
    }
    public void perform_actions(RobotState.Action action){
        switch (action) {
            case FIRE_CANNON: {
                turnGunRight(getHeading() - getGunHeading() + enemyBearing);
                fire(3.0);
                break;
            }
            case TURN_LEFT: {
                setTurnLeft(30);
                setAhead(100);
                execute();
                break;
            }
            case TURN_RIGHT: {
                setTurnRight(30);
                setAhead(100);
                execute();
                break;
            }
            case GO_FORWARD: {
                setAhead(150);
                execute();
                break;
            }
            case GO_BACK: {
                setBack(150);
                execute();
                break;
            }
        }
    }
    @Override
    public void onBulletMissed(BulletMissedEvent e){
        if(enable_immediate_rewards) {
            reward += immediate_penalty;
            total_reward += reward;
        }
    }
    @Override
    public void onHitByBullet(HitByBulletEvent e){
        if(enable_immediate_rewards) {
            reward += immediate_penalty;
            total_reward += reward;
        }
    }
    @Override
    public void onHitRobot(HitRobotEvent e) {
        if(enable_immediate_rewards) {
            reward += immediate_penalty;
            total_reward += reward;
        }
        setBack(200);
        setTurnRight(60);
        execute();
        this.execute();
    }
    @Override
    public void onHitWall(HitWallEvent e){
        if(enable_immediate_rewards) {
            reward += immediate_penalty;
            total_reward += reward;
        }
        go_to_centre(battlefield_Width, battlefield_height);
        this.execute();
    }
    @Override
    public void onBulletHit(BulletHitEvent e){
        if(enable_immediate_rewards) {
            reward += immediate_bonus;
            total_reward += reward;
        }
    }
    @Override
    public void onWin(WinEvent e){
        winRound++;
        total_round++;
        reward = terminal_bonus;
        total_reward += reward;
        nn.train_online(alpha, gamma, reward, policy, previous_state, current_state, replayMemory, MINI_BATCH_SIZE);
        replayMemory.printMemory();
//        myLUT.computeQ(alpha, gamma, reward, policy, previous_state, current_state);
        double mean_total_rewards = total_reward / total_round;
        reward = 0.0;
        update_epsilon();
        if((total_round % 100 == 0) && (total_round != 0)){
            win_percentage = (double) winRound / 100;
            System.out.println(String.format("%d, %.3f",++round, win_percentage));
            save_win_rate(total_round, winRound);
            save_mean_total_rewards(total_round, mean_total_rewards);
            saveRMSError();
//            myLUT.save(getDataFile("LUT"  + ".txt"));
            winRound = 0;
        }

    }

    @Override
    public void onDeath(DeathEvent e){
        total_round++;
        reward = terminal_penalty;
        total_reward += reward;
        nn.train_online(alpha, gamma, reward, policy, previous_state, current_state, replayMemory, MINI_BATCH_SIZE);
        replayMemory.printMemory();
        double meanTotalReward = total_reward / total_round;
        reward = 0.0;
        update_epsilon();
        if((total_round % 100 == 0) && (total_round != 0)){
            win_percentage = (double) winRound / 100;
            System.out.println(String.format("%d, %.3f",++round, win_percentage));
            save_win_rate(total_round, winRound);
            save_mean_total_rewards(total_round, meanTotalReward);
            saveRMSError();
            winRound = 0;
        }
    }
    private void update_epsilon() {
        if (total_round < start_decay_round) {
            epsilon = initial_epsilon;
        } else if (total_round >= start_decay_round && total_round <= end_decay_round) {
            double linearDecayRate = (initial_epsilon - final_epsilon) / (end_decay_round - start_decay_round);
            epsilon = initial_epsilon - linearDecayRate * (total_round - start_decay_round);
            epsilon = Math.max(epsilon, final_epsilon); // Ensuring epsilon does not go below final_epsilon
        } else if (total_round > end_decay_round) {
            epsilon = final_epsilon;
        }
    }

    public void go_to_centre(double field_width, double field_height) {
        double centerX = field_width / 2;
        double centerY = field_height / 2;
        double turn_angle = Math.toDegrees(Math.atan2(centerX - getX(), centerY - getY())) - getHeading();
        turn_angle += turn_angle > 180 ? -360 : (turn_angle < -180 ? 360 : 0);// Normalise the turn angle
        turnRight(turn_angle);
        ahead(200);
    }
    private void save_win_rate(int totalRounds, int winRounds) {
        double winRate = winRounds / 100.0;
        try {
            String filePath = getDataFile("win_rate.txt").getAbsolutePath();
            FileWriter fw = new FileWriter(filePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);
            out.println(String.format("%.2f", winRate));
            out.close();
        } catch (IOException e) {
            e.printStackTrace(out);
        }
    }
    private void save_mean_total_rewards(int totalRounds, double meanTotalReward) {
        try {
            String filePath = getDataFile("mean_total_reward.txt").getAbsolutePath();
            FileWriter fw = new FileWriter(filePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);
            out.println(String.format("%.2f", meanTotalReward));
            out.close();
        } catch (IOException e) {
            e.printStackTrace(out);
        }
    }
    private void saveRMSError() {
        double rmsError = nn.RMS_error / 100.0;
        try {
            File file = getDataFile(RMS_ERROR_FILE);
            FileWriter fw = new FileWriter(file, true); // 'true' to append to the file
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);
            out.println(String.format("%.4f", rmsError));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Reset for next calculation
        nn.RMS_error = 0.0;
    }
}