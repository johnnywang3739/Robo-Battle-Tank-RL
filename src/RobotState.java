
public class RobotState {

    public enum HP {VERY_LOW, LOW, MEDIUM, HIGH};
    public enum Distance {CLOSE, MEDIUM, FAR};
    public enum Action {FIRE, GO_FORWARD, GO_BACK, TURN_RIGHT, TURN_LEFT};
//    public enum Heading {NORTH, EAST, SOUTH, WEST};
    private double x;
    private double y;
    private double my_hp;
    private double enemy_hp;
    private double raw_distanceToEnemy;

    private double enemyBearing;
    private double myHeading;

    private Distance quantised_distance_to_enemy;
    private Distance quantised_distance_from_centre;
    private HP my_HP_quantised;
    private HP enemy_HP_quantised;
    // Constructor
    private Action my_action;

//    private Heading quantised_my_heading;
//    private Velocity enemy_velocity_quantised;

    public RobotState() {
        // Initialize with default values
        this.x = 0.0;
        this.y = 0.0;
        this.my_hp = 100.0;
        this.enemy_hp = 100.0;
        this.raw_distanceToEnemy = 0.0;
        this.myHeading = 0.0;
        this.enemyBearing = 0.0;
        this.quantised_distance_to_enemy = Distance.CLOSE;
        this.quantised_distance_from_centre = Distance.CLOSE;
        this.my_HP_quantised = HP.HIGH;
        this.enemy_HP_quantised = HP.HIGH;
//        this.quantised_my_heading= Heading.NORTH;
        this.my_action = Action.GO_FORWARD;
    }
    public RobotState(RobotState other) {
        this.x = other.x;
        this.y = other.y;
        this.my_hp = other.my_hp;
        this.enemy_hp = other.enemy_hp;
        this.myHeading = other.myHeading;
        this.raw_distanceToEnemy = other.raw_distanceToEnemy;
        this.enemyBearing = other.enemyBearing;
        this.quantised_distance_to_enemy = other.quantised_distance_to_enemy;
        this.quantised_distance_from_centre = other.quantised_distance_from_centre;
        this.my_HP_quantised = other.my_HP_quantised ;
        this.enemy_HP_quantised = other.enemy_HP_quantised;
        this.my_action = other.my_action;
    }


    // Setters
    private void setX(double x) { this.x = x; }
    private void setY(double y) { this.y = y; }
    private void setMyHp(double hp) { this.my_hp = hp; }
    private void setEnemy_hp(double enemy_hp) { this.enemy_hp = enemy_hp; }
    private void setRawdistanceToEnemy(double raw_distanceToEnemy) { this.raw_distanceToEnemy = raw_distanceToEnemy; }
//    public void setEnemyBearing(double enemyBearing) { this.enemyBearing = enemyBearing; }
    public void setMy_action(Action my_action){ this.my_action = my_action;}
    private void set_my_heading(double my_heading){this.myHeading = my_heading;}
//    private void set_enemy_velocity(double velocity){this.enemy_velocity = velocity;}



    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getMyHp() { return my_hp; }
    public double getEnemy_hp() { return enemy_hp; }
    public double getRaw_distanceToEnemy() { return raw_distanceToEnemy; }
    public double getEnemyBearing() { return enemyBearing; }
    public Action getMy_action(){return my_action;}
    public Distance get_quantised_distance_to_enemy(){ return quantised_distance_to_enemy;}
    public Distance get_quantised_distance_from_centre(){ return quantised_distance_from_centre;}
    public HP get_my_HP_quantised(){ return this.my_HP_quantised;}
    public HP get_enemy_HP_quantised(){ return this.enemy_HP_quantised;}
//    public Heading getQuantised_my_heading(){return this.quantised_my_heading;};
//    public Velocity getQuantised_enemy_velocity(){return this.enemy_velocity_quantised;}

    public void quantise_distance_to_enemy() {
        if (this.raw_distanceToEnemy < 300) {
            this.quantised_distance_to_enemy = Distance.CLOSE;
        } else if (this.raw_distanceToEnemy < 600) {
            this.quantised_distance_to_enemy = Distance.MEDIUM;
        } else {
            this.quantised_distance_to_enemy = Distance.FAR;
        }
    }

    public void quantise_distance_from_centre(double field_width, double field_height) {
        double centerX = field_width / 2;
        double centerY = field_height / 2;

        // Calculate distance from the center
        double distanceFromCenter = Math.hypot(getX() - centerX, getY() - centerY);

        // Assuming the battlefield is a 800x600 field, divide it into quantized regions
        if (distanceFromCenter < 100) {
            this.quantised_distance_from_centre = Distance.CLOSE;
        } else if (distanceFromCenter < 200) {
            this.quantised_distance_from_centre = Distance.MEDIUM;
        } else {
            this.quantised_distance_from_centre = Distance.FAR;
        }
    }

    private HP quantise_HP(double hp) {
        if (hp <= 25) {
            return HP.VERY_LOW;
        } else if (hp <= 50) {
            return HP.LOW;
        } else if (hp <= 75) {
            return HP.MEDIUM;
        } else {
            return HP.HIGH;
        }
    }



    public void update_robotState(double width, double height, double x, double y, double my_energy, double enemy_energy,
                                  double distance){
        this.setX(x);
        this.setY(y);
        this.setMyHp(my_energy);
        this.setEnemy_hp(enemy_energy);
        this.setRawdistanceToEnemy(distance);
        this.set_my_heading(myHeading);
//        this.set_enemy_velocity(enemyVelocity);
        this.quantise_distance_to_enemy();
        this.quantise_distance_from_centre(width, height);
        this.my_HP_quantised = quantise_HP(this.my_hp);
        this.enemy_HP_quantised = quantise_HP(this.enemy_hp);
//        this.quantise_my_heading();
//        this.quantise_enemy_velocity();

    }



}