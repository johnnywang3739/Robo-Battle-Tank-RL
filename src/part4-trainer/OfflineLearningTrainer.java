import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class OfflineLearningTrainer {
    private static double minQ = Double.MAX_VALUE;
    private static double maxQ = Double.MIN_VALUE;

    public static void main(String[] args) throws IOException {
        String lutFilename = "LUT_end_of_battle.txt";
        int maxTrainSet = 4 * 4 * 3 * 3 * 5; // Assuming this is the size of your LUT
        double[][] trainInput = new double[maxTrainSet][5];
        double[] trainOutput = new double[maxTrainSet];

        int numTrainSet = parseLUTData(lutFilename, trainInput, trainOutput);
        scaleTrainingData(trainOutput);

//        double[] learningRates = { 0.001, 0.003, 0.005, 0.007, 0.01, 0.015, 0.02};
//        double[] momentums = { 0.9, 0.8, 0.85, 0.0};
//        int[] hiddenNeurons = {10, 8, 12, 16};

        double[] learningRates = { 0.007};
        double[] momentums = { 0.85};
        int[] hiddenNeurons = {12};

        double optimalLR = 0.0, optimalMomentum = 0.0;
        int optimalHidden = 0, minEpoch = Integer.MAX_VALUE;
        NeuralNet optimalNN = null;

        for (double lr : learningRates) {
            for (double momentum : momentums) {
                for (int hidden : hiddenNeurons) {
                    NeuralNet nn = new NeuralNet(5, hidden, 1, lr, momentum, "bipolar");
                    int total_epoch = trainNetwork(nn, trainInput, trainOutput, numTrainSet);
                    if (total_epoch < minEpoch) {
                        minEpoch = total_epoch;
                        optimalLR = lr;
                        optimalMomentum = momentum;
                        optimalHidden = hidden;
                        optimalNN = nn;
                    }
                }
            }
        }

        System.out.println("Optimal parameters:");
        System.out.println("Learning Rate: " + optimalLR);
        System.out.println("Momentum: " + optimalMomentum);
        System.out.println("Hidden Neurons: " + optimalHidden);
        System.out.println("Minimum Epochs: " + minEpoch);

        if (optimalNN != null) {
            // Save the model with the best parameters
            saveModel(optimalNN, "LUT_model.txt");
            System.out.println("Optimal model saved.");
        }
    }

    private static int parseLUTData(String filename, double[][] inputs, double[] outputs) throws IOException {
        int idx = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null && idx < inputs.length) {
                String[] values = line.split(",");
                for (int i = 0; i < 5; i++) {
                    inputs[idx][i] = Double.parseDouble(values[i]);
                }
                outputs[idx] = Double.parseDouble(values[5]);
                minQ = Math.min(minQ, outputs[idx]);
                maxQ = Math.max(maxQ, outputs[idx]);
                idx++;
            }
        }
        return idx;
    }

    private static void scaleTrainingData(double[] outputs) {
        for (int i = 0; i < outputs.length; i++) {
            outputs[i] = scale(outputs[i], minQ, maxQ, -1.0, 1.0);
        }
    }

    private static double scale(double value, double min, double max, double newMin, double newMax) {
        return (newMax - newMin) * (value - min) / (max - min) + newMin;
    }

    private static int trainNetwork(NeuralNet nn, double[][] trainInput, double[] trainOutput, int numTrainSet) {
        double totalError, rmsError;
        int epoch = 0;
        do {
            totalError = 0.0;
            for (int i = 0; i < numTrainSet; i++) {
                double error = nn.train(trainInput[i], new double[]{trainOutput[i]});
                totalError += Math.pow(error, 2); // Accumulate squared error
            }
            rmsError = Math.sqrt(totalError / numTrainSet); // Compute RMS error
            System.out.println(rmsError);
            epoch++;
        } while (rmsError > 0.05); // Continue until RMS error is below the threshold

        return epoch;
    }

    private static void saveModel(NeuralNet nn, String filename) throws IOException {
        nn.save(new File(filename));
    }
}
