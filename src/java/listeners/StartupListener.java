package listeners;

import java.io.InputStream;
import java.sql.SQLException;
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

    //Servlet contect variable
    private ServletContext context;
    //JDBC connection bean
    private JDBCBean bean;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        System.out.println("Entering StartupListener...");

        //Get servlet context
        context = event.getServletContext();

        //Start JDBC bean
        bean = new JDBCBean();

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
            Instances trainingSet = getDataset(context, "resources/datasets/TrainingSet.arff");
            Instances validationSet = getDataset(context, "resources/datasets/ValidationSetTEST.arff");
            Instances testSet = getDataset(context, "resources/datasets/TestSet.arff");

            //Pass datasets to context
            context.setAttribute("TrainingSet", trainingSet);
            context.setAttribute("ValidationSet", validationSet);
            context.setAttribute("TestSet", testSet);   
            
            //Set initial results flag to false
            context.setAttribute("InitialResultsFlag", false);

            //Generate table in database for query list
            createQueryTable();

        } catch (Exception ex) {
            System.err.println("StartupListener contextInitialized exception: " + ex);
        }

        System.out.println("Exiting StartupListener...");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        //StopJDBC bean
        bean.stopJDBC();
    }

    //Returns Instances list variable for provided resource string
    public Instances getDataset(ServletContext context, String resource) {
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

    //Create new database table for stars in the query list
    public void createQueryTable() {
        System.out.println("Entering StartupListener - createQueryTable");

        try {
            //Create new table
            bean.executeSQLUpdate("DROP TABLE IF EXISTS `queryList`;");
            bean.executeSQLUpdate("CREATE TABLE IF NOT EXISTS `queryList` ("
                    + " `starID` text CHARACTER SET ascii NOT NULL,"
                    + " `decisionCount` int NOT NULL,"
                    + " `classVal_1` int NOT NULL,"
                    + " `classVal_2` int NOT NULL,"
                    + " `classVal_3` int NOT NULL,"
                    + " `classVal_4` int NOT NULL,"
                    + " `classVal_5` int NOT NULL,"
                    + " `total` int NOT NULL,"
                    + " PRIMARY KEY (`starID`(15))"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=latin1;");

        } catch (SQLException ex) {
            System.err.println("StartupListener failed to create queryList table exception: " + ex);
        }

        System.out.println("Exiting StartupListener - createQueryTable");
    }  
}
