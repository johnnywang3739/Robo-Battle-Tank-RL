import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
public class NeuralNet implements NeuralNetInterface {
    private int input_layer_num = 2;
    private int hidden_layer_num = 4;
    private int output_layer_num = 1;
    private double lr = 0.2;
    private double momentum = 0.0;
    private double[] input_layer, hidden_layer;
    private double output_layer; // a single output
    private double[][] weights_input_hidden;
    private double[] weights_hidden_output;
    private double[][] delta_weights_input_hidden;
    private double[] delta_weights_hidden_output;
    private double sample_error = 0.0;
    private int epochs = 0;
    private String dataset_type;

    public NeuralNet(int argNumInputs, int argNumHidden,
                     double argLearningRate, double argMomentumTerm, String dataset_type) {
        this.input_layer_num = argNumInputs;
        this.hidden_layer_num = argNumHidden; // to include bias term
        this.lr = argLearningRate;
        this.momentum = argMomentumTerm;
        this.dataset_type = dataset_type;
        this.input_layer = new double[this.input_layer_num+1]; // in A1, it should be 2 + 1, + 1 for bias term,
        this.input_layer[this.input_layer_num] = 1; // set the bias term to 1
        this.hidden_layer = new double[this.hidden_layer_num+1]; // in A1, it should be 4 + 1, + 1 for bias term
        this.hidden_layer[this.hidden_layer_num] = 1; // set the bias term to 1
        this.output_layer = 0.0;  // a single output...
        initializeWeights();

    }

    @Override
    public void initializeWeights() {
        System.out.println("[INFO] Initializing Weights...");

        this.weights_input_hidden = new double[this.input_layer_num+1][this.hidden_layer_num];
        // 3 X 4 matrix     last row is b1
        this.weights_hidden_output = new double[this.hidden_layer_num+1]; // 5 X 1 matrix     last row is b2
        this.delta_weights_input_hidden = new double[this.input_layer_num+1][this.hidden_layer_num];
        // derivative of the weights;
        this.delta_weights_hidden_output = new double[this.hidden_layer_num+1]; // derivative of the weights;
        // Print dimensions of weight matrices
        System.out.println("[Info] W1 matrix size: ["
                + (this.input_layer_num+1) + " x " + this.hidden_layer_num + "]");
        System.out.println("[Info] W2 matrix size: ["
                + (this.hidden_layer_num+1) + " x " + this.output_layer_num + "]");

        double min_inputhidden = Double.MAX_VALUE;
        double max_inputhidden = Double.MIN_VALUE;

        for(int i = 0; i < this.input_layer_num + 1; i++) {
            for(int j = 0; j < this.hidden_layer_num; j++) {
                weights_input_hidden[i][j] = Math.random() - 0.5;
                // initalize the weights to be from - 0.5 to 0.5 for binary dataset
                if (weights_input_hidden[i][j] < min_inputhidden)
                    min_inputhidden = weights_input_hidden[i][j];
                if (weights_input_hidden[i][j] > max_inputhidden)
                    max_inputhidden = weights_input_hidden[i][j];
            }
        }
        double min_hiddenoutput = Double.MAX_VALUE;
        double max_hiddenoutput = Double.MIN_VALUE;

        for(int i = 0; i < this.hidden_layer_num + 1; i++) {
            weights_hidden_output[i] = Math.random() - 0.5;
            // initalize the weights to be from - 0.5 to 0.5 for binary dataset
            if (weights_hidden_output[i] < min_hiddenoutput)
                min_hiddenoutput = weights_hidden_output[i];
            if (weights_hidden_output[i] > max_hiddenoutput)
                max_hiddenoutput = weights_hidden_output[i];
            }
        System.out.println("[Info] W1 Input to Hidden range: [" + min_inputhidden + ", " + max_inputhidden + "]");
        System.out.println("[Info] W2 Hidden to Output range: [" + min_hiddenoutput + ", " + max_hiddenoutput + "]");
    }

    private void forwardprop(double[] sample_x) {
        // initialize input layer
        for(int i = 0; i < sample_x.length; i++) {
            this.input_layer[i] = sample_x[i];
            // set input layer to x1, x2, leave the bias term as already set as 1 in initialization.
        }

        // from input layer to hidden layer
        for (int i = 0; i < this.hidden_layer_num; i++) {                // Xin (1*2 + 1) * W1 (3*4) multiplication:
            // Reset the hidden layer values to zero before accumulation
            this.hidden_layer[i] = 0;
            for (int j = 0; j < this.input_layer_num + 1; j++) {
                this.hidden_layer[i] += this.weights_input_hidden[j][i] * this.input_layer[j];
            }
            // apply activation function
            if ("binary".equals(dataset_type)){
                this.hidden_layer[i] = sigmoid(this.hidden_layer[i]);
            }else if("bipolar".equals(dataset_type)){
                this.hidden_layer[i] = customSigmoid(this.hidden_layer[i]);
            }

        }

        //       from hidden layer to output layer
            // Xhidden (1*4 + 1 ) * W1 (5* 1) multiplication:
        for(int i = 0; i < this.hidden_layer_num + 1; i++) {
            this.output_layer += this.weights_hidden_output[i] * this.hidden_layer[i];
        }

        if ("binary".equals(dataset_type)){
            this.output_layer = sigmoid(this.output_layer);
        }else if("bipolar".equals(dataset_type)){
            this.output_layer = customSigmoid(this.output_layer);
        }
        //

    }

    private void backprop() {
        double delta_output = 0.0;


        if ("binary".equals(dataset_type)){
            delta_output = delta_sigmoid(this.output_layer) * this.sample_error;
        }else if("bipolar".equals(dataset_type)){
            delta_output = delta_customSigmoid(this.output_layer) * this.sample_error;
        }


        // update hidden to output weights
        for(int i=0; i < this.hidden_layer_num+ 1; i++){
            this.delta_weights_hidden_output[i] = this.momentum*this.delta_weights_hidden_output[i]
                    + this.lr * delta_output * this.hidden_layer[i];
            // [5 X 1] matrix
            weights_hidden_output[i] += this.delta_weights_hidden_output[i];
        }


        double[] delta_hidden = new double[this.hidden_layer_num];
        for(int i=0; i < this.hidden_layer_num; i++){

            delta_hidden[i] += this.weights_hidden_output[i] * delta_output;            // [4 * 1 + 1 bias]


            if ("binary".equals(dataset_type)){
                delta_hidden[i] = delta_hidden[i]* delta_sigmoid(hidden_layer[i]);
            }else if("bipolar".equals(dataset_type)){
                delta_hidden[i] = delta_hidden[i]* 0.5* delta_customSigmoid(hidden_layer[i]);
            }

        }
        // update input to hidden weights
        for(int i=0; i < this.hidden_layer_num; i++){
            for(int j = 0; j < this.input_layer_num + 1; j++){
                this.delta_weights_input_hidden[j][i] = this.momentum * this.delta_weights_input_hidden[j][i]
                        + this.lr * delta_hidden[i] * this.input_layer[j];
                // [(2+1) X 4] matrix
                this.weights_input_hidden[j][i] += this.delta_weights_input_hidden[j][i];
            }
        }
    }
    public double outputFor(double[] inputs) {
        forwardprop(inputs);
        return (this.output_layer >= 0.5) ? 1.0 : 0.0;
    }
    @Override
    public double train(double[] sample_x, double sample_y) {
        forwardprop(sample_x);
        this.sample_error = sample_y - this.output_layer;
        backprop();
        return this.sample_error;
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
            weights_saver.println(); // separate w1 w2 with an empty line
            // Save weights_hidden_output
            for (double weight : weights_hidden_output) {
                weights_saver.println(weight);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load(String fileName) throws IOException {
        //https://docs.oracle.com/javase/8/docs/api/java/io/BufferedReader.html
        try (BufferedReader weights_loader = new BufferedReader(new FileReader(fileName))) {
            // Load weights_input_hidden
            for (int i = 0; i < weights_input_hidden.length; i++) {
                String[] weights = weights_loader.readLine().split(",");
                for (int j = 0; j < weights.length; j++) {
                    weights_input_hidden[i][j] = Double.parseDouble(weights[j]);
                }
            }
            weights_loader.readLine();
            String line;
            int index = 0;
            while ((line = weights_loader.readLine()) != null && index < weights_hidden_output.length) {
                weights_hidden_output[index] = Double.parseDouble(line);
                index++;
            }
        }
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
}
