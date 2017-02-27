package servlets;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.JDBCBean;
import model.ResultSet;

//@author Nate
public class ClassifyManager extends HttpServlet {
    //JSON converter

    Gson gson = new Gson();

    //Servlet context variable
    private ServletContext context;

    //JDBC connection bean
    private JDBCBean bean;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        getServletContext().log("----------------------------------------");
        getServletContext().log("Entering ClassifyManager processRequest...");

        //Get servlet context
        context = request.getServletContext();

        //Get JDBC bean     
        bean = (JDBCBean) context.getAttribute("JDBCBean");

        ResultSet resultSet = (ResultSet) context.getAttribute("ResultSet");

        JsonObject resultObj = new JsonObject();
        
        resultObj.addProperty("correct", resultSet.getCorrect());
        resultObj.addProperty("correctPercent", resultSet.getCorrectPercent());
        resultObj.addProperty("incorrect", resultSet.getIncorrect());
        resultObj.addProperty("incorrectPercent", resultSet.getIncorrectPercent());
        resultObj.addProperty("totalNumInstances", resultSet.getTotalNumInstances());
        resultObj.add("matrix", gson.toJsonTree(resultSet.getConfusionMatrix()));
  
//        String results = gson.toJson(resultSet.getConfusionMatrix());
                String results = gson.toJson(resultObj);
        String responseText = results;

        getServletContext().log("Sending response " + responseText);
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

}
