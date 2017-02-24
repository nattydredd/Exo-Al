package model;

// @author Nate
import java.util.ArrayList;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

public class StarClassifier {

    //Classifier variables
    private FilteredClassifier classifier;
    private double correct;
    private double correctPercent;
    private double incorrect;
    private double incorrectPercent;
    private double totalNumInstances;
    private double[][] confusionMatrix;
    private ArrayList<PredictionSet> predictions;

    //Constructors   
    public StarClassifier() {
    }

    //Methods
    public FilteredClassifier getClassifier() {
        return classifier;
    }

    public void setClassifier(FilteredClassifier classifier) {
        this.classifier = classifier;
    }

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

    //Builds a classifier on input instances
    public void buildClassifier(Instances data) {
        System.out.println("Entering StarClassifier - buildClassifier");

        try {
            //Set class index
            data.setClassIndex(data.numAttributes() - 1);

            //Remove Id
            Remove removedAtt = new Remove();
            removedAtt.setAttributeIndices("1");

            //Remove mean //???????
            Remove removedAtt2 = new Remove();
            removedAtt2.setAttributeIndices("5");

            //Create classifier
            RandomForest randForest = new RandomForest();
            randForest.setOptions(weka.core.Utils.splitOptions("-P 100 -I 1000 -num-slots 1 -K 0 -M 1.0 -V 0.001 -S 1"));

            //Create filtered classifier
            this.classifier = new FilteredClassifier();
            this.classifier.setFilter(removedAtt);
//            this.classifier.setFilter(removedAtt2);
            this.classifier.setClassifier(randForest);
            this.classifier.buildClassifier(data);

        } catch (Exception ex) {
            System.err.println("StarClassifier buildClassifier exception: " + ex);
        }

        System.out.println("Exiting StarClassifier - buildClassifier");
    }

    //Evaluates pre-built classifier on given instances and saves results, anonFlag anonymises the class label
    public void evaluateClassifier(Instances data, boolean anonymiseFlag) {
        System.out.println("Entering StarClassifier - evaluateClassifier");

        //Set class index
        data.setClassIndex(data.numAttributes() - 1);

        //Anonymise class label
        if (anonymiseFlag) {
            for (int i = 0; i < data.size(); i++) {
                data.get(i).setClassMissing();
            }
        }

        try {
            //Evaluate classifier
            Evaluation eval = new Evaluation(data);
            eval.evaluateModel(classifier, data);

            //Save results
            saveResults(eval, data);

            System.out.println(eval.toSummaryString());
            System.out.println(eval.toMatrixString());
            System.out.println(eval.toClassDetailsString());

        } catch (Exception ex) {
            System.err.println("StarClassifier evaluateClassifier exception: " + ex);
        }

        System.out.println("Exiting StarClassifier - evaluateClassifier");
    }

    //Saves results of evaluated classifier
    private void saveResults(Evaluation eval, Instances data) {
        System.out.println("Entering StarClassifier - saveResults");

        //Results variables
        this.correct = eval.correct();
        this.correctPercent = eval.pctCorrect();
        this.incorrect = eval.incorrect();
        this.incorrectPercent = eval.pctIncorrect();
        this.totalNumInstances = eval.numInstances();
        this.confusionMatrix = eval.confusionMatrix();
        this.predictions = new ArrayList<>();

        //Get predictions from evaluation object
        ArrayList<Prediction> classifierPredictions = eval.predictions();
        Prediction prediction;
        String[] predTokens;

        //Variables for each prediction
        int starID;
        double actualClass;
        double predictedClass;
        double nonHostDistribution;
        double hostDistribution;
        boolean error;

        for (int i = 0; i < data.size(); i++) {
            //Stars ID
            starID = Double.valueOf(data.instance(i).value(0)).intValue();

            //Split prediction string
            prediction = classifierPredictions.get(i);
            predTokens = prediction.toString().split(" ");

            //Prediction results
            actualClass = Double.valueOf(predTokens[1]);
            predictedClass = Double.valueOf(predTokens[2]);
            nonHostDistribution = Double.valueOf(predTokens[4]);
            hostDistribution = Double.valueOf(predTokens[5]);
            error = actualClass == predictedClass ? false : true;

            this.predictions.add(new PredictionSet(starID, actualClass, predictedClass, nonHostDistribution, hostDistribution, error));
        }

        System.out.println("Exiting StarClassifier - saveResults");
    }

    //Returns list of star Id's that have been classified with low confidence
    public ArrayList generateQueryList() {
        System.out.println("Entering StarClassifier - generateQueryList");

        //Check predictions have been made
        if (this.predictions == null) {
            System.err.println("No predictions made yet!");
            System.out.println("Exiting StarClassifier - generateQueryList");
            return null;
        }

        //List of star ID's
        ArrayList<String> queryList = new ArrayList<>();

        //For each prediction
        for (PredictionSet prediction : predictions) {
            
//            //If prediction was incorrect add to the list
//            if (prediction.getError() == true) {
//                queryList.add(String.valueOf(prediction.getStarID()));
//            } //Else if confidence was low add to the list
//            else if (prediction.getError() == false) {
//                if (prediction.getActualClass() == 0 && prediction.getNonHostDistribution() < 0.55) {
//                    queryList.add(String.valueOf(prediction.getStarID()));
//                } else if (prediction.getActualClass() == 1 && prediction.getHostDistribution() < 0.55) {
//                    queryList.add(String.valueOf(prediction.getStarID()));
//                }
//            }
            
            //If prediction confidence was low add to the list
            if (prediction.getPredictedClass() == 0 && prediction.getNonHostDistribution() < 0.7) {
                System.out.println("pred Class " + prediction.getPredictedClass() + " " + prediction.getNonHostDistribution());
                queryList.add(String.valueOf(prediction.getStarID()));
            }
            else if (prediction.getPredictedClass() == 1 && prediction.getHostDistribution() < 0.7) {
                System.out.println("pred Class " + prediction.getPredictedClass() + " " + prediction.getHostDistribution());
                 queryList.add(String.valueOf(prediction.getStarID()));
            }         
        }
        System.out.println("query List Length " + queryList.size());
        System.out.println("Query List " + queryList);
        System.out.println("Exiting StarClassifier - generateQueryList");
        return queryList;
    }
}//End StarClassifier
