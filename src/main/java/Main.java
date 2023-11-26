import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
public class Main {
    public static void main(String[] args) throws IOException {
        int trials = 10000;
        int total_epoch = 0;
        String dataset_type = "bipolar"; // "binary" or "bipolar"
        double momentum = 0.9;
        String fileName = dataset_type.equals("binary") ? "binary_XOR_error_data.csv" : "bipolar_XOR_error_data.csv";
        File file = new File(fileName);
        if (file.exists() && !file.delete()) {
            System.out.println("[Info] Failed to delete file.");
            return;
        }

        FileWriter error_file = new FileWriter(fileName, false);
        List<Integer> epoch_list = new ArrayList<>();

        for (int trial = 0; trial < trials; trial++) {
            System.out.println("====================================================================================");
            System.out.println("[INFO] Trial " + (trial + 1));
            List<Double> trail_error_list = new ArrayList<>();
            double[][] train_x;
            double[][] train_y; // Changed to 2D array for compatibility with multiple outputs
            if ("binary".equals(dataset_type)) {
                train_x = new double[][]{{0.0, 0.0}, {0.0, 1.0}, {1.0, 0.0}, {1.0, 1.0}};
                train_y = new double[][]{{0.0}, {1.0}, {1.0}, {0.0}}; // Each target is now an array
            } else {
                train_x = new double[][]{{-1.0, -1.0}, {-1.0, 1.0}, {1.0, -1.0}, {1.0, 1.0}};
                train_y = new double[][]{{-1.0}, {1.0}, {1.0}, {-1.0}}; // Each target is now an array
            }

            NeuralNet xor_mlp = new NeuralNet(2, 4, 1, 0.2, momentum, dataset_type); // Output layer size set to 1 for XOR
            double total_error;
            int epochs = 0;
            System.out.println("[INFO] Training...");
            do {
                total_error = 0;
                for (int sample_index = 0; sample_index < train_x.length; sample_index++) {
                    double error = xor_mlp.train(train_x[sample_index], train_y[sample_index]);
                    total_error += error;
                }
                epochs++;
                trail_error_list.add(total_error);
            } while (total_error > 0.05);

            total_epoch += epochs;
            System.out.println("[INFO] Predicting:");
            for (int i = 0; i < train_x.length; i++) {
                double[] prediction = xor_mlp.outputFor(train_x[i]);
                System.out.println("input: " + Arrays.toString(train_x[i]) + " predicted: " + Arrays.toString(prediction) +
                        ", ground truth: " + Arrays.toString(train_y[i]));
            }

            error_file.write(trail_error_list.toString().replace("[", "").replace("]", "").replace(", ", ",") + System.lineSeparator());
            epoch_list.add(epochs);
            System.out.println("====================================================================================");
        }
        error_file.close();
        int min = 9;         // calculate statistics
        int max = 0;
        int sum = 0;
        double sumOfSquares = 0.0;
        for (int epochs : epoch_list) {
            if (epochs < min) {
                min = epochs;
            }
            if (epochs > max) {
                max = epochs;
            }
            sum += epochs;
            sumOfSquares += Math.pow(epochs, 2);
        }
        double mean = (double) sum / epoch_list.size(); // calculate mean and standard deviation
        double variance = sumOfSquares / epoch_list.size() - Math.pow(mean, 2);
        double std = Math.sqrt(variance);

        System.out.println("Statistics over " + trials + " Trials:");
        System.out.println("Min: " + min);
        System.out.println("Max: " + max);
        System.out.println("Mean Epochs: " + mean);
        System.out.println("Standard Deviation of Epochs: " + std);
        System.out.println("Mean +- std: " + (mean - std) + " to " + (mean + std));
        System.out.println("[INFO] Average epochs to converge over " + trials + " trials: "
                + total_epoch / (double) trials);
    }
}
