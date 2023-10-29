import robocode.*;
import java.awt.*;

public class MyAgent extends AdvancedRobot {
    private RobotState current_state = new RobotState(); // Initialize with a new state
    private RobotState previous_state = new RobotState(); // Initialize with a new state
    public enum opMode { onScan, onAction };
    private opMode my_opMode = opMode.onScan;

    @Override
    public void run() {

        // Set the robot colors
        setBodyColor(Color.red);
        setGunColor(Color.darkGray);
        setRadarColor(Color.white);
        setBulletColor(Color.red);
        // Debug output to Robocode screen


        // Main robot loop
        while (true) {
            switch (my_opMode) {
                case onScan:
                    turnRadarRight(360); // Scan for other robots
                    break;
                case onAction:
                    // Perform action based on the current state
                    // ...
                    break;
            }
            ahead(100);
            turnGunRight(360);
            back(100);
            turnGunRight(360);

            // Perform the drawing at the end of each loop iteration
            execute(); // Ensure all pending actions are executed before drawing
            Graphics2D g = getGraphics(); // Get the graphics context for drawing
            if (g != null) { // Check if the graphics context is available
                g.setColor(Color.white);
                g.drawString("X: " + current_state.getX(), 20, 20);
                g.drawString("Y: " + current_state.getY(), 20, 40);
                g.drawString("My HP: " + current_state.getMyHp(), 20, 60);
                g.drawString("Enemy HP: " + current_state.getEnemyHp(), 20, 80);
                g.drawString("Distance to Enemy: " + current_state.getDistanceToEnemy(), 20, 100);
            }
        }


    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        // Copy current state to previous state
        previous_state = new RobotState(current_state);
        // Update current state with the new scanned data
        current_state.setX(getX());
        current_state.setY(getY());
        current_state.setMyHp(getEnergy());
        current_state.setEnemyHp(e.getEnergy());
        current_state.setDistanceToEnemy(e.getDistance());
        // Set operation mode to onAction
        my_opMode = opMode.onAction;



        fire(1);
    }
    /**
     * onHitByBullet: What to do when you're hit by a bullet
     */
    public void onHitByBullet(HitByBulletEvent e) {
        turnLeft(90 - e.getBearing());
    }



    @Override
    public void onPaint(Graphics2D g) {
        // Set the paint color for drawing
        g.setColor(Color.white);
        // Draw strings on the battlefield
        g.drawString("X: " + current_state.getX(), 20, 20);
        g.drawString("Y: " + current_state.getY(), 20, 40);
        g.drawString("My HP: " + current_state.getMyHp(), 20, 60);
        g.drawString("Enemy HP: " + current_state.getEnemyHp(), 20, 80);
        g.drawString("Distance to Enemy: " + current_state.getDistanceToEnemy(), 20, 100);
    }


    }