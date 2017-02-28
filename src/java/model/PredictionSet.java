package model;

// @author Nate

public class PredictionSet {

    //Variables
    private int starID;
    private double actualClass;
    private double predictedClass;
    private double nonHostDistribution;
    private double hostDistribution;
    private boolean error;
    
    //Constructors
    public PredictionSet() {
    }

    public PredictionSet(int starID, double actualClass, double predictedClass, double nonHostDistribution, double hostDistribution, boolean error) {
        this.starID = starID;
        this.actualClass = actualClass;
        this.predictedClass = predictedClass;
        this.nonHostDistribution = nonHostDistribution;
        this.hostDistribution = hostDistribution;
        this.error = error;
    }
    
    //Methods
    public int getStarID() {
        return starID;
    }

    public void setStarID(int starID) {
        this.starID = starID;
    }

    public double getActualClass() {
        return actualClass;
    }

    public void setActualClass(double actualClass) {
        this.actualClass = actualClass;
    }

    public double getPredictedClass() {
        return predictedClass;
    }

    public void setPredictedClass(double predictedClass) {
        this.predictedClass = predictedClass;
    }

    public double getNonHostDistribution() {
        return nonHostDistribution;
    }

    public void setNonHostDistribution(double nonHostDistribution) {
        this.nonHostDistribution = nonHostDistribution;
    }

    public double getHostDistribution() {
        return hostDistribution;
    }

    public void setHostDistribution(double hostDistribution) {
        this.hostDistribution = hostDistribution;
    }

    public boolean getError() {
        return error;
    }

    public void setHostDistribution(boolean error) {
        this.error = error;
    }
    
    @Override
    public String toString() {
        return "PredictionSet{" + " starID = " + starID + ", actualClass = " + actualClass + ", predictedClass = " + predictedClass + ", nonHostDistribution = " + nonHostDistribution + ", hostDistribution = " + hostDistribution + ", error = " + error + '}';
    }
    
    
}//End PredictionSet
