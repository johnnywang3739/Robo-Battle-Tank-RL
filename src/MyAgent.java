import robocode.*;

import java.awt.Color;

import java.io.*;


public class MyAgent extends AdvancedRobot {
    private static RobotState current_state = new RobotState(); // Initialize with a new state
    private static RobotState previous_state = new RobotState(); // Initialize with a new state
    public enum opMode { onScan, onAction };
    public static int current_action_index;
    public static double enemyBearing;
    private static opMode my_opMode = opMode.onScan;


    private double battlefield_Width = 800.0;
    private double battlefield_height = 600.0;

    private final double immediateBonus = 0.5;
    private final double terminalBonus = 1.0;
    private final double immediatePenalty = -0.1;
    private final double terminalPenalty = -0.2;
    // Whether take immediate rewards
    public static boolean takeImmediate = true;
    // Whether take on-policy algorithm
    public static boolean onPolicy = false;
    // Discount factor
    private double gamma = 0.8;
    private double alpha = 0.3;
    private static double epsilon = 0.9;
    // Q

    // Reward
    private static double reward = 0.0;
    private static double totalReward = 0.0;


    // static numbers for winning rounds
    public static int totalRound = 0;
    public static int round = 0;
    public static int winRound = 0;
    //    public static double[] winPercentage = new double[351];
    public static double winPercentage = 0.0;

//    private final double epsilonDecayRate = 0.9999; // Adjust this value as needed
//    private final double minEpsilon = 0.01;


    public static LUT myLUT = new LUT(
            RobotState.HP.values().length,
            RobotState.HP.values().length,
            RobotState.Distance.values().length,
            RobotState.Distance.values().length,
            RobotState.Action.values().length
    );

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
//            System.out.println(winRound);
            switch (my_opMode) {
                case onScan:
//                    System.out.println("Scanning...");
                    reward = 0.0;
                    turnRadarRight(180.0); // Scan for other robots
//                    myLUT.printLUT();
                    break;
                case onAction:
//                    System.out.println("Acting...");
                    // Perform action based on the current state
                    // ...
                    current_action_index = (Math.random() <= epsilon)
                            ? this.myLUT.getRandomAction()
                            : this.myLUT.getBestAction(current_state.get_my_HP_quantised().ordinal(),
                            current_state.get_enemy_HP_quantised().ordinal(),
                            current_state.get_quantised_distance_to_enemy().ordinal(),
                            current_state.get_quantised_distance_from_centre().ordinal());


//                    curAction = Action.values()[curActionIndex];
                    current_state.setMy_action(RobotState.Action.values()[current_action_index]);
                    System.out.println(current_state.getMy_action());


                    switch (current_state.getMy_action()) {
                        case FIRE: {
                            turnGunRight(getHeading() - getGunHeading() + enemyBearing);
                            fire(3);
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
        // Retrieve enemy velocity
        double enemyVelocity = e.getVelocity();
        double myHeading = getHeading();

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
            totalReward += reward;
        }
    }

    @Override
    public void onBulletHit(BulletHitEvent e){
        if(takeImmediate) {
            reward += immediateBonus;
            totalReward += reward;
        }
    }

    @Override
    public void onBulletMissed(BulletMissedEvent e){
        if(takeImmediate) {
            reward += immediatePenalty;
            totalReward += reward;
        }
    }

    @Override
    public void onHitWall(HitWallEvent e){
        if(takeImmediate) {
            reward += immediatePenalty;
            totalReward += reward;
        }
        goToCentre(battlefield_Width, battlefield_height);

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
            totalReward += reward;
        }
        avidObstacle();
    }



    @Override
    public void onWin(WinEvent e){

        winRound++;
        totalRound++;

        reward = terminalBonus;
        totalReward += reward;
        double calcualted_Q = 0.0;
        calcualted_Q = this.myLUT.calculate_Q(alpha, gamma, reward, onPolicy, previous_state, current_state);
        this.myLUT.setQValue(previous_state);
        double meanTotalReward = totalReward / totalRound;
        saveMeanTotalRewardToTXT(totalRound, meanTotalReward);
        reward = 0.0;
        updateEpsilon();


        if((totalRound % 100 == 0) && (totalRound != 0)){
            winPercentage = (double) winRound / 100;
            System.out.println(String.format("%d, %.3f",++round, winPercentage));
//            File folderDst1 = getDataFile(fileToSaveName);
//            log.writeToFile(folderDst1, winPercentage, round);
            saveWinRateToTXT(totalRound, winRound);

//            File lutFile = getDataFile("LUT_end_of_battle.txt");
//            myLUT.saveLUTToFile(lutFile);
            myLUT.saveLUTToFile(getDataFile("LUT"  + ".txt"));
//            myLUT.saveLUTToFile(getDataFile("LUT_" + totalRound + ".txt"));
            winRound = 0;


        }

    }

    @Override
    public void onDeath(DeathEvent e){
        totalRound++;
        reward = terminalPenalty;
        totalReward += reward;
        double calcualted_Q = 0.0;
        calcualted_Q = this.myLUT.calculate_Q(alpha, gamma, reward, onPolicy, previous_state, current_state);
        this.myLUT.setQValue(previous_state);
        double meanTotalReward = totalReward / totalRound;
        saveMeanTotalRewardToTXT(totalRound, meanTotalReward);
        reward = 0.0;
        updateEpsilon();


        if((totalRound % 100 == 0) && (totalRound != 0)){
            winPercentage = (double) winRound / 100;
            System.out.println(String.format("%d, %.3f",++round, winPercentage));
//            File folderDst1 = getDataFile(fileToSaveName);
//            log.writeToFile(folderDst1, winPercentage, round);
            saveWinRateToTXT(totalRound, winRound);
//            File lutFile = getDataFile("LUT_end_of_battle.txt");
//            myLUT.saveLUTToFile(lutFile);
            myLUT.saveLUTToFile(getDataFile("LUT"  + ".txt"));
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

    public void goToCentre(double field_width, double field_height) {
        double centerX = field_width / 2;
        double centerY = field_height / 2;

        // Calculate the angle to the center from the current position
        double angleToCenter = Math.toDegrees(Math.atan2(centerX - getX(), centerY - getY()));

        // Calculate the difference between the robot's current heading and the angle to the center
        double turnAngle = angleToCenter - getHeading();

        // Normalize the turn angle to be within the range [-180, 180]
        turnAngle += turnAngle > 180 ? -360 : (turnAngle < -180 ? 360 : 0);

        // Turn the robot to face the center
        turnRight(turnAngle);

        // Move towards the center
//        double distanceToCenter = Math.sqrt(Math.pow(centerX - getX(), 2) + Math.pow(centerY - getY(), 2));
        ahead(200);
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

    public double normalRelativeAngleDegrees(double angle) {
        double normalizedAngle = angle % 360;
        if (normalizedAngle > 180) {
            return normalizedAngle - 360;
        } else if (normalizedAngle < -180) {
            return normalizedAngle + 360;
        }
        return normalizedAngle;
    }

    private void updateEpsilon() {
        // Set epsilon to 0.1 after 20000 rounds, otherwise keep it as is.
//        if (totalRound >= 3000) {
//            epsilon = 0.0;
//        }
//        if (totalRound <= 5000) {
//            epsilon = 0.9 - (0.89/5000) * totalRound;
//        } else {
//            epsilon = 0.01;
//        }
        int cutoff_Round = 5000; // The round at which epsilon should be 0.01
        double finalEpsilon = 0.01; // The final value of epsilon
        double initialEpsilon = 0.9; // The initial value of epsilon

        if (totalRound <= cutoff_Round) {
            double decayRate = -Math.log(finalEpsilon / initialEpsilon) / cutoff_Round;
            epsilon = initialEpsilon * Math.exp(-decayRate * totalRound);
        } else {
            epsilon = finalEpsilon;
        }
    }
    private void saveMeanTotalRewardToTXT(int totalRounds, double meanTotalReward) {
        out.println("Saving mean total reward: " + meanTotalReward); // Debugging output

        try {
            String filePath = getDataFile("mean_total_reward.txt").getAbsolutePath();
            FileWriter fw = new FileWriter(filePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);

            out.println(totalRounds + "," + String.format("%.2f", meanTotalReward));
            out.close();
        } catch (IOException e) {
            e.printStackTrace(out);
        }
    }







}