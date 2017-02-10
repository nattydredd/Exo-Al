package listeners;

import java.io.InputStream;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import model.JDBCBean;
import model.StarClassifier;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Web application lifecycle listener.
 *
 * @author Nate
 */
public class StartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        System.out.println("Entering StartupListener...");

        //Get servlet context
        ServletContext context = event.getServletContext();
        
        //Start JDBC bean
        JDBCBean bean = new JDBCBean();

        //Get JDBC Parameters
        String driver = context.getInitParameter("JDBC-Driver");
        String url = context.getInitParameter("JDBC-URL");
        String userName = context.getInitParameter("JDBC-UserName");
        String password = context.getInitParameter("JDBC-Password");

        //Start JDBC
        bean.startJDBC(driver, url, userName, password);

        //Pass JDBC connected bean to context
        context.setAttribute("JDBCBean", bean);

        try {
            //Retreive datasets
            Instances trainingData = getDataset(context, "resources/datasets/TrainingSet.arff");
            Instances validationData = getDataset(context, "resources/datasets/ValidationSetTEST.arff");
            Instances testData = getDataset(context, "resources/datasets/TestSet.arff");
            
            //Build and train classifier
            StarClassifier classifier = new StarClassifier();
            classifier.buildClassifier(trainingData);
            
            //Evaluate classifier on validation set
            classifier.evaluateClassifier(validationData);
            
            //Get list for user classification
            ArrayList<String> classificationList = classifier.generateClassificationList();
            context.setAttribute("ClassificationList", classificationList);
            
        } catch (Exception ex) {
            System.err.println("StartupListener contextInitialized exception: " + ex);
        }
        
        System.out.println("Exiting StartupListener...");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        //Get servlet context
        ServletContext context = event.getServletContext();
        //StopJDBC bean
        JDBCBean bean = (JDBCBean) context.getAttribute("JDBCBean");
        bean.stopJDBC();
    }
    
    //Returns Instances list variable for provided resource string
    public Instances getDataset(ServletContext context, String resource){
        System.out.println("Entering StartupListener - getDataset");
        
        Instances returnSet = null;
        try {
            InputStream inputStream = context.getResourceAsStream(resource);
            DataSource source = new ConverterUtils.DataSource(inputStream);
            returnSet = source.getDataSet();
            inputStream.close();
            
        } catch (Exception ex) {
            System.err.println("StartupListener getDataset exception: " + ex);
        }
        
        System.out.println("Exiting StartupListener - getDataset");
        
    return returnSet;
    }
}
