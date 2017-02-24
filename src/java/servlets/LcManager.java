package servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import utilities.LightCurveComparator;
import weka.core.Debug.Random;

//@author Nate
@WebServlet(name = "LcManager", urlPatterns = {"/LcManager"})
public class LcManager extends HttpServlet {

    //Light curve directory path
    public String lcPath;
    //Light curve list
    public HashMap<String, ArrayList<String>> starList;
    //Current star
    public String currentStar;
    //Current light curve
    public String currentLc;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        getServletContext().log("Entering LcManager processRequest...");

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
        getServletContext().log("Star List: " + starList);
        getServletContext().log("Current Star: " + currentStar);
        getServletContext().log("Current Light Curve: " + currentLc);

        String responseText = null;
        switch (action) {

            case "getCurrentLc":
                responseText = "../resources/lightcurves/" + currentStar + "/" + currentLc;
                break;

            case "getNextLc":
                currentLc = getNextLc(starList, currentStar, currentLc);
                session.setAttribute("CurrentLc", currentLc);
                responseText = "../resources/lightcurves/" + currentStar + "/" + currentLc;
                break;

            case "getPrevLc":
                currentLc = getPrevLc(starList, currentStar, currentLc);
                session.setAttribute("CurrentLc", currentLc);
                responseText = "../resources/lightcurves/" + currentStar + "/" + currentLc;
                break;

            default:

                break;
        }
        //Send light curve path as response
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseText);

//        //Remove the light curve that was just sent     
//        getServletContext().log("Removing light curve: " + starList.get(0));
//        starList.remove(0);
//        getServletContext().log("New list size: " + starList.size());
//
//        //Set list to session attribute
//        session.setAttribute("LcList", starList);
        getServletContext().log("Exiting LcManager processRequest...");
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

        currentStar = getRandomFirstStar(starList);
        session.setAttribute("CurrentStar", currentStar);

        currentLc = starList.get(currentStar).get(0);
        session.setAttribute("CurrentLc", currentLc);

        getServletContext().log("Exiting LcManager - generateNewSessionVariables");
    }

    //Generates HashMap with star IDs as the key and a list of light curve quarters as the values
    private HashMap<String, ArrayList<String>> createSessionLcList(String lcPath) {
        getServletContext().log("Entering LcManager - createSessionLcList");

        //Get the list of stars to classify
        ServletContext context = getServletContext();
        ArrayList<String> queryList = (ArrayList) context.getAttribute("QueryList");

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

    //Gets a random first stars ID from the HashMap
    private String getRandomFirstStar(HashMap<String, ArrayList<String>> starList) {
        getServletContext().log("Entering LcManager - getRandomFirstStar");

        //Get the keys as a list
        ArrayList keysAsArray = new ArrayList(starList.keySet());
        Random r = new Random();
        //Select a random star id
        String randomID = (String) keysAsArray.get(r.nextInt(keysAsArray.size()));

        getServletContext().log("Exiting LcManager - getRandomFirstStar");
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

}
