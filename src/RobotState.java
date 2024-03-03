
public class RobotState {
    public enum Action {FIRE_CANNON, GO_FORWARD, GO_BACK, TURN_RIGHT, TURN_LEFT};
    private double x;
    private double y;
    private double my_energy;
    private double enemy_energy;
    private double raw_distanceToEnemy;
    private double enemyBearing;
    private double myHeading;
    private double raw_distanceFromCentre;

    private double scaled_myEnergy;
    private double scaled_enemyEnergy;
    private double scaled_distanceToEnemy;
    private double scaled_distanceFromCentre;

//    // Constructor
    private Action my_action;

    public RobotState() {
        // Initialize with default values
        this.x = 0.0;
        this.y = 0.0;
        this.my_energy = 100.0;
        this.enemy_energy = 100.0;
        this.raw_distanceToEnemy = 0.0;
        this.raw_distanceFromCentre = 0.0;

        this.myHeading = 0.0;
        this.enemyBearing = 0.0;
        this.scaled_myEnergy = this.my_energy / 25;
        this.scaled_enemyEnergy = this.enemy_energy / 25;
        this.scaled_distanceFromCentre = this.raw_distanceFromCentre / 167;
        this.scaled_distanceToEnemy = this.scaled_distanceToEnemy / 167;
        this.my_action = Action.GO_FORWARD;
    }
    public RobotState(RobotState other) {
        this.x = other.x;
        this.y = other.y;
        this.my_energy = other.my_energy;
        this.enemy_energy = other.enemy_energy;
        this.myHeading = other.myHeading;
        this.raw_distanceToEnemy = other.raw_distanceToEnemy;
        this.raw_distanceFromCentre = other.raw_distanceFromCentre;
        this.enemyBearing = other.enemyBearing;
        this.scaled_distanceToEnemy = other.scaled_distanceToEnemy;
        this.scaled_distanceFromCentre = other.scaled_distanceFromCentre;
        this.scaled_myEnergy = other.scaled_myEnergy;
        this.scaled_enemyEnergy = other.scaled_enemyEnergy;
        this.my_action = other.my_action;
    }

    // Setters
    private void setX(double x) { this.x = x; }
    private void setY(double y) { this.y = y; }
    private void set_my_energy(double energy) { this.my_energy = energy; }
    private void set_enemy_energy(double enemy_energy) { this.enemy_energy = enemy_energy; }
    private void set_raw_distance_Enemy(double raw_distance_with_enemy)
    { this.raw_distanceToEnemy = raw_distance_with_enemy; }
    public void set_action(Action my_action){ this.my_action = my_action;}
    private void set_my_heading(double my_heading){this.myHeading = my_heading;}
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public Action get_my_action(){return my_action;}

    public double getScaled_distanceToEnemy(){return this.scaled_distanceToEnemy;}
    public double getScaled_distanceFromCentre(){return this.scaled_distanceFromCentre;}
    public double getScaled_myEnergy(){return this.my_energy;}
    public double getScaled_enemyEnergy(){return this.enemy_energy;}
    // before unquantise, would be 0, 1, 2 mapped to range from 0 - 500. now could be raw / 167
    public void setScaled_distances(double field_width, double field_height) {
        double centerX = field_width / 2;
        double centerY = field_height / 2;

        double distance_from_center = Math.hypot(getX() - centerX, getY() - centerY);
        this.scaled_distanceFromCentre = distance_from_center / 167;
        this.scaled_distanceToEnemy = this.scaled_distanceToEnemy / 167;
    }
    // before unquantise, would be 0, 1, 2 , 3 mapped to range from 0 - 100, now could be raw / 25
    private void setScaled_energy() {
        this.scaled_myEnergy = this.my_energy / 25;
        this.scaled_enemyEnergy = this.enemy_energy / 25;
    }
    public void update_robotState(double width, double height, double x, double y, double my_energy,
                                  double enemy_energy,
                                  double distance){
        this.setX(x);
        this.setY(y);
        this.set_my_energy(my_energy);
        this.set_enemy_energy(enemy_energy);
        this.set_raw_distance_Enemy(distance);
        this.set_my_heading(myHeading);
        this.setScaled_distances(width, height);
        this.setScaled_energy();

    }
}