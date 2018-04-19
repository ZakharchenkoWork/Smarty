package com.znshadows.neural_network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Neuron {

    private static double learningRate = 1;

    private ArrayList<String> backConnections = new ArrayList<>();
    private HashMap<String, Double> weights = new HashMap<>();


    private transient Double lastSignal = 0.0;
    private String neuronId;
    private LAYER layer;

    Neuron() {

    }


    Neuron(LAYER layer, ArrayList<Neuron> backConnections) {
        this.layer = layer;
        neuronId = UUID.randomUUID().toString();
        if(backConnections != null) {

            this.backConnections = new ArrayList<>();
            for (Neuron backConnection : backConnections) {
                this.backConnections.add(backConnection.neuronId);
                weights.put(backConnection.neuronId, (Math.random()*2) - 1);
            }
        }



    }

    public static double getLearningRate() {
        return learningRate;
    }

    public String getNeuronId() {
        return neuronId;
    }

    public ArrayList<String> getBackConnections() {
        return backConnections;
    }


    public void setBackConnections(ArrayList<String> backConnections) {
        this.backConnections = backConnections;
    }

    double getWeight(Neuron neuron) {
        return weights.get(neuron.getNeuronId());
    }
    double getWeight(String neuronId) {
        return weights.get(neuronId);
    }

    public String getWeightsString() {
        String result = "";
        if(backConnections.size() > 0) {
            for (String backConnection : backConnections) {
                result += String.format("%.2f", getWeight(backConnection)) + ",";
            }
        } else {
            result = "N/A";
        }
        return result;
    }


    double process(Smarty smarty) {
        if (layer != LAYER.INPUT) {

            double summ = 0.0;
            for (String backConnection : backConnections) {
                double backSignal =  smarty.getNeuronById(backConnection).getLastSignal();
                double weight = getWeight(backConnection);
                summ += weight * backSignal;
            }

            return lastSignal = sigmoid(summ);
        } else {
            return lastSignal;
        }
    }

    void correction(Smarty smarty, double expected) {
        if(layer != LAYER.INPUT) {
            if (backConnections.size() != 0) {
                double error;
                if(layer == LAYER.OUTPUT) {
                    error = getLastSignal() - expected;
                } else {
                    error = expected;
                }
                double weightsDelta = error * (getLastSignal() * (1.0 - getLastSignal()));
                for (String neuronId : backConnections) {
                    Neuron backConnection = smarty.getNeuronById(neuronId);
                    double newWeight = getWeight(backConnection) - (backConnection.getLastSignal() * weightsDelta * learningRate);
                    weights.put(backConnection.getNeuronId(), newWeight);
                    backConnection.correction(smarty, newWeight * weightsDelta);
                }
            }
        }
    }

    void setBaseSignal(double baseSignal) {
        this.lastSignal = baseSignal;
    }

    public static void recalculateLearnringRate(){
        learningRate = learningRate/ (1+(learningRate*2));
    }
    public void clean() {
        lastSignal = 0.0;
    }

    public double getLastSignal() {
        return lastSignal;
    }

    private double sigmoid(double input) {return (1.0 / (1.0 + Math.exp(-input)));}

    public HashMap<String, Double> getWeights() {
        return weights;
    }

    enum LAYER{
        INPUT,
        HIDEN,
        OUTPUT
    }
}
