package servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.JDBCBean;
import model.ResultSet;
import model.StarClassifier;
import weka.core.Instance;
import weka.core.Instances;

//@author Nate
public class ClassifyManager extends HttpServlet {

    //JSON converter
    Gson gson = new Gson();

    //Servlet context variable
    private ServletContext context;

    //JDBC connection bean
    private JDBCBean bean;

    //Classifier
    StarClassifier classifier;

    //Query confidence threshold
    private final double queryConfidenceThreshold = 0.7;

    //Global query list
    private ArrayList<String> queryList;

    //Datasets
    Instances trainingSet;
    Instances validationSet;
    Instances testSet;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        getServletContext().log("----------------------------------------");
        getServletContext().log("Entering ClassifyManager processRequest...");

        //Get servlet context
        context = request.getServletContext();

        //Get JDBC bean     
        bean = (JDBCBean) context.getAttribute("JDBCBean");

        //Determine action requested
        String action = request.getParameter("action");
        getServletContext().log("ClassifyManager received a request to: " + action);

        String responseText = null;
        getServletContext().log("Executing action: " + action);
        switch (action) {

            case "build":
                responseText = buildClassifier();
                getServletContext().log(responseText);
                break;

            case "query":
                responseText = queryClassifier();
                getServletContext().log(responseText);
                break;

            case "resample":
                responseText = resample();
                getServletContext().log(responseText);
                break;

            case "test":
                responseText = testClassifier();
                getServletContext().log(responseText);
                break;

            case "getResults":
                responseText = getResults(request.getParameter("resultSet"));
                getServletContext().log("Retreived results: " + responseText);
                break;

            case "getUsageStats":
                responseText = getUsageStats();
                getServletContext().log("Retreived usage stats: " + responseText);
                break;

            case "getTable":
                responseText = getTable(request.getParameter("table"));
                getServletContext().log("Retreived table: " + responseText);
                break;

            default:
                responseText = "null";
                getServletContext().log("Default response: null");
                break;
        }

        getServletContext().log("Sending response: " + responseText);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseText);

        getServletContext().log("Exiting ClassifyManager processRequest...");
        getServletContext().log("----------------------------------------");
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    //Builds classifier on current training set,
    //Checks if there are instances to add from user classified query set
    private String buildClassifier() {
        getServletContext().log("Entering ClassifyManager - buildClassifier");

        //Get the current training set
        trainingSet = (Instances) context.getAttribute("TrainingSet");

        //Check training set is not empty
        if (trainingSet.isEmpty()) {
            getServletContext().log("Training set empty! Classifier not built");
            getServletContext().log("Exiting ClassifyManager - buildClassifier");
            return "Training set empty! Classifier not built";
        }

        //Build and train classifier
        classifier = new StarClassifier();
        classifier.buildClassifier(trainingSet);

        getServletContext().log("Exiting ClassifyManager - buildClassifier");
        return "Classifier built";
    }

    //Applies classifier to validation set and generates query set,
    //Populates queryTable in database with results
    private String queryClassifier() {
        getServletContext().log("Entering ClassifyManager - queryClassifier");

        //Start JDBC
        bean.startJDBC();

        //Check classifier has been built
        if (classifier == null || classifier.getClassifier() == null) {
            getServletContext().log("Classifier has not been built yet! No query set created");
            getServletContext().log("Exiting ClassifyManager - queryClassifier");
            return "Classifier has not been built yet! No query set created";
        }

        //Get validation set
        validationSet = (Instances) context.getAttribute("ValidationSet");
        //Check validation set is not empty
        if (validationSet.isEmpty()) {
            getServletContext().log("Validation set is empty! No query set created");
            getServletContext().log("Exiting ClassifyManager - queryClassifier");
            return "Validation set is empty! No query set created";
        }

        //Evaluate classifier on validation set
        boolean anonymiseFlag = true;
        classifier.evaluateClassifier(validationSet, anonymiseFlag);
        classifier.saveResults(validationSet, "Evaluation");

        //Get list for user classification
        queryList = classifier.getResultSet().generateQueryList(queryConfidenceThreshold);
        context.setAttribute("QueryList", queryList);

        //Populate table with stars in the query list
        updateQueryTable(queryList);

        getServletContext().log("Query List Length " + queryList.size());
        getServletContext().log("Query List " + queryList);

        //Stop JDBC
        bean.stopJDBC();

        getServletContext().log("Exiting ClassifyManager - queryClassifier");
        return "Query set generated";
    }

    //Adds user classified instances from query set into training set
    public String resample() {
        getServletContext().log("Entering ClassifyManager - resample");

        //Start JDBC
        bean.startJDBC();

        //Get all instances in query set that have reached query limit and have non zero classification total
        ArrayList<ArrayList<Object>> resultsSet = null;
        try {
            resultsSet = bean.sqlQueryToArrayList("Select * FROM queryTable WHERE decisionCount>='5' AND total<>'0'");
        } catch (SQLException ex) {
            System.err.println("ClassifyManager failed to retreive queryTable exception: " + ex);
        }

        //Check results set is not empty
        if (resultsSet.isEmpty()) {
            getServletContext().log("Results set empty! No stars have been classified");
            getServletContext().log("Exiting ClassifyManager - resample");
            return "Results set empty! No stars have been classified";
        }

        //Get validation and training sets
        validationSet = (Instances) context.getAttribute("ValidationSet");
        trainingSet = (Instances) context.getAttribute("TrainingSet");

        //Check validation and training set are not empty
        if (validationSet.isEmpty() || trainingSet.isEmpty()) {
            getServletContext().log("Validation or Training set empty! Cannon resample");
            getServletContext().log("Exiting ClassifyManager - resample");
            return "Validation or Training set empty! Cannon resample";
        }
        //Set class index
        validationSet.setClassIndex(validationSet.numAttributes() - 1);

        //Get user classification counts
        int userClassificationCount = (int) context.getAttribute("UserClassificationCount");
        int userClassificationCorrect = (int) context.getAttribute("UserClassificationCorrect");
        int userClassificationIncorrect = (int) context.getAttribute("UserClassificationIncorrect");

        //For each instance in the result set get the corresponding instance from the validation set
        String currentStarID;
        for (int i = 0; i < resultsSet.size(); i++) {
            for (int j = 0; j < validationSet.size(); j++) {
                currentStarID = (String) resultsSet.get(i).get(0);

                //If the star ID's match
                if (validationSet.get(j).value(0) == Double.valueOf(currentStarID)) {

                    //Make a copy of the instance from the validation set
                    Instance tmpInstance = validationSet.get(j);
                    getServletContext().log("Copying instance for star: " + tmpInstance.value(0));

                    //Get the total user classification score
                    int classificationScore = (int) resultsSet.get(i).get(7);
                    getServletContext().log("User classification score: " + classificationScore);

                    //Determine what class it has been classified as
                    int starClass = classificationScore > 0 ? 1 : 0;
                    getServletContext().log("User assigned star class: " + starClass);

                    //Update user classifiction counters
                    userClassificationCount++;
                    context.setAttribute("UserClassificationCount", userClassificationCount);
                    if (tmpInstance.classValue() == starClass) {
                        userClassificationCorrect++;
                        context.setAttribute("UserClassificationCorrect", userClassificationCorrect);
                        getServletContext().log("User assigned star class was correct!");
                    } else {
                        userClassificationIncorrect++;
                        context.setAttribute("UserClassificationIncorrect", userClassificationIncorrect);
                        getServletContext().log("User assigned star class was not correct!");
                    }

                    //Set the class for the user classified instance
                    tmpInstance.setClassValue(starClass);

                    //Determine how many copies to insert (turn score positive if negative)             
                    classificationScore = classificationScore < 0 ? classificationScore * -1 : classificationScore;
                    //Insert one copy for every 20 classification score
                    int numberToInsert = ((classificationScore - 1) / 20) + 1;
                    getServletContext().log("Inserting " + numberToInsert + " copies");

                    //Insert copies into training set
                    for (int k = 0; k < numberToInsert; k++) {
                        trainingSet.add(tmpInstance);
                    }

                    try {
                        //Remove user classified instances from the queryTable and validation set
                        bean.executeSQLUpdate("DELETE FROM queryTable WHERE starID='" + currentStarID + "'");
                        validationSet.remove(j);
                    } catch (SQLException ex) {
                        System.err.println("ClassifyManager failed to remove queryTable exception: " + ex);
                    }

                    //Add to classified table
                    updateClassifiedTable(resultsSet.get(i), starClass);
                }
            }
        }

        //Set validation and training sets
        context.setAttribute("TrainingSet", trainingSet);
        context.setAttribute("ValidationSet", validationSet);

        //Stop JDBC
        bean.stopJDBC();

        getServletContext().log("Exiting ClassifyManager - resample");
        return "Resampled instances added to training set";
    }

    //Applies classifier to test set and generates results
    private String testClassifier() {
        getServletContext().log("Entering ClassifyManager - testClassifier");

        //Check classifier has been built       
        if (classifier == null || classifier.getClassifier() == null) {
            getServletContext().log("Classifier has not been built yet! Test set not evaluated");
            getServletContext().log("Exiting ClassifyManager - testClassifier");
            return "Classifier has not been built yet! Test set not evaluated";
        }

        //Get test set
        testSet = (Instances) context.getAttribute("TestSet");
        //Check test set is not empty
        if (testSet.isEmpty()) {
            getServletContext().log("Test set empty! Classifier not tested");
            getServletContext().log("Exiting ClassifyManager - testClassifier");
            return "Test set empty! Classifier not tested";
        }

        //Check if this is the first test set evaluation
        String testSetLabel = null;
        if ((boolean) context.getAttribute("InitialResultsFlag") == false) {
            //If it is, set to initial result set and change flag
            testSetLabel = "InitialResults";
            context.setAttribute("InitialResultsFlag", true);
        } else {
            //Else this is the latest set of results
            testSetLabel = "CurrentResults";
        }

        //Evaluate classifier on test set
        boolean anonymiseFlag = false;
        classifier.evaluateClassifier(testSet, anonymiseFlag);
        classifier.saveResults(testSet, testSetLabel);

        //Get results set and pass to servlet context
        ResultSet testResultSet = classifier.getResultSet();
        context.setAttribute(testSetLabel, testResultSet);

        getServletContext().log("Exiting ClassifyManager - testClassifier");
        return "Test set evaluated";
    }

    //Returns results set in JSON string format
    private String getResults(String requestedResultSet) {
        getServletContext().log("Entering ClassifyManager - getResults");
        getServletContext().log("Request for results: " + requestedResultSet);

        //Check valid results set has been requested
        if ((!Collections.list(context.getAttributeNames()).contains(requestedResultSet))) {
            getServletContext().log("Requested results set is null!");
            getServletContext().log("Exiting ClassifyManager - getResults");
            return "No Results yet!";
        }

        //Get the requested resultSet
        ResultSet resultSet = (ResultSet) context.getAttribute(requestedResultSet);

        //Return results set in JSON format
        JsonObject resultObj = new JsonObject();
        resultObj.addProperty("resultSetLabel", resultSet.getName());
        resultObj.addProperty("correct", resultSet.getCorrect());
        resultObj.addProperty("correctPercent", resultSet.getCorrectPercent());
        resultObj.addProperty("incorrect", resultSet.getIncorrect());
        resultObj.addProperty("incorrectPercent", resultSet.getIncorrectPercent());
        resultObj.addProperty("totalNumInstances", resultSet.getTotalNumInstances());
        resultObj.add("matrix", gson.toJsonTree(resultSet.getConfusionMatrix()));

        getServletContext().log("Exiting ClassifyManager - getResultSet");
        return gson.toJson(resultObj);
    }

    //Returns usage statistics in JSON string format
    private String getUsageStats() {
        getServletContext().log("Entering ClassifyManager - getUsageStats");

        //Return results set in JSON format
        JsonObject resultObj = new JsonObject();
        resultObj.addProperty("sessionCounter", (int) context.getAttribute("SessionCounter"));
        resultObj.addProperty("userClassificationCount", (int) context.getAttribute("UserClassificationCount"));
        resultObj.addProperty("userClassificationCorrect", (int) context.getAttribute("UserClassificationCorrect"));
        resultObj.addProperty("userClassificationIncorrect", (int) context.getAttribute("UserClassificationIncorrect"));

        getServletContext().log("Exiting ClassifyManager - getUsageStats");
        return gson.toJson(resultObj);
    }

    //Returns query and classifications table in JSON format
    private String getTable(String table) {
        getServletContext().log("Entering ClassifyManager - getTable");

        //Start JDBC
        bean.startJDBC();

        //Create lists to hoild column and row data
        ArrayList rowData = new ArrayList();
        ArrayList columnLabels = new ArrayList();

        //Create empty JSON object for returning
        JsonObject resultObj = new JsonObject();

        //Create the column labels      
        columnLabels.add("Star ID");
        columnLabels.add("Count");
        columnLabels.add("Class val 1");
        columnLabels.add("Class val 2");
        columnLabels.add("Class val 3");
        columnLabels.add("Class val 4");
        columnLabels.add("Class val 5");
        columnLabels.add("Total");
        if (table.equalsIgnoreCase("classifiedTable")) {
            columnLabels.add("Class");
        }

        //Get data for specified table
        ArrayList<ArrayList<Object>> resultsSet = null;
        try {
            resultsSet = bean.sqlQueryToArrayList("Select * FROM " + table);
        } catch (SQLException ex) {
            System.err.println("ClassifyManager failed to get table exception: " + ex);

            //Stop JDBC
            bean.stopJDBC();
        }
        getServletContext().log("Requested " + table + " data: " + resultsSet);

        //Check requested table contains data
        if (resultsSet.isEmpty() || resultsSet == null) {
            getServletContext().log("Requested table " + table + " is empty!");

            //If not return only the column labels
            resultObj.add("columnLabels", gson.toJsonTree(columnLabels));
            resultObj.add("rowData", gson.toJsonTree(rowData));

            //Stop JDBC
            bean.stopJDBC();

            getServletContext().log("Exiting ClassifyManager - getTable");
            return gson.toJson(resultObj);
        }

        //Stop JDBC
        bean.stopJDBC();

        //Get the row data        
        for (ArrayList<Object> row : resultsSet) {
            JsonObject currentObj = new JsonObject();
            currentObj.addProperty("Star ID", (String) row.get(0));
            currentObj.addProperty("Count", (int) row.get(1));
            currentObj.addProperty("Class val 1", (int) row.get(2));
            currentObj.addProperty("Class val 2", (int) row.get(3));
            currentObj.addProperty("Class val 3", (int) row.get(4));
            currentObj.addProperty("Class val 4", (int) row.get(5));
            currentObj.addProperty("Class val 5", (int) row.get(6));
            currentObj.addProperty("Total", (int) row.get(7));
            if (table.equalsIgnoreCase("classifiedTable")) {
                currentObj.addProperty("Class", (int) row.get(8));
            }
            rowData.add(currentObj);
        }

        //Return results set in JSON format
        resultObj.add("columnLabels", gson.toJsonTree(columnLabels));
        resultObj.add("rowData", gson.toJsonTree(rowData));

        getServletContext().log("Exiting ClassifyManager - getTable");
        return gson.toJson(resultObj);
    }

    //Populates classified list table with user classified stars
    private void updateClassifiedTable(ArrayList currentStar, int starClass) {
        System.out.println("Entering ClassifyManager - updateClassifiedTable");

        try {
            //Add to classified table
            String sql = "INSERT INTO `classifiedTable` (`starID`, `decisionCount`,"
                    + "`classVal_1`, `classVal_2`, `classVal_3`, `classVal_4`, `classVal_5`, "
                    + "`total`, `class`)"
                    + "VALUES('" + currentStar.get(0) + "',";
            for (int i = 1; i < currentStar.size(); i++) {
                sql += currentStar.get(i) + ",";
            }
            sql += starClass + ");";

            bean.executeSQLUpdate(sql);
        } catch (SQLException ex) {
            System.err.println("ClassifyManager failed to update classifiedTable table exception: " + ex);
        }

        System.out.println("Exiting ClassifyManager - updateClassifiedTable");
    }

    //Populates query list table with stars in the query list
    private void updateQueryTable(ArrayList<String> queryList) {
        System.out.println("Entering ClassifyManager - updateQueryTable");

        try {
            //Create new rows for each star in query list
            for (String starID : queryList) {

                bean.executeSQLUpdate("INSERT INTO `queryTable` (`starID`, `decisionCount`,"
                        + "`classVal_1`, `classVal_2`, `classVal_3`, `classVal_4`, `classVal_5`, "
                        + "`total`)"
                        + "VALUES('" + starID + "', 4,"//////MAKE 0 again
                        + " 0, 0, 0, 0, 0,"
                        + " 0);");
            }

        } catch (SQLException ex) {
            System.err.println("ClassifyManager failed to populate queryTable exception: " + ex);
        }

        System.out.println("Exiting ClassifyManager - updateQueryTable");
    }
}
