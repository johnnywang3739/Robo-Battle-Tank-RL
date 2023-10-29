public class RobotState {
    private double x;
    private double y;
    private double hp;
    private double enemyHp;
    private double distanceToEnemy;
    private double enemyBearing;

    public enum HP {low, medium, high};
    public enum Distance {CLOSE, MEDIUM, FAR};
    public enum Action {fire, left, right, forward, back};
    public enum operaMode {onScan, onAction};

    // Constructor
    public RobotState() {
        // Initialize with default values
        this.x = 0.0;
        this.y = 0.0;
        this.hp = 0.0;
        this.enemyHp = 0.0;
        this.distanceToEnemy = 0.0;
        this.enemyBearing = 0.0;
    }
    public RobotState(RobotState other) {
        this.x = other.x;
        this.y = other.y;
        this.hp = other.hp;
        this.enemyHp = other.enemyHp;
        this.distanceToEnemy = other.distanceToEnemy;
    }
    // Setters
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setMyHp(double hp) { this.hp = hp; }
    public void setEnemyHp(double enemyHp) { this.enemyHp = enemyHp; }
    public void setDistanceToEnemy(double distanceToEnemy) { this.distanceToEnemy = distanceToEnemy; }
    public void setEnemyBearing(double enemyBearing) { this.enemyBearing = enemyBearing; }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getMyHp() { return hp; }
    public double getEnemyHp() { return enemyHp; }
    public double getDistanceToEnemy() { return distanceToEnemy; }
    public double getEnemyBearing() { return enemyBearing; }

    public Distance quantise_distacne(double distance){
        Distance quantised_distance = null;
        if( distance < 334){
            quantised_distance = Distance.CLOSE;
        }else if(distance < 667){
            quantised_distance = Distance.MEDIUM;
        }else{
            quantised_distance = Distance.FAR;
        }
        return quantised_distance;
    }

    // ... include other getters and setters as needed ...
}