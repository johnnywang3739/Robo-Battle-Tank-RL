import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// ref: https://github.com/sh0nk/matplotlib4j/tree/master
public class Plots {
    public static void main(String[] args) {
        String dataset_type = "binary"; // or "bipolar" this is the only line to change for bipolar or binary

        String fileName = dataset_type.equals("binary") ? "binary_XOR_error_data.csv" : "bipolar_XOR_error_data.csv";


        Scanner scanner = new Scanner(System.in);
        System.out.print("How many trials to plot? ");
        int num_of_trials = scanner.nextInt();

        List<double[]> y = new ArrayList<>();
        // load error data from the CSV file
        try (Scanner fileScanner = new Scanner(new File(fileName))) {
            int trial = 0;
            while (fileScanner.hasNextLine() && trial < num_of_trials) {
                String line = fileScanner.nextLine();
                String[] error_list = line.split(",");
                double[] errors = new double[error_list.length];
                for (int i = 0; i < error_list.length; i++) {
                    errors[i] = Double.parseDouble(error_list[i].trim()); // remove whitespaces
                }
                y.add(errors);
                trial++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Plot plot = Plot.create();
        for (int i = 0; i < y.size(); i++) {
            List<Double> epochs = new ArrayList<>();
            for (int j = 1; j <= y.get(i).length; j++) {
                epochs.add((double) j);
            }

            List<Double> errors = new ArrayList<>();
            for (double error : y.get(i)) {
                errors.add(error);
            }

            plot.plot().add(epochs, errors).label("Trial " + (i + 1));
        }

        String title_name = dataset_type.equals("binary") ? "Total Error Versus Epochs (Binary XOR dataset) with momentum of 0.9"
                : "Total Error Versus Epochs (Bipolar XOR dataset)";
        plot.title(title_name);
        plot.xlabel("Epochs");
        plot.ylabel("Total Error");
        plot.legend();
        try {
            plot.show();
        } catch (IOException | PythonExecutionException e) {
            e.printStackTrace();
        }
    }
}
