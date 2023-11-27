import Interface.LUTInterface;
import java.util.*;
import java.io.*;
import java.io.IOException;
public class LUT implements LUTInterface{
    private int my_energy_size;
    private int enemy_energy_size;
    private int distance_size;
    private int distance_from_centre_size;
    private int action_size;
    private double[][][][][] lut;
    private int[][][][][] visit;     // visit: track for the used actions
    private double Q = 0.0;
    public LUT (int agent_energy_size, int enemy_energy_size,
                int distance_to_enemy_size, int distance_to_wall_size, int action_size){
        this.my_energy_size = agent_energy_size;
        this.enemy_energy_size = enemy_energy_size;
        this.distance_size = distance_to_enemy_size;
        this.distance_from_centre_size = distance_to_wall_size;
        this.action_size = action_size;
        this.lut = new double[agent_energy_size][enemy_energy_size][distance_to_enemy_size]
                [distance_to_wall_size][action_size];
        this.visit = new int[agent_energy_size][enemy_energy_size][distance_to_enemy_size]
                [distance_to_wall_size][action_size];
        initialiseLUT();
    };


    public void setQValue(RobotState state) {
        int[] x = new int[]{
                state.get_my_energy_quantised().ordinal(),
        state.get_enemy_energy_quantised().ordinal(),
        state.get_quantised_distance_to_enemy().ordinal(),
        state.get_quantised_distance_from_centre().ordinal(),
//                previous_state.getQuantised_my_heading().ordinal(),
        state.get_my_action().ordinal()};

        this.lut[x[0]][x[1]][x[2]][x[3]][x[4]] = this.Q;
        this.visit[x[0]][x[1]][x[2]][x[3]][x[4]]++;
    }
    @Override
    public void initialiseLUT() {
        for(int i = 0; i < this.my_energy_size; i++) {
            for(int j = 0; j < this.enemy_energy_size; j++) {
                for(int k = 0; k < this.distance_size; k++) {
                    for(int m = 0; m < this.distance_from_centre_size; m++) {
                            for(int n = 0; n < this.action_size; n++) {
                                this.lut[i][j][k][m][n] = 0.0; // Initialise the look up table to all zeros.
                                this.visit[i][j][k][m][n] = 0;
                             }
                    }
                }
            }
        }
    }
    public int get_random_action() {
        Random random = new Random();
        return random.nextInt(action_size); // [0, action_size)
    }

    public int calculate_best_action(RobotState state) {
        int myEnergy_index = state.get_my_energy_quantised().ordinal();
        int enemyEnergy_index = state.get_enemy_energy_quantised().ordinal();
        int distance_to_enemy_index = state.get_quantised_distance_to_enemy().ordinal();
        int distance_to_centre_index = state.get_quantised_distance_from_centre().ordinal();
        double maxQ = Double.NEGATIVE_INFINITY;
        int actionIndex = -1;

        for(int i = 0; i < action_size; i++) {
            if(this.lut[myEnergy_index][enemyEnergy_index][distance_to_enemy_index]
                    [distance_to_centre_index][i] > maxQ) {
                actionIndex = i;
                maxQ = this.lut[myEnergy_index][enemyEnergy_index][distance_to_enemy_index]
                        [distance_to_centre_index][i];
            }
        }
        return actionIndex;
    }

    public double getQValue(RobotState state, int action_index) {

        return this.lut[state.get_my_energy_quantised().ordinal()][state.get_enemy_energy_quantised().ordinal()]
                [state.get_quantised_distance_to_enemy().ordinal()][state.get_quantised_distance_from_centre().ordinal()]
                [action_index];
    }


    public double computeQ(double alpha, double gamma, double reward, MyAgent_LUT.Policy policy, RobotState previous_state,
                           RobotState current_state){
        double previous_Q = getQValue(previous_state, previous_state.get_my_action().ordinal());

        // For on-policy (SARSA), use the Q-value of the action actually taken in the current state.
        double next_Q_onPolicy = getQValue(current_state, current_state.get_my_action().ordinal());
        int best_action_index_offPolicy = calculate_best_action(current_state);
        double maxQ_offPolicy = getQValue(current_state, best_action_index_offPolicy);
        double next_Q;
        if (policy == MyAgent_LUT.Policy.onPolicy) {
            next_Q = next_Q_onPolicy;
        } else {
            next_Q = maxQ_offPolicy;
        }
        // Update the Q-value
        this.Q = previous_Q + alpha * (reward + gamma * next_Q - previous_Q);
        this.setQValue(previous_state);
        return this.Q;
    }
    public void save(File file) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            for (int a = 0; a < my_energy_size; a++) {
                for (int b = 0; b < enemy_energy_size; b++) {
                    for (int c = 0; c < distance_size; c++) {
                        for (int d = 0; d < distance_from_centre_size; d++) {
                                for (int e = 0; e < action_size; e++) {
                                    writer.println(a + "," + b + "," + c + "," + d + "," + e + ","
                                            + this.lut[a][b][c][d][e]+ ","+this.visit[a][b][c][d][e]);
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
    public void load(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 7) {
                    int a = Integer.parseInt(values[0]);
                    int b = Integer.parseInt(values[1]);
                    int c = Integer.parseInt(values[2]);
                    int d = Integer.parseInt(values[3]);
                    int e = Integer.parseInt(values[4]);
                    double qValue = Double.parseDouble(values[5]);
                    int visits = Integer.parseInt(values[6]);
                    if (a < my_energy_size && b < enemy_energy_size && c < distance_size
                            && d < distance_from_centre_size && e < action_size) {
                        this.lut[a][b][c][d][e] = qValue;
                        this.visit[a][b][c][d][e] = visits;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("LUT not found: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error reading LUT file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
