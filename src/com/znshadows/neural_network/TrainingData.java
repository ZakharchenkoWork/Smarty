package com.znshadows.neural_network;

public class TrainingData {
    double[] input = null;
    double[] output = null;

    public TrainingData(double[] input, double[] output) {
        this.input = input;
        this.output = output;
    }
    public TrainingData(double[] input, double output) {
        this.input = input;
        this.output = new double[]{output};
    }

    public double[] getInput() {
        return input;
    }

    public double[] getOutput() {
        return output;
    }
}
