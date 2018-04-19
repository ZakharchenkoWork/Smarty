import com.znshadows.neural_network.Smarty;
import com.znshadows.neural_network.TrainingData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Testing {
    public static void main(String[] args) {
        double learningRate = 1;


        //*
        List<TrainingData> trainingData = new ArrayList<>();
        trainingData.add(new TrainingData(new double[]{0, 0, 0}, new double[]{0}));
        trainingData.add(new TrainingData(new double[]{0, 0, 1}, new double[]{1}));
        trainingData.add(new TrainingData(new double[]{0, 1, 0}, new double[]{0}));
        trainingData.add(new TrainingData(new double[]{0, 1, 1}, new double[]{0}));
        trainingData.add(new TrainingData(new double[]{1, 0, 0}, new double[]{1}));
        trainingData.add(new TrainingData(new double[]{1, 0, 1}, new double[]{1}));
        trainingData.add(new TrainingData(new double[]{1, 1, 0}, new double[]{0}));
        trainingData.add(new TrainingData(new double[]{1, 1, 1}, new double[]{1}));

        Smarty neural = new Smarty.Builder(3, 2, 1)
                .setWeightsListener(weights -> System.out.println(weights))
                .setSignalListener(signals -> System.out.println(signals))
                .educateForResult(trainingData, 100)
                // .educateForEpochs(trainingData, 5000)
                .showGUI()
                .setTimeBetweenIterations(10)
                .build();
        neural.process();
        neural.setWeightsListener(null);

        System.out.println(neural.getAsJson());
        //*/

    }
}
