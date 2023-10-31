import Interface.LUTInterface;
import java.lang.Math;
import java.util.*;
import java.io.*;
import robocode.RobocodeFileOutputStream;
import java.io.PrintStream;
import java.io.IOException;
public class LUT implements LUTInterface{

    private int myHP_size;
    private int enemyHP_size;
    private int distance_size;
    private int distanceWall_size;
    private int action_size;
    private double[][][][][] lut;
    // visit: track for the used actions
    private int[][][][][] visit;
//    private RobotState previous_state;
//    private RobotState current_state;

    private double Q = 0.0;
    public LUT (int agentHP_size, int enemyHP_size, int distance_to_enemy_size, int distance_to_wall_size, int action_size){
        this.myHP_size = agentHP_size;
        this.enemyHP_size = enemyHP_size;
        this.distance_size = distance_to_enemy_size;
        this.distanceWall_size = distance_to_wall_size;
        this.action_size = action_size;
        this.lut = new double[agentHP_size][enemyHP_size][distance_to_enemy_size][distance_to_wall_size][action_size];
        this.visit = new int[agentHP_size][enemyHP_size][distance_to_enemy_size][distance_to_wall_size][action_size];
        initialiseLUT();
    };


    public void setQValue(RobotState previous_state) {
        int[] x = new int[]{
                previous_state.get_my_HP_quantised().ordinal(),
        previous_state.get_enemy_HP_quantised().ordinal(),
        previous_state.get_quantised_distance_to_enemy().ordinal(),
        previous_state.get_quantised_distance_to_wall().ordinal(),
        previous_state.getMy_action().ordinal()};

        this.lut[x[0]][x[1]][x[2]][x[3]][x[4]] = this.Q;
        this.visit[x[0]][x[1]][x[2]][x[3]][x[4]]++;
    }

    /**
     * Initialise the look up table to all zeros.
     */
    @Override
    public void initialiseLUT() {
        for(int i = 0; i < this.myHP_size ; i++) {
            for(int j = 0; j < this.enemyHP_size ; j++) {
                for(int k = 0; k < this.distance_size; k++) {
                    for(int m = 0; m < this.distanceWall_size; m++) {
                        for(int n = 0; n < this.action_size; n++) {
                            this.lut[i][j][k][m][n] =0.0;
                            this.visit[i][j][k][m][n] = 0;
                        }
                    }
                }
            }
        }
    }

    public int getRandomAction() {
        Random random = new Random();
        return random.nextInt(action_size); // [0, action_size)
    }

    public int getBestAction(int myHP_index, int enemyHP_index, int distance_enemy_index, int distance_wall_index) {
        double maxQ = Double.NEGATIVE_INFINITY;
        int actionIndex = -1;

        for(int i = 0; i < action_size; i++) {
            if(this.lut[myHP_index][enemyHP_index][distance_enemy_index][distance_wall_index][i] > maxQ) {
                actionIndex = i;
                maxQ = this.lut[myHP_index][enemyHP_index][distance_enemy_index][distance_wall_index][i];
            }
        }
        return actionIndex;
    }

    public double getQValue(int myHP, int enemyHP, int distance, int distanceWall, int action) {
        return this.lut[myHP][enemyHP][distance][distanceWall][action];
    }


    // Inside your LUT class
    public void printLUT() {
        for (int i = 0; i < myHP_size; i++) {
            for (int j = 0; j < enemyHP_size; j++) {
                for (int k = 0; k < distance_size; k++) {
                    for (int m = 0; m < distanceWall_size; m++) {
                        for (int n = 0; n < action_size; n++) {
                            double qValue = lut[i][j][k][m][n];
                            System.out.println("LUT[" + i + "][" + j + "][" + k + "][" + m + "][" + n + "] = " + qValue);
                        }
                    }
                }
            }
        }
    }


    public double calculate_Q(double alpha, double gamma, double reward, boolean policy, RobotState previous_state,
                              RobotState current_state){
        double previous_Q = getQValue(previous_state.get_my_HP_quantised().ordinal(),
                previous_state.get_enemy_HP_quantised().ordinal(), previous_state.get_quantised_distance_to_enemy().ordinal(),
                previous_state.get_quantised_distance_to_wall().ordinal(), previous_state.getMy_action().ordinal());

        double current_Q = getQValue(current_state.get_my_HP_quantised().ordinal(),
                current_state.get_enemy_HP_quantised().ordinal(), current_state.get_quantised_distance_to_enemy().ordinal(),
                current_state.get_quantised_distance_to_wall().ordinal(), current_state.getMy_action().ordinal());

        int current_best_action_ordinal = getBestAction(current_state.get_my_HP_quantised().ordinal(),
                current_state.get_enemy_HP_quantised().ordinal(), current_state.get_quantised_distance_to_enemy().ordinal(),
                current_state.get_quantised_distance_to_wall().ordinal());

        double maxQ = getQValue(current_state.get_my_HP_quantised().ordinal(),
                current_state.get_enemy_HP_quantised().ordinal(), current_state.get_quantised_distance_to_enemy().ordinal(),
                current_state.get_quantised_distance_to_wall().ordinal(), current_best_action_ordinal);

        this.Q = policy ?
                previous_Q + alpha * (reward + gamma * current_Q - previous_Q) :
                previous_Q + alpha * (reward + gamma * maxQ - previous_Q);
        return this.Q;
    }

    // Add this method to save the LUT to a file
    public void saveLUTToFile(File file) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            for (int a = 0; a < myHP_size; a++) {
                for (int b = 0; b < enemyHP_size; b++) {
                    for (int c = 0; c < distance_size; c++) {
                        for (int d = 0; d < distanceWall_size; d++) {
                            for (int e = 0; e < action_size; e++) {
                                // Write the LUT values and the visit count for each state-action pair
                                writer.println(a + "," + b + "," + c + "," + d + "," + e + ","
                                        + this.lut[a][b][c][d][e] + "," + this.visit[a][b][c][d][e]);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing to LUT file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * A helper method that translates a vector being used to index the look up table
     * into an ordinal that can then be used to access the associated look up table element
     * @param X The state action vector used to index the LookUpTable
     * @return The index where this vector maps to
     */
//    public int indexFor(double [] X){
//      return 0;
//    };

}
