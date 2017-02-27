package model;

// @author Nate
import java.util.ArrayList;

public class ResultSet {

    //Variables
    private double correct;
    private double correctPercent;
    private double incorrect;
    private double incorrectPercent;
    private double totalNumInstances;
    private double[][] confusionMatrix;
    private ArrayList<PredictionSet> predictions;

    //Constructors   
    public ResultSet() {
    }
    
    //Methods
    public double getCorrect() {
        return correct;
    }

    public void setCorrect(double correct) {
        this.correct = correct;
    }

    public double getCorrectPercent() {
        return correctPercent;
    }

    public void setCorrectPercent(double correctPercent) {
        this.correctPercent = correctPercent;
    }

    public double getIncorrect() {
        return incorrect;
    }

    public void setIncorrect(double incorrect) {
        this.incorrect = incorrect;
    }

    public double getIncorrectPercent() {
        return incorrectPercent;
    }

    public void setIncorrectPercent(double incorrectPercent) {
        this.incorrectPercent = incorrectPercent;
    }

    public double getTotalNumInstances() {
        return totalNumInstances;
    }

    public void setTotalNumInstances(double totalNumInstances) {
        this.totalNumInstances = totalNumInstances;
    }

    public double[][] getConfusionMatrix() {
        return confusionMatrix;
    }

    public void setConfusionMatrix(double[][] confusionMatrix) {
        this.confusionMatrix = confusionMatrix;
    }

    public ArrayList<PredictionSet> getPredictions() {
        return predictions;
    }

    public void setPredictions(ArrayList<PredictionSet> predictions) {
        this.predictions = predictions;
    }
        
    //Returns list of star Id's that have been classified with confidence below supplied threshold
    public ArrayList generateQueryList(double confidenceThreshold) {
        System.out.println("Entering ResultSet - generateQueryList");

        //Check predictions have been made
        if (this.predictions == null) {
            System.err.println("No predictions exist!");
            System.out.println("Exiting ResultSet - generateQueryList");
            return null;
        }

        //List of star ID's
        ArrayList<String> queryList = new ArrayList<>();

        //For each prediction
        for (PredictionSet prediction : predictions) {
            //If prediction confidence was low add to the list
            if (prediction.getPredictedClass() == 0 && prediction.getNonHostDistribution() < confidenceThreshold) {
                queryList.add(String.valueOf(prediction.getStarID()));
            } else if (prediction.getPredictedClass() == 1 && prediction.getHostDistribution() < confidenceThreshold) {
                queryList.add(String.valueOf(prediction.getStarID()));
            }
        }
        
        System.out.println("Query List Length " + queryList.size());
        System.out.println("Query List " + queryList);
        System.out.println("Exiting ResultSet - generateQueryList");
        return queryList;
    }
}//End ResultSet
