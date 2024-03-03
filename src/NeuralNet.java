import java.io.*;
import java.util.Random;
import ReplayMemory.*;
import Interface.NeuralNetInterface;

public class NeuralNet implements NeuralNetInterface {
    private int input_layer_num;
    private int hidden_layer_num;
    private int output_layer_num;
    private double lr;
    private double momentum;
    private double[] input_layer, hidden_layer;
    private double[] output_layer;
    private double[][] weights_input_hidden;
    private double[][] weights_hidden_output;
    private double[][] delta_weights_input_hidden;
    private double[][] delta_weights_hidden_output;
    private double[] sample_error; // an array for multiple outputs
    private int epochs;
    private String dataset_type;
    private double TD_error = 0.0;
    public double RMS_error= 0.0;

    public NeuralNet(int argNumInputs, int argNumHidden, int argNumOutputs,
                     double argLearningRate, double argMomentumTerm, String dataset_type) {
        this.input_layer_num = argNumInputs;
        this.hidden_layer_num = argNumHidden;
        this.output_layer_num = argNumOutputs;
        this.lr = argLearningRate;
        this.momentum = argMomentumTerm;
        this.dataset_type = dataset_type;

        this.input_layer = new double[this.input_layer_num + 1]; // +1 for bias term
        this.hidden_layer = new double[this.hidden_layer_num + 1]; // +1 for bias term
        this.output_layer = new double[this.output_layer_num]; // Array for multiple outputs

        this.weights_input_hidden = new double[this.input_layer_num + 1][this.hidden_layer_num];
        this.weights_hidden_output = new double[this.hidden_layer_num + 1][this.output_layer_num];
        this.delta_weights_input_hidden = new double[this.input_layer_num + 1][this.hidden_layer_num];
        this.delta_weights_hidden_output = new double[this.hidden_layer_num + 1][this.output_layer_num];
        this.sample_error = new double[this.output_layer_num]; // Array for multiple outputs

        this.input_layer[this.input_layer_num] = 1; // Set the bias term to 1
        this.hidden_layer[this.hidden_layer_num] = 1; // Set the bias term to 1

        initializeWeights();
    }
    @Override
    public void initializeWeights() {
//        System.out.println("[INFO] Initializing Weights...");
        this.weights_input_hidden = new double[this.input_layer_num + 1][this.hidden_layer_num];
        this.delta_weights_input_hidden = new double[this.input_layer_num + 1][this.hidden_layer_num];
        this.weights_hidden_output = new double[this.hidden_layer_num + 1][this.output_layer_num];
        this.delta_weights_hidden_output = new double[this.hidden_layer_num + 1][this.output_layer_num];
        // Print dimensions of weight matrices
//        System.out.println("[Info] W1 matrix size: [" + (this.input_layer_num + 1) + " x " + this.hidden_layer_num + "]");
//        System.out.println("[Info] W2 matrix size: [" + (this.hidden_layer_num + 1) + " x " + this.output_layer_num + "]");
        double min_inputhidden = Double.MAX_VALUE;
        double max_inputhidden = Double.MIN_VALUE;
        double min_hiddenoutput = Double.MAX_VALUE;
        double max_hiddenoutput = Double.MIN_VALUE;
        for (int i = 0; i < this.input_layer_num + 1; i++) {
            for (int j = 0; j < this.hidden_layer_num; j++) {
                this.weights_input_hidden[i][j] = Math.random() - 0.5; // Initialize weights between -0.5 and 0.5
                min_inputhidden = Math.min(min_inputhidden, this.weights_input_hidden[i][j]);
                max_inputhidden = Math.max(max_inputhidden, this.weights_input_hidden[i][j]);
            }
        }
        for (int i = 0; i < this.hidden_layer_num + 1; i++) {
            for (int j = 0; j < this.output_layer_num; j++) {
                this.weights_hidden_output[i][j] = Math.random() - 0.5; // Initialize weights between -0.5 and 0.5
                min_hiddenoutput = Math.min(min_hiddenoutput, this.weights_hidden_output[i][j]);
                max_hiddenoutput = Math.max(max_hiddenoutput, this.weights_hidden_output[i][j]);
            }
        }
//        System.out.println("[Info] W1 Input to Hidden range: [" + min_inputhidden + ", " + max_inputhidden + "]");
//        System.out.println("[Info] W2 Hidden to Output range: [" + min_hiddenoutput + ", " + max_hiddenoutput + "]");
    }
    private void forwardprop(double[] sample_x) {
        for (int i = 0; i < sample_x.length; i++) {
            this.input_layer[i] = sample_x[i];
        }
        for (int i = 0; i < this.hidden_layer_num; i++) {
            this.hidden_layer[i] = 0;
            for (int j = 0; j < this.input_layer_num + 1; j++) {
                this.hidden_layer[i] += this.weights_input_hidden[j][i] * this.input_layer[j];
            }
            this.hidden_layer[i] = ("binary".equals(dataset_type)) ? sigmoid(this.hidden_layer[i])
                    : customSigmoid(this.hidden_layer[i]);
        }
        for (int i = 0; i < this.output_layer_num; i++) {
            for (int j = 0; j < this.hidden_layer_num + 1; j++) {
                this.output_layer[i] += this.weights_hidden_output[j][i] * this.hidden_layer[j];
            }
            this.output_layer[i] = ("binary".equals(dataset_type)) ? sigmoid(this.output_layer[i])
                    : customSigmoid(this.output_layer[i]);
        }
    }


    private void backprop(double[] expected_output) {
        double[] delta_output = new double[this.output_layer_num];
        for (int i = 0; i < this.output_layer_num; i++) {
            double error = expected_output[i] - this.output_layer[i];
            delta_output[i] = ("binary".equals(dataset_type)) ? delta_sigmoid(this.output_layer[i])
                    * error : delta_customSigmoid(this.output_layer[i]) * error;
        }
        for (int i = 0; i < this.hidden_layer_num + 1; i++) {
            for (int j = 0; j < this.output_layer_num; j++) {
                this.delta_weights_hidden_output[i][j] = this.momentum * this.delta_weights_hidden_output[i][j]
                        + this.lr * delta_output[j] * this.hidden_layer[i];
                this.weights_hidden_output[i][j] += this.delta_weights_hidden_output[i][j];
            }
        }
        double[] delta_hidden = new double[this.hidden_layer_num];
        for (int i = 0; i < this.hidden_layer_num; i++) {
            for (int j = 0; j < this.output_layer_num; j++) {
                delta_hidden[i] += this.weights_hidden_output[i][j] * delta_output[j];
            }
            delta_hidden[i] *= ("binary".equals(dataset_type)) ? delta_sigmoid(this.hidden_layer[i]) :
                    0.5 * delta_customSigmoid(this.hidden_layer[i]);

            for (int j = 0; j < this.input_layer_num + 1; j++) {
                this.delta_weights_input_hidden[j][i] = this.momentum * this.delta_weights_input_hidden[j][i]
                        + this.lr * delta_hidden[i] * this.input_layer[j];
                this.weights_input_hidden[j][i] += this.delta_weights_input_hidden[j][i];
            }
        }
    }
    @Override
    public double[] outputFor(double[] inputs) {
        forwardprop(inputs);
        return this.output_layer; // Return the array of output values
    }



    @Override
    public double train(double[] sample_x, double[] expected_output) {
        forwardprop(sample_x);
        double total_error = 0.0;
        for (int i = 0; i < this.output_layer_num; i++) {
            double error = expected_output[i] - this.output_layer[i];
            total_error += Math.pow(error, 2);
            // Accumulate squared error
//            accumulatedSquaredError += Math.pow(error, 2);
        }
        backprop(expected_output);
//        trainingInstanceCount++;
//        System.out.println(trainingInstanceCount);
//        if (trainingInstanceCount % 100 == 0) { // For example, every 100 rounds
//            saveRMSError();
//        }

        return total_error / this.output_layer_num;
    }

//    public double calculateRMSerror() {
//        if (trainingInstanceCount == 0) return 0;
//        double rmsError = Math.sqrt(accumulatedSquaredError / trainingInstanceCount);
//        // Reset for next calculation
//        accumulatedSquaredError = 0.0;
//        trainingInstanceCount = 0;
//        return rmsError;
//    }
    @Override
    public double customSigmoid(double x) {
        double b = 1.0;
        double a = -1.0;
        return (b - a) / (1+Math.exp(-x)) + a; // scale the range from 0,1 to -1 , 1
    }
    private double delta_customSigmoid(double y){
        return 0.5 * (1.0 - Math.pow(y,2));
    }
    @Override
    public void zeroWeights() {
    }

    public double sigmoid(double x) {
        return (double)1 / (1 + Math.exp(-x));
    }

    private double delta_sigmoid(double y){
        return y * (1.0 - y);
    }
    @Override
    public void save(File file) {
        try (PrintWriter weights_saver = new PrintWriter(new FileWriter(file))) {
            // Save weights_input_hidden
            for (double[] row : weights_input_hidden) {
                for (double weight : row) {
                    weights_saver.print(weight);
                    weights_saver.print(","); // separate weights with commas
                }
                weights_saver.println();
            }
            weights_saver.println(); // separate w1 and w2 with an empty line
            // Save weights_hidden_output
            for (int i = 0; i < weights_hidden_output.length; i++) {
                for (int j = 0; j < weights_hidden_output[i].length; j++) {
                    weights_saver.print(weights_hidden_output[i][j]);
                    if (j < weights_hidden_output[i].length - 1) {
                        weights_saver.print(","); // separate weights with commas
                    }
                }
                weights_saver.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void load(String absoluteFilePath) throws IOException {
        try (BufferedReader weights_loader = new BufferedReader(new FileReader(absoluteFilePath))) {
            // Load weights_input_hidden
            for (int i = 0; i < weights_input_hidden.length; i++) {
                String[] weights = weights_loader.readLine().split(",");
                for (int j = 0; j < weights.length; j++) {
                    weights_input_hidden[i][j] = Double.parseDouble(weights[j]);
                }
            }
            weights_loader.readLine(); // Skip the empty line
            // Load weights_hidden_output
            for (int i = 0; i < weights_hidden_output.length; i++) {
                String[] weights = weights_loader.readLine().split(",");
                for (int j = 0; j < weights.length; j++) {
                    weights_hidden_output[i][j] = Double.parseDouble(weights[j]);
                }
            }
        }
    }
    public int get_random_action() {
        Random random = new Random();
        return random.nextInt(RobotState.Action.values().length); // [0, action_size)
    }

    public int calculate_best_action(RobotState state) {
        // Initialize maxQ to a very small number
        double maxQ = Double.NEGATIVE_INFINITY;
        int actionIndex = -1;
        // Iterate through all possible actions
        for (int i = 0; i < RobotState.Action.values().length; i++) {
            // Prepare the input for the NN
            double[] x = new double[] {
                    state.getScaled_myEnergy(),
                    state.getScaled_enemyEnergy(),
                    state.getScaled_distanceToEnemy(),
                    state.getScaled_distanceFromCentre(),
                    i
            };
            double[] output = outputFor(x);

            // Check if the output is the maximum so far
            if (output[0] > maxQ) {
                maxQ = output[0];
                actionIndex = i;
            }
        }

        return actionIndex;
    }

    public void train_online(double alpha, double gamma, double reward, MyAgent_NN.Policy policy,
                             RobotState previous_state, RobotState current_state,
                             ReplayMemory<Experience> replayMemory, int MINI_BATCH_SIZE) {
        // Add the new experience to the memory
        replayMemory.add(new Experience(previous_state, previous_state.get_my_action(), reward, current_state));
        // Train the network with mini-batches
        if (replayMemory.sizeOf() >= MINI_BATCH_SIZE) {
            Object[] experiences = replayMemory.randomSample(MINI_BATCH_SIZE);
            for (Object obj : experiences) {
                Experience exp = (Experience) obj;
                double[] x_previous = new double[] {
                        exp.previous_state.getScaled_myEnergy(),
                        exp.previous_state.getScaled_enemyEnergy(),
                        exp.previous_state.getScaled_distanceToEnemy(),
                        exp.previous_state.getScaled_distanceFromCentre(),
                        exp.previous_state.get_my_action().ordinal(),
                };
                double[] expected_q = computeQ(alpha, gamma, exp.current_reward, policy, exp.previous_state,
                        exp.current_state, exp.previous_action);
                this.train(x_previous, expected_q);
            }
        }
    }

    private double[] computeQ(double alpha, double gamma, double reward,
                              MyAgent_NN.Policy policy, RobotState previous_state,
                           RobotState current_state, RobotState.Action previous_action){
        RobotState.Action best_action = RobotState.Action.values()[calculate_best_action(current_state)];

        double [] previous_SA = new double[]{
                previous_state.getScaled_myEnergy(),
                previous_state.getScaled_enemyEnergy(),
                previous_state.getScaled_distanceToEnemy(),
                previous_state.getScaled_distanceFromCentre(),
                previous_action.ordinal(),
        };
        double [] current_SA;
        double previous_Q = this.outputFor(previous_SA)[0];

        double next_Q;
        if (policy == MyAgent_NN.Policy.onPolicy) {
            current_SA = new double[]{
                current_state.getScaled_myEnergy(),
                current_state.getScaled_enemyEnergy(),
                current_state.getScaled_distanceToEnemy(),
                current_state.getScaled_distanceFromCentre(),
                current_state.get_my_action().ordinal(),
            };
        } else {
            current_SA = new double[]{
                    current_state.getScaled_myEnergy(),
                    current_state.getScaled_enemyEnergy(),
                    current_state.getScaled_distanceToEnemy(),
                    current_state.getScaled_distanceFromCentre(),
                    best_action.ordinal()
            };
        }
        next_Q = outputFor(current_SA)[0];
        double[] Q = {0};
        this.TD_error = alpha * (reward + gamma * next_Q - previous_Q);
        Q[0] = previous_Q + this.TD_error;
        this.RMS_error += TD_error * TD_error;
        return Q;
    }
}
