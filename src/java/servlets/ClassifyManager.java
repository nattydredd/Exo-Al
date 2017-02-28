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

    //Query threshold
    private final double queryThreshold = 0.7;

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
                responseText = buildClassifier() ? "Classifier built" : "Classifier not built";
                getServletContext().log(responseText);
                break;

            case "query":
                responseText = queryClassifier() ? "Query set built" : "Query set not built";
                getServletContext().log(responseText);
                break;

            case "test":
                responseText = testClassifier() ? "Test set evaluated" : "Test set not evaluated";
                getServletContext().log(responseText);
                break;

            case "getResults":               
                responseText = getResults(request.getParameter("resultSet"));
                getServletContext().log("Retreived results: " + responseText);
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
    private boolean buildClassifier() {
        getServletContext().log("Entering ClassifyManager - buildClassifier");

        //Get the current training set
        trainingSet = (Instances) context.getAttribute("TrainingSet");
        //Check if there are instances to add from classified query set

        //Build and train classifier
        classifier = new StarClassifier();
        classifier.buildClassifier(trainingSet);

        getServletContext().log("Exiting ClassifyManager - buildClassifier");
        return true;
    }

    //Applies classifier to validation set and generates query set,
    //Populates queryList table in database with results
    private boolean queryClassifier() {
        getServletContext().log("Entering ClassifyManager - queryClassifier");

        //Get validation set
        validationSet = (Instances) context.getAttribute("ValidationSet");

        //Check classifier has been built
        if (classifier == null || classifier.getClassifier() == null) {
            getServletContext().log("Classifier has not been built yet!");
            return false;
        }
        
        //Evaluate classifier on validation set
        boolean anonymiseFlag = true;
        classifier.evaluateClassifier(validationSet, anonymiseFlag);
        classifier.saveResults(validationSet, "Evaluation");

        //Get list for user classification
        ArrayList<String> queryList = classifier.getResultSet().generateQueryList(queryThreshold);
        context.setAttribute("QueryList", queryList);

        //Populate table with stars in the query list
        populateQueryTable(queryList);

        getServletContext().log("Query List Length " + queryList.size());
        getServletContext().log("Query List " + queryList);

        getServletContext().log("Exiting ClassifyManager - queryClassifier");
        return true;
    }

    //Applies classifier to test set and generates results
    private boolean testClassifier() {
        getServletContext().log("Entering ClassifyManager - testClassifier");

        //Get test set
        testSet = (Instances) context.getAttribute("TestSet");
    
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

        //Check classifier has been built       
        if (classifier == null || classifier.getClassifier() == null) {
            getServletContext().log("Classifier has not been built yet!");
            return false;
        }
 
        //Evaluate classifier on test set
        boolean anonymiseFlag = false;
        classifier.evaluateClassifier(testSet, anonymiseFlag);
        classifier.saveResults(testSet, testSetLabel);

        //Get results set and pass to servlet context
        ResultSet testResultSet = classifier.getResultSet();
        context.setAttribute(testSetLabel, testResultSet);

        getServletContext().log("Exiting ClassifyManager - testClassifier");
        return true;
    }

    //Returns results set in JSON string format
    private String getResults(String requestedResultSet) {
        getServletContext().log("Entering ClassifyManager - getResults");
        getServletContext().log("Request for results: " + requestedResultSet);
        
        //Check valid results set has been requested
        if ((!Collections.list(context.getAttributeNames()).contains(requestedResultSet))) {
            getServletContext().log("Requested results set is null!");
            getServletContext().log("Exiting ClassifyManager - getResults");
            return "null";
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

    //Populates query list table with stars in the query list
    public void populateQueryTable(ArrayList<String> queryList) {
        System.out.println("Entering ClassifyManager - populateQueryTable");

        try {
            //Create new rows for each star in query list
            for (String starID : queryList) {

                bean.executeSQLUpdate("INSERT INTO `queryList` (`starID`, `decisionCount`,"
                        + "`classVal_1`, `classVal_2`, `classVal_3`, `classVal_4`, `classVal_5`, "
                        + "`total`)"
                        + "VALUES('" + starID + "', 0,"
                        + " 0, 0, 0, 0, 0,"
                        + " 0);");
            }

        } catch (SQLException ex) {
            System.err.println("ClassifyManager failed to populate queryList table exception: " + ex);
        }

        System.out.println("Exiting ClassifyManager - populateQueryTable");
    }
}
