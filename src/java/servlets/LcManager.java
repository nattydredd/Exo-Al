package servlets;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.JDBCBean;
import utilities.LightCurveComparator;
import weka.core.Debug.Random;

//@author Nate
@WebServlet(name = "LcManager", urlPatterns = {"/LcManager"})
public class LcManager extends HttpServlet {

    //Servlet contect variable
    private ServletContext context;
    //JDBC connection bean
    private JDBCBean bean;

    //Light curve directory path
    private String lcPath;

    //Global query list
    private ArrayList<String> queryList;

    //Light curve list
    private HashMap<String, ArrayList<String>> starList;
    //Current star
    private String currentStar;
    //Current light curve
    private String currentLc;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        getServletContext().log("----------------------------------------");
        getServletContext().log("Entering LcManager processRequest...");

        //Get JDBC bean
        context = request.getServletContext();
        bean = (JDBCBean) context.getAttribute("JDBCBean");

        //Get light curve directory path
        lcPath = request.getSession().getServletContext().getRealPath("/resources/lightcurves/");

        //Determine action requested
        String action = request.getParameter("action");
        getServletContext().log("LcManager received a request to: " + action);

        //Get or create session
        HttpSession session = request.getSession();
        getServletContext().log("Session ID " + session.getId());
        session.setMaxInactiveInterval(30);

        //If session is new, generate list of light curves, get current star and current light curve
        if (session.isNew()) {
            generateNewSessionVariables(session);
            //Set action to get current light curve (rather than next, back or submit)
            action = "getCurrentLc";

        } //Else get light curve list, current star and current light curve
        else {
            starList = (HashMap) session.getAttribute("StarList");
            currentStar = (String) session.getAttribute("CurrentStar");
            currentLc = (String) session.getAttribute("CurrentLc");

            //If light curve list is null (because session was created elsewhere) get new values
            if (starList == null) {
                generateNewSessionVariables(session);
                //Set action to get current light curve (rather than next, back or submit)
                action = "getCurrentLc";
            }
        }

        //Display current list, star and light curve
        getServletContext().log("Star List: " + starList.keySet());
        getServletContext().log("Current Star: " + currentStar);
        getServletContext().log("Current Light Curve: " + currentLc);

        String responseText = null;
        switch (action) {

            case "getCurrentLc":
                responseText = "../resources/lightcurves/" + currentStar + "/" + currentLc;
                getServletContext().log("Getting current light curve: " + currentLc);
                break;

            case "getNextLc":
                currentLc = getNextLc(starList, currentStar, currentLc);
                session.setAttribute("CurrentLc", currentLc);
                responseText = "../resources/lightcurves/" + currentStar + "/" + currentLc;
                getServletContext().log("Getting next light curve: " + currentLc);
                break;

            case "getPrevLc":
                currentLc = getPrevLc(starList, currentStar, currentLc);
                session.setAttribute("CurrentLc", currentLc);
                responseText = "../resources/lightcurves/" + currentStar + "/" + currentLc;
                getServletContext().log("Getting previous light curve: " + currentLc);
                break;

            case "submit":
                //Record submitted value for current star
                if (currentStar != null) {
                    getServletContext().log("Updating queryListTable for star: " + currentStar + " with value " + request.getParameter("sliderValue"));
                    updateQueryListTable(currentStar, request.getParameter("sliderValue"));
                }

                //Remove star from this session     
                getServletContext().log("Removed current star: " + currentStar);
                starList.remove(currentStar);
                session.setAttribute("StarList", starList);

                currentStar = null;
                session.setAttribute("CurrentStar", currentStar);
                getServletContext().log("New star list: " + starList.keySet());

                //Set new current star and light curve if there are some left in the list     
                if (starList.isEmpty()) {
                    getServletContext().log("Sessions star list is empty!");
                    responseText = "null";
                    break;
                }
                getServletContext().log("Getting new current star and light curve");
                currentStar = getRandomStar(starList);
                session.setAttribute("CurrentStar", currentStar);

                currentLc = starList.get(currentStar).get(0);
                session.setAttribute("CurrentLc", currentLc);

                responseText = "../resources/lightcurves/" + currentStar + "/" + currentLc;
                break;

            default:
                responseText = "../resources/lightcurves/" + currentStar + "/" + currentLc;
                break;
        }

        //Send light curve path as response
        getServletContext().log("Sending response " + responseText);
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseText);

        getServletContext().log("Exiting LcManager processRequest...");
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

    //Create new light curve list, current star and current light curve for this session
    private void generateNewSessionVariables(HttpSession session) {
        getServletContext().log("Entering LcManager - generateNewSessionVariables");

        starList = createSessionLcList(lcPath);
        session.setAttribute("StarList", starList);

        currentStar = getRandomStar(starList);
        session.setAttribute("CurrentStar", currentStar);

        currentLc = starList.get(currentStar).get(0);
        session.setAttribute("CurrentLc", currentLc);

        getServletContext().log("Exiting LcManager - generateNewSessionVariables");
    }

    //Generates HashMap with star IDs as the key and a list of light curve quarters as the values
    private HashMap<String, ArrayList<String>> createSessionLcList(String lcPath) {
        getServletContext().log("Entering LcManager - createSessionLcList");

        //Get the list of stars to classify
        context = getServletContext();
        queryList = (ArrayList) context.getAttribute("QueryList");

        //Check query list is not empty
        if (queryList.isEmpty()) {
            getServletContext().log("Global query list is empty!");
            getServletContext().log("Exiting LcManager - createSessionLcList");
            return null;
        }

        //List to return for session
        HashMap<String, ArrayList<String>> sessionList = new HashMap();

        //Get list of quarterly light curves for each star in directory
        File[] listFiles;
        ArrayList<String> lcQuartersList;
        for (String starID : queryList) {
            listFiles = new File(lcPath + starID).listFiles();
            lcQuartersList = new ArrayList();

            //Copy each quarters light curve to quarters list
            for (int i = 0; i < listFiles.length; i++) {
                lcQuartersList.add(listFiles[i].getName());
            }

            //Sort so quarters show in correct order
            Collections.sort(lcQuartersList, new LightCurveComparator());

            //Add list of light curve quarters to session list
            sessionList.put(starID, lcQuartersList);
        }

        getServletContext().log("Exiting LcManager - createSessionLcList");
        return sessionList;
    }

    //Gets a random stars ID from the HashMap
    private String getRandomStar(HashMap<String, ArrayList<String>> starList) {
        getServletContext().log("Entering LcManager - getRandomStar");

        //Get the keys as a list
        ArrayList keysAsArray = new ArrayList(starList.keySet());

        //Check the list is not empty (all stars classified)
        if (keysAsArray.isEmpty()) {
            getServletContext().log("Session star list is empty!");
            getServletContext().log("Exiting LcManager - getRandomStar");
            return null;
        }

        //Select a random star id
        Random r = new Random();
        String randomID = (String) keysAsArray.get(r.nextInt(keysAsArray.size()));

        getServletContext().log("Exiting LcManager - getRandomStar");
        return randomID;
    }

    //Gets the next light curve for the current star
    private String getNextLc(HashMap<String, ArrayList<String>> starList, String currentStar, String currentLc) {
        getServletContext().log("Entering LcManager - getNextLc");

        //Add one to the curent light curves index
        int newIndex = starList.get(currentStar).indexOf(currentLc) + 1;
        //Wrap around if the end of the list is reached
        if (newIndex > starList.get(currentStar).size() - 1) {
            newIndex = 0;
        }

        getServletContext().log("Exiting LcManager - getNextLc");
        return starList.get(currentStar).get(newIndex);
    }

    //Gets the previous light curve for the current star
    private String getPrevLc(HashMap<String, ArrayList<String>> starList, String currentStar, String currentLc) {
        getServletContext().log("Entering LcManager - getPrevLc");

        //Subtract one to the curent light curves index
        int newIndex = starList.get(currentStar).indexOf(currentLc) - 1;
        //Wrap around if the begining of the list is reached
        if (newIndex < 0) {
            newIndex = starList.get(currentStar).size() - 1;
        }

        getServletContext().log("Exiting LcManager - getPrevLc");
        return starList.get(currentStar).get(newIndex);
    }

    //Adds submitted value for current star to queryList table and updates decision count
    //Removes current star from global query list if number of classifications >= 10
    private void updateQueryListTable(String starID, String sliderValue) {
        getServletContext().log("Entering LcManager - updateQueryList");

        try {
            //Convert submitted slider value to double
            int decisionValue = Integer.valueOf(sliderValue);

            //Get the current number of decisions submitted for this star
            int decisionCount = getStarDecisionCount(starID);

            //Get the total value from all decisions
            int totalDecisionValue = getStarDecisionTotal(starID);

            //If decision count maxiumum has not been reached record submitted result
            if (decisionCount <= 9) {
                //Update the table with the submitted decision value, new decision count and total
                bean.executeSQLUpdate("UPDATE queryList SET "
                        + "classVal_" + (decisionCount + 1) + "='" + decisionValue + "',"
                        + "decisionCount='" + (decisionCount + 1) + "',"
                        + "total='" + (decisionValue + totalDecisionValue) + "'"
                        + " WHERE starID='" + starID + "'");
            }
            //If decision count is/will at maxiumum, remove the star from the global query list
            if ((decisionCount + 1) >= 10) {
                getServletContext().log("Removing current star from global query list");
                //Get the query list from servlet context
                context = getServletContext();
                queryList = (ArrayList) context.getAttribute("QueryList");
                //Remove current star
                queryList.remove(currentStar);
                context.setAttribute("QueryList", queryList);
            }

        } catch (SQLException ex) {
            System.err.println("LcManager failed to updateQueryList exception: " + ex);
        }
        getServletContext().log("Exiting LcManager - updateQueryList");
    }

    //Retreive the current number of classification decisions for the give stars ID
    private int getStarDecisionCount(String starID) {
        getServletContext().log("Entering LcManager - getStarDecisionCount");

        int result = 0;
        ArrayList queryResult;
        try {
            queryResult = (ArrayList) bean.sqlQueryToArrayList("SELECT decisionCount FROM queryList WHERE starID='" + starID + "'").get(0);
            result = (int) queryResult.get(0);
        } catch (SQLException ex) {
            System.err.println("LcManager failed to getStarDecisionCount exception: " + ex);
        }

        getServletContext().log("Exiting LcManager - getStarDecisionCount");
        return result;
    }

    //Retreive the current number of classification decisions for the give stars ID
    private int getStarDecisionTotal(String starID) {
        getServletContext().log("Entering LcManager - getStarDecisionTotal");

        int result = 0;
        ArrayList queryResult;
        try {
            queryResult = (ArrayList) bean.sqlQueryToArrayList("SELECT total FROM queryList WHERE starID='" + starID + "'").get(0);
            result = (int) queryResult.get(0);
        } catch (SQLException ex) {
            System.err.println("LcManager failed to getStarDecisionCount exception: " + ex);
        }

        getServletContext().log("Exiting LcManager - getStarDecisionTotal");
        return result;
    }
}
