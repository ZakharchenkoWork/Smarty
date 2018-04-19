package com.znshadows.neural_network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class Smarty {
    private ArrayList<ArrayList<Neuron>> brain = new ArrayList<>();
    private transient WeightsListener weightsListener = (w) -> {
    };
    private transient SignalListener signalListener = (s) -> {
    };

    private transient List<TrainingData> trainingData;
    private transient int epochs = 0;
    private transient int learnTillPercentage = 101;
    private boolean showGUI = false;
    private GUI gui = new GUI();
    private long timeBetweenIterations = 0;


    public Smarty setTimeBetweenIterations(long timeBetweenIterations) {
        this.timeBetweenIterations = timeBetweenIterations;
        return this;
    }

    private Smarty showGUI() {
        showGUI = true;
        return this;
    }

    /**
     * Construction of neural_network network. <br>
     * Usage: 3,5,5,2 will create 4 layers with 15 neurons.
     **/
    private Smarty(int... layers) {

        for (int layersIndex = 0; layersIndex < layers.length; layersIndex++) {
            brain.add(new ArrayList<>());

            for (int neuronsIndex = 0; neuronsIndex < layers[layersIndex]; neuronsIndex++) {
                if (layersIndex == 0) {
                    brain.get(layersIndex).add(new Neuron(Neuron.LAYER.INPUT, null));
                } else  if (layersIndex != layers.length-1) {
                    brain.get(layersIndex).add(new Neuron(Neuron.LAYER.HIDEN, brain.get(layersIndex - 1)));
                }else {
                    brain.get(layersIndex).add(new Neuron(Neuron.LAYER.OUTPUT, brain.get(layersIndex - 1)));
                }
            }
        }

    }

    public Smarty(String json) {
        brain = new Gson().fromJson(json, new TypeToken<ArrayList<ArrayList<Neuron>>>() {
        }.getType());

    }

    Neuron getNeuronById(String id) {
        for (int layersIndex = 0; layersIndex < brain.size(); layersIndex++) {
            for (int neuronsIndex = 0; neuronsIndex < brain.get(layersIndex).size(); neuronsIndex++) {
                if (brain.get(layersIndex).get(neuronsIndex).getNeuronId().equals(id)) {
                    return brain.get(layersIndex).get(neuronsIndex);
                }

            }
        }
        return null;
    }

    public String getAsJson() {
        return new Gson().toJson(brain);
    }

    private String getNeuralRepresentation() {
        String neuralRepresentation = "Weights\n";

        for (int layersIndex = 0; layersIndex < brain.size(); layersIndex++) {
            if (layersIndex == 0) {
                neuralRepresentation += generateBorder();
            }
            neuralRepresentation += "|| ";


            for (int neuronsIndex = 0; neuronsIndex < brain.get(layersIndex).size(); neuronsIndex++) {
                neuralRepresentation += brain.get(layersIndex).get(neuronsIndex).getWeightsString() + " ";
            }
            neuralRepresentation += "||\n";
        }
        neuralRepresentation += generateBorder();
        return neuralRepresentation;
    }

    private String getSignalsRepresentation() {
        String neuralRepresentation = "Signals\n";

        for (int layersIndex = 0; layersIndex < brain.size(); layersIndex++) {
            if (layersIndex == 0) {
                neuralRepresentation += generateBorder();
            }
            neuralRepresentation += "|| ";


            for (int neuronsIndex = 0; neuronsIndex < brain.get(layersIndex).size(); neuronsIndex++) {
                neuralRepresentation += String.format("%.2f", brain.get(layersIndex).get(neuronsIndex).getLastSignal()) + " ";
            }
            neuralRepresentation += "||\n";
        }
        neuralRepresentation += generateBorder();
        return neuralRepresentation;
    }

    private String generateBorder() {
        String result = "====";
        for (int i = 0; i < brain.size(); i++) {
            result += "==";
        }
        return result + "\n";
    }

    public Smarty setBaseData(double[] baseData) {
        if (baseData.length == brain.get(0).size()) {
            for (int neuronsIndex = 0; neuronsIndex < brain.get(0).size(); neuronsIndex++) {
                brain.get(0).get(neuronsIndex).setBaseSignal(baseData[neuronsIndex]);
            }
        } else {
            System.err.println("wrong base data length");
        }
        return this;
    }

    private double[] processData() {
        for (int layersIndex = 0; layersIndex < brain.size(); layersIndex++) {
            for (int neuronsIndex = 0; neuronsIndex < brain.get(layersIndex).size(); neuronsIndex++) {
                brain.get(layersIndex).get(neuronsIndex).process(this);
            }
        }
        ArrayList<Neuron> lastLayer = brain.get(brain.size() - 1);
        double[] result = new double[lastLayer.size()];
        for (int i = 0; i < lastLayer.size(); i++) {
            result[i] = lastLayer.get(i).getLastSignal();
        }
        return result;
    }

    public double[] process() {
        double[] result = new double[]{0.0};
        if (trainingData == null) {
            weightsListener.onWeightsCalculated(getNeuralRepresentation());
             result =  processData();
            signalListener.onSignalsCalculated(getSignalsRepresentation());
            if (showGUI) {
                gui.onUpdate(brain);
            }

        } else {

            weightsListener.onWeightsCalculated("Before education\n" + getNeuralRepresentation());

            if (trainingData != null) {
                if(epochs != 0) {
                    for (int learningIteration = 0; learningIteration < epochs; learningIteration++) {

                        train();
                        if (learningIteration % (epochs / 100) == 0) {
                            Neuron.recalculateLearnringRate();
                            checkProgress(learningIteration);
                        }
                    }

                }
                if(learnTillPercentage > 0){
                    while (learnTillPercentage > checkProgress(1)){
                        Neuron.recalculateLearnringRate();
                        train();
                    }
                }
                weightsListener.onWeightsCalculated("After education\n" + getNeuralRepresentation());
            }
        }

        cleanup();
        return result;
    }

    private double checkProgress(int learningIteration) {
        double percentage = 0;
        for (int i = 0; i < trainingData.size(); i++) {
            setBaseData(trainingData.get(i).getInput());
            double[] nextResult =  processData();
            if(compare(nextResult, trainingData.get(i).output)){
                percentage += 100.0/(double)( trainingData.size());
            }
        }

        int completion = epochs != 0 ?learningIteration/(epochs/100) : 0;
        if(showGUI) {
            gui.showProgress(completion, (int)percentage, Neuron.getLearningRate());
        } else {
            System.out.println("Completed on: " + completion + "%");
            System.out.println("Learning percentage is: " + percentage + "%");
        }
        return percentage;
    }

    private void train() {
        int resultDataIndex = (int) (Math.random() * trainingData.size());
        setBaseData(trainingData.get(resultDataIndex).getInput());
        processData();
        for (int neuronsIndex = 0; neuronsIndex < brain.get(brain.size() - 1).size(); neuronsIndex++) {
            brain.get(brain.size() - 1).get(neuronsIndex).correction(this, trainingData.get(resultDataIndex).getOutput()[neuronsIndex]);
        }
        if (showGUI) {
            gui.onUpdate(brain);
        }
        try {
            Thread.sleep(timeBetweenIterations);
        } catch (InterruptedException ie) {
        }
    }

    boolean compare(double[] first, double[] second){

    for (int i = 0; i < first.length; i++) {
        if(first[i] > 0.5 && second[i] >=0.5 || first[i] <= 0.5 && second[i] <=0.5){

        } else {
            return false;
        }
    }
    return true;
}

    private void cleanup() {
        for (int layersIndex = 0; layersIndex < brain.size(); layersIndex++) {
            for (int neuronsIndex = 0; neuronsIndex < brain.get(layersIndex).size(); neuronsIndex++) {
                brain.get(layersIndex).get(neuronsIndex).clean();
            }
        }
        trainingData = null;
        epochs = 0;
    }

    public Smarty educateForResult(List<TrainingData> trainingData, int correctAnswerPersentage) {
        if (trainingData.get(0).getInput().length == brain.get(0).size() && trainingData.get(0).getOutput().length == brain.get(brain.size() - 1).size()) {

            this.trainingData = trainingData;
        } else {
            System.err.println("wrong education data length");
        }
        if (correctAnswerPersentage < 0) {
             learnTillPercentage = 0;
        } else if (correctAnswerPersentage > 100){
            learnTillPercentage = 100;
        } else {
            learnTillPercentage = correctAnswerPersentage;
        }
        return this;
    }

    private Smarty educateForEpochs(List<TrainingData> trainingData, int epochs) {
        this.epochs = epochs;
        if (trainingData.get(0).getInput().length == brain.get(0).size() && trainingData.get(0).getOutput().length == brain.get(brain.size() - 1).size()) {

            this.trainingData = trainingData;
        } else {
            System.err.println("wrong education data length");
        }
        return this;
    }

    public Smarty setWeightsListener(WeightsListener weightsListener) {
        if (weightsListener != null) {
            this.weightsListener = weightsListener;
        } else {
            this.weightsListener = (w) -> {
            };
        }
        return this;
    }

    public Smarty setSignalListener(SignalListener signalListener) {
        if (signalListener != null) {
            this.signalListener = signalListener;
        } else {
            this.signalListener = (s) -> {
            };
        }
        return this;
    }

    public static final class Builder {
        private Smarty instance;

        public Builder(int... layers) {
            instance = new Smarty(layers);
        }

        public Builder(String json) {
            instance = new Smarty(json);
        }

        public Builder setSignalListener(SignalListener signalListener) {
            instance.setSignalListener(signalListener);
            return this;
        }

        public Builder setWeightsListener(WeightsListener weightsListener) {
            instance.setWeightsListener(weightsListener);
            return this;
        }

        public Builder setBaseData(double[] baseData) {
            this.instance.setBaseData(baseData);
            return this;
        }

        public Builder educateForResult(List<TrainingData> trainingData, int epochs) {
            this.instance.educateForResult(trainingData, epochs);
            return this;
        }

        public Builder setTimeBetweenIterations(long time) {
            instance.setTimeBetweenIterations(time);
            return this;
        }

        public Builder showGUI() {
            instance.showGUI();
            return this;
        }

        boolean guiStarted = false;

        public Smarty build() {
            if (instance.showGUI) {

                new Thread(() -> {
                    GUI.setOnStartListener((gui) -> {
                        instance.gui = gui;
                        gui.setUp(instance.brain);
                        guiStarted = true;

                    });
                    GUI.launchUp();
                }).start();
                while (!guiStarted) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            return instance;
        }

        public Builder educateForEpochs(List<TrainingData> trainingData, int epochs) {
            instance.educateForEpochs(trainingData, epochs);
            return this;
        }
    }

    

}
