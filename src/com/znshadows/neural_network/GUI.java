package com.znshadows.neural_network;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;

public class GUI extends Application {
    Stage primaryStage;
    HashMap<String, Button> signals = new HashMap<>();
    HashMap<String, HashMap<String, WeightView>> weights = new HashMap<>();
    Pane root;
    Text completionText;
    Text percentageText;
    Text learningRateText;
    static Thread currentThread;
    private static OnStartListener onStartListener;
    int width = 1920;
    int height = 1080;

    static void setOnStartListener(OnStartListener onStartListener) {
        GUI.onStartListener = onStartListener;
    }

    public static void launchUp() {
        launch(null);
        currentThread = Thread.currentThread();

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Smarty");
        MenuBar menuBar = new MenuBar();
        Menu menuStart = new Menu("Start");
        menuBar.getMenus().add(menuStart);
        root = new Pane();
        root.getChildren().add(menuBar);
        Scene scene = new Scene(new ScrollPane(root), width, height);
        primaryStage.setScene(scene);
        primaryStage.show();


        completionText = new Text(0, 50, "Comletion");
        percentageText = new Text(0, 70, "Learning persentage");
        learningRateText = new Text(100, 50, "Learning Rate");
        root.getChildren().add(completionText);
        root.getChildren().add(percentageText);
        root.getChildren().add(learningRateText);
        onStartListener.onStarted(this);

    }

    void onUpdate(ArrayList<ArrayList<Neuron>> brain) {
        Platform.runLater(()-> {
            double[] minmax = getMinMax(brain);
        for(int layersIndex = 0; layersIndex<brain.size();layersIndex++) {
                for (int neuronsIndex = 0; neuronsIndex < brain.get(layersIndex).size(); neuronsIndex++) {
                    Neuron neuron = brain.get(layersIndex).get(neuronsIndex);
                    Button signal = signals.get(neuron.getNeuronId());
                    signal.setText(String.format("%.1f", neuron.getLastSignal()));
                    String tooltipText = "";
                    HashMap<String, WeightView> weightViews = weights.get(neuron.getNeuronId());
                    for (String key : weightViews.keySet()) {
                        weightViews.get(key).showData(minmax[0], minmax[1],neuron.getWeight(key));
                        tooltipText += neuron.getWeight(key) +"\n";
                    }
                    String fTooltipText = tooltipText;
                    signal.setOnAction((event)-> showTooltip(primaryStage, signal, fTooltipText));

                }

            }
        });
    }
    private double[] getMinMax(ArrayList<ArrayList<Neuron>> brain){
        double[] minmax = new double[2];
        for(int layersIndex = 0; layersIndex<brain.size();layersIndex++) {
            for (int neuronsIndex = 0; neuronsIndex < brain.get(layersIndex).size(); neuronsIndex++) {
                HashMap<String, Double> weights = brain.get(layersIndex).get(neuronsIndex).getWeights();
                for (String key : weights.keySet()) {
                    if(weights.get(key) < minmax[0]){
                        minmax[0] = weights.get(key);
                    }
                    if(weights.get(key) > minmax[1]){
                        minmax[1] = weights.get(key);
                    }
                }
            }}
        return minmax;
    }
    public static void showTooltip(Stage owner, Control control, String tooltipText)
    {
        Point2D p = control.localToScene(0.0, 0.0);

        final Tooltip customTooltip = new Tooltip();
        customTooltip.setText(tooltipText);

        control.setTooltip(customTooltip);
        customTooltip.setAutoHide(true);

        customTooltip.show(owner, p.getX()
                + control.getScene().getX() + control.getScene().getWindow().getX(), p.getY()
                + control.getScene().getY() + control.getScene().getWindow().getY());

    }

    void setUp(ArrayList<ArrayList<Neuron>> brain) {

        /*Text scenetitle = new Text("Welcome");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));


        grid.add(scenetitle, 0, 1, 2, 1);*/

        int maxSize = 0;
        for (int layersIndex = 0; layersIndex < brain.size(); layersIndex++) {
            if (maxSize < brain.get(layersIndex).size()) {
                maxSize = brain.get(layersIndex).size();
            }
        }

//7 = 1000, 3 =
        int neuronSpace = maxSize *100 < height ? height / maxSize : 100;
        int layersSpace = brain.size()*250 < width ?width / brain.size() : 250;

        for (int layersIndex = 0; layersIndex < brain.size(); layersIndex++) {
            for (int neuronsIndex = 0; neuronsIndex < brain.get(layersIndex).size(); neuronsIndex++) {
                Button btn = new Button();
                int x = 100 + layersSpace * layersIndex;
                int y = maxSize *100 < height ? 100 + (height - (neuronSpace * brain.get(layersIndex).size())) / 2 + (neuronsIndex * neuronSpace) : 100 + (maxSize*100 - (neuronSpace * brain.get(layersIndex).size())) / 2 + (neuronsIndex * neuronSpace); //  (1000 - (neuronSpace * brain.get(layersIndex).size()))/2 + (neuronsIndex*neuronSpace)
                btn.setLayoutX(x);
                btn.setLayoutY(y);
                btn.setMinWidth(40);
                btn.setMaxWidth(40);
                btn.setMinHeight(40);
                btn.setMaxHeight(40);
                btn.setText("0.0");
                signals.put(brain.get(layersIndex).get(neuronsIndex).getNeuronId(), btn);
                root.getChildren().add(btn);
                HashMap<String, WeightView> weightViews = new HashMap<>();
                for (String neuronId : brain.get(layersIndex).get(neuronsIndex).getBackConnections()) {
                    Button button = signals.get(neuronId);
                    int lineStartX = (int) (button.getLayoutX() + 50);
                    int lineStartY = (int) (button.getLayoutY() + 25);
                    int lineEndX = x;
                    int lineEndY = y + 25;

                    Line line = new Line(lineStartX, lineStartY, lineEndX, lineEndY);
                    line.setStroke(Color.BLACK);
                    root.getChildren().add(line);
                    //lineStartX + (lineEndX - lineStartX)/2
                    int textX = (int) (lineStartX + (lineEndX - lineStartX) / 1.2);
                    int textY = (int) (lineStartY + (lineEndY - lineStartY) / 1.2) - 15;

                    Text text = new Text(textX, textY, "");
                    root.getChildren().add(text);
                    weightViews.put(neuronId, new WeightView(text, line));
                }
                weights.put(brain.get(layersIndex).get(neuronsIndex).getNeuronId(), weightViews);
            }
        }
    }

    public void showProgress(int completeion, int percentage, double learningRate) {
        Platform.runLater(()-> {
            completionText.setText("Completed on: " + completeion + "%");
            percentageText.setText("Learning percentage is: " + percentage + "%");
            learningRateText.setText("Learning rate is: " + learningRate + "%");
        });
    }

    interface OnStartListener {
        void onStarted(GUI instance);
    }


    class WeightView {
        Text text;
        Line line;

        public WeightView(Text text, Line line) {
            this.text = text;
            this.line = line;
        }

        public void showData(double min, double max, double data) {
       // text.setText(String.format("%.1f", data));
           /* if(min < 0) {
                data = min + data;
                max = min + max;
                data = 0.01 *((max/100) * data);
                // max = min + max // == 151 == 100% ; 1% = 151/100 == 1.51; data == 14 == 1.51*14 == blabla%; data = 0.01 * blabla

            } else {*/

           //min = -35, max = 500, data = 0

                data = data - min; // data = 35
                max = max - min; // max = 535
                data = 0.01 *((100/max) * data); // data = 0.01 * (( 100 / 100) *50)
            //}
            //data = (data+1) /2;
        if(data >=0 && data <=1) {

            line.setStroke(new Color(1 - data, data, 0, 1));
        } else {
            if(data < 0) {
                line.setStroke(new Color(1, 0, 0, 0));
            } else {
                    line.setStroke(new Color(0, 1, 0, 0));
                }

        }
        }
    }
}
