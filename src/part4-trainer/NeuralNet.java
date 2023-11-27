import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NeuralNet implements NeuralNetInterface {
    private int input_layer_num;
    private int hidden_layer_num;
    private int output_layer_num;
    private double lr;
    private double momentum;
    private double[] input_layer, hidden_layer;
    private double[] output_layer; // Now an array for multiple outputs
    private double[][] weights_input_hidden;
    private double[][] weights_hidden_output; // Now a 2D array
    private double[][] delta_weights_input_hidden;
    private double[][] delta_weights_hidden_output; // Now a 2D array
    private double[] sample_error; // Now an array for multiple outputs
    private int epochs;
    private String dataset_type;

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

        // Initialize weights for input-hidden layer
        this.weights_input_hidden = new double[this.input_layer_num + 1][this.hidden_layer_num];
        // Initialize derivative of weights for input-hidden layer
        this.delta_weights_input_hidden = new double[this.input_layer_num + 1][this.hidden_layer_num];

        // Initialize weights for hidden-output layer
        this.weights_hidden_output = new double[this.hidden_layer_num + 1][this.output_layer_num];
        // Initialize derivative of weights for hidden-output layer
        this.delta_weights_hidden_output = new double[this.hidden_layer_num + 1][this.output_layer_num];

        // Print dimensions of weight matrices
//        System.out.println("[Info] W1 matrix size: [" + (this.input_layer_num + 1) + " x " + this.hidden_layer_num + "]");
//        System.out.println("[Info] W2 matrix size: [" + (this.hidden_layer_num + 1) + " x " + this.output_layer_num + "]");

        // Randomly initialize weights
        double min_inputhidden = Double.MAX_VALUE;
        double max_inputhidden = Double.MIN_VALUE;
        double min_hiddenoutput = Double.MAX_VALUE;
        double max_hiddenoutput = Double.MIN_VALUE;

        // Initialize input-hidden weights
        for (int i = 0; i < this.input_layer_num + 1; i++) {
            for (int j = 0; j < this.hidden_layer_num; j++) {
                this.weights_input_hidden[i][j] = Math.random() - 0.5; // Initialize weights between -0.5 and 0.5
                min_inputhidden = Math.min(min_inputhidden, this.weights_input_hidden[i][j]);
                max_inputhidden = Math.max(max_inputhidden, this.weights_input_hidden[i][j]);
            }
        }

        // Initialize hidden-output weights
        for (int i = 0; i < this.hidden_layer_num + 1; i++) {
            for (int j = 0; j < this.output_layer_num; j++) {
                this.weights_hidden_output[i][j] = Math.random() - 0.5; // Initialize weights between -0.5 and 0.5
                min_hiddenoutput = Math.min(min_hiddenoutput, this.weights_hidden_output[i][j]);
                max_hiddenoutput = Math.max(max_hiddenoutput, this.weights_hidden_output[i][j]);
            }
        }

        // Log range of weights
//        System.out.println("[Info] W1 Input to Hidden range: [" + min_inputhidden + ", " + max_inputhidden + "]");
//        System.out.println("[Info] W2 Hidden to Output range: [" + min_hiddenoutput + ", " + max_hiddenoutput + "]");
    }
    private void forwardprop(double[] sample_x) {
        // Initialize input layer
        for (int i = 0; i < sample_x.length; i++) {
            this.input_layer[i] = sample_x[i];
        }

        // From input layer to hidden layer
        for (int i = 0; i < this.hidden_layer_num; i++) {
            this.hidden_layer[i] = 0;
            for (int j = 0; j < this.input_layer_num + 1; j++) {
                this.hidden_layer[i] += this.weights_input_hidden[j][i] * this.input_layer[j];
            }
            // Apply activation function
            this.hidden_layer[i] = ("binary".equals(dataset_type)) ? sigmoid(this.hidden_layer[i]) : customSigmoid(this.hidden_layer[i]);
        }

        // From hidden layer to output layer
        for (int i = 0; i < this.output_layer_num; i++) {
            for (int j = 0; j < this.hidden_layer_num + 1; j++) {
                this.output_layer[i] += this.weights_hidden_output[j][i] * this.hidden_layer[j];
            }
            // Apply activation function
            this.output_layer[i] = ("binary".equals(dataset_type)) ? sigmoid(this.output_layer[i]) : customSigmoid(this.output_layer[i]);
        }
    }


    private void backprop(double[] expected_output) {
        double[] delta_output = new double[this.output_layer_num];
        // Calculate delta for each output neuron
        for (int i = 0; i < this.output_layer_num; i++) {
            double error = expected_output[i] - this.output_layer[i];
            delta_output[i] = ("binary".equals(dataset_type)) ? delta_sigmoid(this.output_layer[i]) * error : delta_customSigmoid(this.output_layer[i]) * error;
        }

        // Update weights from hidden to output layer
        for (int i = 0; i < this.hidden_layer_num + 1; i++) {
            for (int j = 0; j < this.output_layer_num; j++) {
                this.delta_weights_hidden_output[i][j] = this.momentum * this.delta_weights_hidden_output[i][j] + this.lr * delta_output[j] * this.hidden_layer[i];
                this.weights_hidden_output[i][j] += this.delta_weights_hidden_output[i][j];
            }
        }

        // Calculate delta for hidden layer and update weights
        double[] delta_hidden = new double[this.hidden_layer_num];
        for (int i = 0; i < this.hidden_layer_num; i++) {
            for (int j = 0; j < this.output_layer_num; j++) {
                delta_hidden[i] += this.weights_hidden_output[i][j] * delta_output[j];
            }
            delta_hidden[i] *= ("binary".equals(dataset_type)) ? delta_sigmoid(this.hidden_layer[i]) : 0.5 * delta_customSigmoid(this.hidden_layer[i]);

            for (int j = 0; j < this.input_layer_num + 1; j++) {
                this.delta_weights_input_hidden[j][i] = this.momentum * this.delta_weights_input_hidden[j][i] + this.lr * delta_hidden[i] * this.input_layer[j];
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
            this.sample_error[i] = expected_output[i] - this.output_layer[i];
            total_error += Math.pow(this.sample_error[i], 2);
        }
        backprop(expected_output);
        return total_error / this.output_layer_num; // Return average error
    }


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
    public void load(String fileName) throws IOException {
        try (BufferedReader weights_loader = new BufferedReader(new FileReader(fileName))) {
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
}
