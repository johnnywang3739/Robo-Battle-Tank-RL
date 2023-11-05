
public class RobotState {

    public enum Energy {VERY_LOW, LOW, MEDIUM, HIGH};
    public enum Distance {CLOSE, MEDIUM, FAR};
    public enum Action {FIRE_CANNON, GO_FORWARD, GO_BACK, TURN_RIGHT, TURN_LEFT};
//    public enum Heading {NORTH, EAST, SOUTH, WEST};
    private double x;
    private double y;
    private double my_energy;
    private double enemy_energy;
    private double raw_distanceToEnemy;
    private double enemyBearing;
    private double myHeading;
    private Distance quantised_distance_to_enemy;
    private Distance quantised_distance_from_centre;
    private Energy my_Energy_quantised;
    private Energy enemy_Energy_quantised;
    // Constructor
    private Action my_action;

//    private Heading quantised_my_heading;
//    private Velocity enemy_velocity_quantised;

    public RobotState() {
        // Initialize with default values
        this.x = 0.0;
        this.y = 0.0;
        this.my_energy = 100.0;
        this.enemy_energy = 100.0;
        this.raw_distanceToEnemy = 0.0;
        this.myHeading = 0.0;
        this.enemyBearing = 0.0;
        this.quantised_distance_to_enemy = Distance.CLOSE;
        this.quantised_distance_from_centre = Distance.CLOSE;
        this.my_Energy_quantised = Energy.HIGH;
        this.enemy_Energy_quantised = Energy.HIGH;
//        this.quantised_my_heading= Heading.NORTH;
        this.my_action = Action.GO_FORWARD;
    }
    public RobotState(RobotState other) {
        this.x = other.x;
        this.y = other.y;
        this.my_energy = other.my_energy;
        this.enemy_energy = other.enemy_energy;
        this.myHeading = other.myHeading;
        this.raw_distanceToEnemy = other.raw_distanceToEnemy;
        this.enemyBearing = other.enemyBearing;
        this.quantised_distance_to_enemy = other.quantised_distance_to_enemy;
        this.quantised_distance_from_centre = other.quantised_distance_from_centre;
        this.my_Energy_quantised = other.my_Energy_quantised;
        this.enemy_Energy_quantised = other.enemy_Energy_quantised;
        this.my_action = other.my_action;
    }

    // Setters
    private void setX(double x) { this.x = x; }
    private void setY(double y) { this.y = y; }
    private void set_my_energy(double energy) { this.my_energy = energy; }
    private void set_enemy_energy(double enemy_energy) { this.enemy_energy = enemy_energy; }
    private void set_raw_distance_Enemy(double raw_distance_with_enemy) { this.raw_distanceToEnemy = raw_distance_with_enemy; }
//    public void setEnemyBearing(double enemyBearing) { this.enemyBearing = enemyBearing; }
    public void set_action(Action my_action){ this.my_action = my_action;}
    private void set_my_heading(double my_heading){this.myHeading = my_heading;}
//    private void set_enemy_velocity(double velocity){this.enemy_velocity = velocity;}
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public Action get_my_action(){return my_action;}
    public Distance get_quantised_distance_to_enemy(){ return quantised_distance_to_enemy;}
    public Distance get_quantised_distance_from_centre(){ return quantised_distance_from_centre;}
    public Energy get_my_energy_quantised(){ return this.my_Energy_quantised;}
    public Energy get_enemy_energy_quantised(){ return this.enemy_Energy_quantised;}
//    public Heading getQuantised_my_heading(){return this.quantised_my_heading;};
//    public Velocity getQuantised_enemy_velocity(){return this.enemy_velocity_quantised;}

    public void quantise_distance_to_enemy() {
        if (this.raw_distanceToEnemy < 333) {
            this.quantised_distance_to_enemy = Distance.CLOSE;
        } else if (this.raw_distanceToEnemy < 667) {
            this.quantised_distance_to_enemy = Distance.MEDIUM;
        } else {
            this.quantised_distance_to_enemy = Distance.FAR;
        }
    }
    public void quantise_distance_from_centre(double field_width, double field_height) {
        double centerX = field_width / 2;
        double centerY = field_height / 2;

        double distance_from_center = Math.hypot(getX() - centerX, getY() - centerY);
        if (distance_from_center < 100) {
            this.quantised_distance_from_centre = Distance.CLOSE;
        } else if (distance_from_center < 200) {
            this.quantised_distance_from_centre = Distance.MEDIUM;
        } else {
            this.quantised_distance_from_centre = Distance.FAR;
        }
    }

    private Energy quantise_energy(double hp) {
        if (hp <= 25) {
            return Energy.VERY_LOW;
        } else if (hp <= 50) {
            return Energy.LOW;
        } else if (hp <= 75) {
            return Energy.MEDIUM;
        } else {
            return Energy.HIGH;
        }
    }
    public void update_robotState(double width, double height, double x, double y, double my_energy, double enemy_energy,
                                  double distance){
        this.setX(x);
        this.setY(y);
        this.set_my_energy(my_energy);
        this.set_enemy_energy(enemy_energy);
        this.set_raw_distance_Enemy(distance);
        this.set_my_heading(myHeading);
//        this.set_enemy_velocity(enemyVelocity);
        this.quantise_distance_to_enemy();
        this.quantise_distance_from_centre(width, height);
        this.my_Energy_quantised = quantise_energy(this.my_energy);
        this.enemy_Energy_quantised = quantise_energy(this.enemy_energy);
//        this.quantise_my_heading();
//        this.quantise_enemy_velocity();

    }
}