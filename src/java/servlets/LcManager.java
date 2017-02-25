package servlets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import weka.core.Debug.Random;

//@author Nate
@WebServlet(name = "LcManager", urlPatterns = {"/LcManager"})
public class LcManager extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        getServletContext().log("Entering LcManager processRequest...");

        //Light curve directory path
        String lcPath = request.getSession().getServletContext().getRealPath("/resources/lightcurves/");

        //Light curve list
        ArrayList<String> lcList;

        //Get or create session
        HttpSession session = request.getSession();
        getServletContext().log("Session ID " + session.getId());

        //If session is new generate list of light curves
        if (session.isNew()) {
            lcList = generateSessionLcList(lcPath);
            session.setAttribute("LcList", lcList);
        } //Else get light curve list
        else {
            lcList = (ArrayList) session.getAttribute("LcList");
            //If light curve list is null get a new one
            if (lcList == null) {
                lcList = generateSessionLcList(lcPath);
                session.setAttribute("LcList", lcList);
            }
        }

        //Send light curve path as response
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("../resources/lightcurves/" + lcList.get(0).split("_")[0] + "/" + lcList.get(0));

        //Remove the light curve that was just sent     
        getServletContext().log("Removing light curve: " + lcList.get(0));
        lcList.remove(0);
        getServletContext().log("New list size: " + lcList.size());
        
        //Set list to session attribute
        session.setAttribute("LcList", lcList);

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

    private ArrayList generateSessionLcList(String lcPath) {
        getServletContext().log("Entering LcManager - generateSessionLcList");

        //Get the list of stars to classify
        ServletContext context = getServletContext();
        ArrayList<String> classificationList = (ArrayList) context.getAttribute("ClassificationList");

        //List to return for session
        ArrayList<String> sessionList = new ArrayList();

        //Get list of light curves in directory
        File[] listFiles;
        for (String starID : classificationList) {
            listFiles = new File(lcPath + starID).listFiles();
            //Copy each quarters light curve to session list
            for (int i = 0; i < listFiles.length; i++) {
                sessionList.add(listFiles[i].getName());
            }
        }
        
        //Shuffle list
        Collections.shuffle(sessionList, new Random());
        
        getServletContext().log("Exiting LcManager - generateSessionLcList");
        return sessionList;
    }
}
