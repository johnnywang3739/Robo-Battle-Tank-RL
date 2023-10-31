import robocode.*;
import java.awt.*;
import java.awt.Color;
import robocode.util.Utils;

import java.io.*;


public class MyAgent extends AdvancedRobot {
    private RobotState current_state = new RobotState(); // Initialize with a new state
    private RobotState previous_state = new RobotState(); // Initialize with a new state
    public enum opMode { onScan, onAction };
    public static int current_action_index;
    public static double enemyBearing;
    private opMode my_opMode = opMode.onScan;

    private double battlefield_Width = 800.0;
    private double battlefield_height = 600.0;
    /* Bonus and Penalty */
    /* Bonus and Penalty */
    private final double immediateBonus = 0.5;
    private final double terminalBonus = 2.0;
    private final double immediatePenalty = -0.1;
    private final double terminalPenalty = -0.2;
    // Whether take immediate rewards
    public static boolean takeImmediate = true;
    // Whether take on-policy algorithm
    public static boolean onPolicy = true;
    // Discount factor
    private double gamma = 0.9;
    // Learning rate
    private double alpha = 0.1;
    // Random number for epsilon-greedy policy
    private double epsilon = 0.2;
    // Q

    // Reward
    private double reward = 0.0;


    // static numbers for winning rounds
    public static int totalRound = 0;
    public static int round = 0;
    public static int winRound = 0;
    //    public static double[] winPercentage = new double[351];
    public static double winPercentage = 0.0;




    public static LUT myLUT = new LUT(RobotState.HP.values().length,
            RobotState.HP.values().length,
            RobotState.Distance.values().length,
            RobotState.Distance.values().length,
            RobotState.Action.values().length);

    @Override
    public void run() {

        // Set the robot colors
        setBodyColor(Color.red);
        setGunColor(Color.darkGray);
        setRadarColor(Color.white);
        setBulletColor(Color.red);
        // Debug output to Robocode screen

        this.battlefield_Width = getBattleFieldWidth();
        this.battlefield_height= getBattleFieldHeight();
        // Main robot loop
        while (true) {
            System.out.println(winRound);
            switch (my_opMode) {
                case onScan:
//                    System.out.println("Scanning...");
                    reward = 0.0;
                    turnRadarRight(90); // Scan for other robots
//                    myLUT.printLUT();
                    break;
                case onAction:
//                    System.out.println("Acting...");
                    // Perform action based on the current state
                    // ...
                    current_action_index = (Math.random() <= epsilon)
                            ? this.myLUT.getRandomAction():
                            this.myLUT.getBestAction(current_state.get_my_HP_quantised().ordinal(),
                                    current_state.get_enemy_HP_quantised().ordinal(),
                                    current_state.get_quantised_distance_to_enemy().ordinal(),
                                    current_state.get_quantised_distance_to_wall().ordinal());

//                    curAction = Action.values()[curActionIndex];
                    current_state.setMy_action(RobotState.Action.values()[current_action_index]);
//                    System.out.println(current_state.getMy_action());

                    switch (current_state.getMy_action()) {
                        case FIRE: {
                            turnGunRight(getHeading() - getGunHeading() + enemyBearing);
                            fire(3);
                            break;
                        }

                        case TURN_LEFT: {
                            setTurnLeft(30);
                            execute();
                            break;
                        }

                        case TURN_RIGHT: {
                            setTurnRight(30);
                            execute();
                            break;
                        }

                        case GO_FORWARD: {
                            setAhead(100);
                            execute();
                            break;
                        }
                        case GO_BACKWARD: {
                            setBack(100);
                            execute();
                            break;
                        }
                    }
                    double calcualted_Q = 0.0;
                    calcualted_Q = this.myLUT.calculate_Q(alpha, gamma, reward, onPolicy, previous_state, current_state);
                    this.myLUT.setQValue(previous_state);
                    my_opMode = opMode.onScan;
//                    System.out.println(calcualted_Q);
                    break;
            }

            execute(); // Ensure all pending actions are executed before drawing

        }


    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        // Copy current state to previous state
        enemyBearing = e.getBearing();
        previous_state = new RobotState(current_state);
        // Update current state with the new scanned datac

        current_state.update_robotState(this.battlefield_Width, this.battlefield_height,
                getX(), getY(), getEnergy(),
                e.getEnergy(), e.getDistance());

        // Set operation mode to onAction
        my_opMode = opMode.onAction;
        // Print current state
//        logDebugInfo();
    }
    @Override
    public void onHitByBullet(HitByBulletEvent e){
        if(takeImmediate) {
            reward += immediatePenalty;
        }
    }

    @Override
    public void onBulletHit(BulletHitEvent e){
        if(takeImmediate) {
            reward += immediateBonus;
        }
    }

    @Override
    public void onBulletMissed(BulletMissedEvent e){
        if(takeImmediate) {
            reward += immediatePenalty;
        }
    }

    @Override
    public void onHitWall(HitWallEvent e){
        if(takeImmediate) {
            reward += immediatePenalty;
        }
        avidObstacle();
    }
    public void avidObstacle() {
        setBack(200);
        setTurnRight(60);
        execute();
    }
    @Override
    public void onHitRobot(HitRobotEvent e) {
        if(takeImmediate) {
            reward += immediatePenalty;
        }
        avidObstacle();
    }



    @Override
    public void onWin(WinEvent e){

        reward = terminalBonus;
        double calcualted_Q = 0.0;
        calcualted_Q = this.myLUT.calculate_Q(alpha, gamma, reward, onPolicy, previous_state, current_state);
        this.myLUT.setQValue(previous_state);


        winRound++;
        totalRound++;
        if((totalRound % 100 == 0) && (totalRound != 0)){
            winPercentage = (double) winRound / 100;
            System.out.println(String.format("%d, %.3f",++round, winPercentage));
//            File folderDst1 = getDataFile(fileToSaveName);
//            log.writeToFile(folderDst1, winPercentage, round);
            saveWinRateToTXT(totalRound, winRound);
//            myLUT.saveLUTToFile(getDataFile("LUT_" + totalRound + ".txt"));
            winRound = 0;


        }

    }

    @Override
    public void onDeath(DeathEvent e){

        reward = terminalPenalty;
        double calcualted_Q = 0.0;
        calcualted_Q = this.myLUT.calculate_Q(alpha, gamma, reward, onPolicy, previous_state, current_state);
        this.myLUT.setQValue(previous_state);
        totalRound++;



        if((totalRound % 100 == 0) && (totalRound != 0)){
            winPercentage = (double) winRound / 100;
            System.out.println(String.format("%d, %.3f",++round, winPercentage));
//            File folderDst1 = getDataFile(fileToSaveName);
//            log.writeToFile(folderDst1, winPercentage, round);
            saveWinRateToTXT(totalRound, winRound);
//            myLUT.saveLUTToFile(getDataFile("LUT_" + totalRound + ".txt"));
            winRound = 0;

            //saveTable();
        }


    }
    @Override
    public void onBattleEnded(BattleEndedEvent event) {
        // Call saveLUTToFile here when the battle ends
        if (!event.isAborted()) {
            File lutFile = getDataFile("LUT_end_of_battle.txt");
            myLUT.saveLUTToFile(lutFile);
        }
    }

    private void saveWinRateToTXT(int totalRounds, int winRounds) {
        double winRate = winRounds / 100.0;
        out.println("Saving win rate: " + winRate); // Debugging output

        try {
            // Use getDataFile to create a File object, then get the path
            String filePath = getDataFile("win_rate.txt").getAbsolutePath();

            // FileWriter and BufferedWriter for appending text to the file
            FileWriter fw = new FileWriter(filePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);

            // Write the data as a new line in the text file
            out.println(totalRounds + "," + winRounds + "," + String.format("%.2f", winRate));

            // Close the PrintWriter, which will also close BufferedWriter and FileWriter
            out.close();
        } catch (IOException e) {
            // In case of an exception, print a stack trace for debugging
            e.printStackTrace(out);
        }
    }





    }