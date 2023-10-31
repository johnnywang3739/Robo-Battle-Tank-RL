
public class RobotState {

    public enum HP {LOW, MEDIUM, HIGH};
    public enum Distance {CLOSE, MEDIUM, FAR};
    public enum Action {FIRE, TURN_LEFT, TURN_RIGHT, GO_FORWARD, GO_BACKWARD};
    private double x;
    private double y;
    private double my_hp;
    private double enemy_hp;
    private double raw_distanceToEnemy;

    private double enemyBearing;

    private Distance quantised_distance_to_enemy;
    private Distance quantised_distance_to_wall;
    private HP my_HP_quantised;
    private HP enemy_HP_quantised;
    // Constructor
    private Action my_action;
    public RobotState() {
        // Initialize with default values
        this.x = 0.0;
        this.y = 0.0;
        this.my_hp = 100.0;
        this.enemy_hp = 100.0;
        this.raw_distanceToEnemy = 0.0;
        this.enemyBearing = 0.0;
        this.quantised_distance_to_enemy = Distance.CLOSE;
        this.quantised_distance_to_wall = Distance.CLOSE;
        this.my_HP_quantised = HP.HIGH;
        this.enemy_HP_quantised = HP.HIGH;
        this.my_action = Action.GO_FORWARD;
    }
    public RobotState(RobotState other) {
        this.x = other.x;
        this.y = other.y;
        this.my_hp = other.my_hp;
        this.enemy_hp = other.enemy_hp;
        this.raw_distanceToEnemy = other.raw_distanceToEnemy;
        this.enemyBearing = other.enemyBearing;
        this.quantised_distance_to_enemy = other.quantised_distance_to_enemy;
        this.quantised_distance_to_wall = other.quantised_distance_to_wall;
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



    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getMyHp() { return my_hp; }
    public double getEnemy_hp() { return enemy_hp; }
    public double getRaw_distanceToEnemy() { return raw_distanceToEnemy; }
    public double getEnemyBearing() { return enemyBearing; }
    public Action getMy_action(){return my_action;}
    public Distance get_quantised_distance_to_enemy(){ return quantised_distance_to_enemy;}
    public Distance get_quantised_distance_to_wall(){ return quantised_distance_to_wall;}
    public HP get_my_HP_quantised(){ return this.my_HP_quantised;}
    public HP get_enemy_HP_quantised(){ return this.enemy_HP_quantised;}

    public void quantise_distance_to_enemy(){

        if( this.raw_distanceToEnemy  < 334){
            this.quantised_distance_to_enemy = Distance.CLOSE;
        }else if(this.raw_distanceToEnemy  < 667){
            this.quantised_distance_to_enemy  = Distance.MEDIUM;
        }else{
            this.quantised_distance_to_enemy  = Distance.FAR;
        }
    }

    public void quantise_distacne_to_wall(double field_width, double field_height ) {
        Distance disWLevel = null;
        double width = field_width;
        double height = field_height;
        double dist = getY();
        double disb = height - getY();
        double disl = getX();
        double disr = width - getX();
        if(dist < 30 || disb < 30 || disl < 30 || disr < 30) {
            this.quantised_distance_to_wall = Distance.CLOSE;
        } else if(dist < 80 || disb < 80 || disl < 80 || disr < 80) {
            this.quantised_distance_to_wall = Distance.MEDIUM;
        } else {
            this.quantised_distance_to_wall = Distance.FAR;
        }

    }


    private HP quantise_HP(double hp) {
        HP HP_LEVEL = null;
        if(hp < 0) {
            return HP_LEVEL;
        } else if(hp <= 33) {
            HP_LEVEL = HP.LOW;
        } else if(hp <= 67) {
            HP_LEVEL = HP.MEDIUM;
        } else {
            HP_LEVEL = HP.HIGH;
        }
        return HP_LEVEL;
    }


    public void update_robotState(double width, double height, double x, double y, double my_energy, double enemy_energy,
                                  double distance){
        this.setX(x);
        this.setY(y);
        this.setMyHp(my_energy);
        this.setEnemy_hp(enemy_energy);
        this.setRawdistanceToEnemy(distance);
        this.quantise_distance_to_enemy();
        this.quantise_distacne_to_wall(width, height);
        this.my_HP_quantised = quantise_HP(this.my_hp);
        this.enemy_HP_quantised = quantise_HP(this.enemy_hp);

    }

    // ... include other getters and setters as needed ...
}