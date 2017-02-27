package model;

// @author Nate
import java.util.ArrayList;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.filters.supervised.instance.Resample;
import weka.filters.unsupervised.attribute.Remove;

public class StarClassifier {

    //Classifier variables
    private FilteredClassifier classifier;
    private ResultSet resultSet;

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

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void setResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
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
            this.classifier.setFilter(removedAtt2);
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

        this.resultSet = new ResultSet();
        //Results variables
        this.resultSet.setCorrect(eval.correct());
        this.resultSet.setCorrectPercent(eval.pctCorrect());
        this.resultSet.setIncorrect(eval.incorrect());
        this.resultSet.setIncorrectPercent(eval.pctIncorrect());
        this.resultSet.setTotalNumInstances(eval.numInstances());
        this.resultSet.setConfusionMatrix(eval.confusionMatrix());

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

        //Get predictions for each star
        ArrayList<PredictionSet> predictions = new ArrayList();
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

            predictions.add(new PredictionSet(starID, actualClass, predictedClass, nonHostDistribution, hostDistribution, error));
        }
        
        //Add to result set
        this.resultSet.setPredictions(predictions);

        System.out.println("Exiting StarClassifier - saveResults");
    }

}//End StarClassifier
