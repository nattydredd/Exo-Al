package listeners;

import java.io.InputStream;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import model.JDBCBean;
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

        //Get JDBC Parameters (local)
//        String driver = context.getInitParameter("JDBC-Driver");
//        String url = context.getInitParameter("JDBC-URL");
//        String userName = context.getInitParameter("JDBC-UserName");
//        String password = context.getInitParameter("JDBC-Password");

        //Get JDBC Parameters (AWS)
        String driver = context.getInitParameter("JDBC-Driver");
        String dbName = System.getProperty("RDS_DB_NAME");
        String userName = System.getProperty("RDS_USERNAME");
        String password = System.getProperty("RDS_PASSWORD");
        String hostname = System.getProperty("RDS_HOSTNAME");
        String port = System.getProperty("RDS_PORT");
        String url = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName;
        
        //Create JDBC bean
        bean = new JDBCBean(driver, url, userName, password);

        //Pass JDBC connected bean to context
        context.setAttribute("JDBCBean", bean);

        try {
            //Retreive datasets
            Instances trainingSet = getDataset(context, context.getInitParameter("TrainingSetSource"));
            Instances validationSet = getDataset(context, context.getInitParameter("ValidationSetSource"));
            Instances testSet = getDataset(context, context.getInitParameter("TestSetSource"));

            //Pass datasets to context
            context.setAttribute("TrainingSet", trainingSet);
            context.setAttribute("ValidationSet", validationSet);
            context.setAttribute("TestSet", testSet);

            //Set initial results flag to false
            context.setAttribute("InitialResultsFlag", false);

            //Set session counter to 0
            context.setAttribute("SessionCounter", 0);

            //Set user classifications/correct/incorrect count to 0
            context.setAttribute("UserClassificationCount", 0);
            context.setAttribute("UserClassificationCorrect", 0);
            context.setAttribute("UserClassificationIncorrect", 0);

            //Generate table in database for query list
            createQueryTable();
            //Generate table in database for user classified stars
            createClassifiedTable();

        } catch (Exception ex) {
            System.err.println("StartupListener contextInitialized exception: " + ex);
        }

        System.out.println("Exiting StartupListener...");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {

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

        //Start JDBC
        bean.startJDBC();

        try {
            //Create new table
            bean.executeSQLUpdate("DROP TABLE IF EXISTS `queryTable`;");
            bean.executeSQLUpdate("CREATE TABLE IF NOT EXISTS `queryTable` ("
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

        //Stop JDBC
        bean.stopJDBC();

        System.out.println("Exiting StartupListener - createQueryTable");
    }

    //Create new database table for stars classified by users
    public void createClassifiedTable() {
        System.out.println("Entering StartupListener - createQueryTable");

        //Start JDBC
        bean.startJDBC();

        try {
            //Create new table
            bean.executeSQLUpdate("DROP TABLE IF EXISTS `classifiedTable`;");
            bean.executeSQLUpdate("CREATE TABLE IF NOT EXISTS `classifiedTable` ("
                    + " `starID` text CHARACTER SET ascii NOT NULL,"
                    + " `decisionCount` int NOT NULL,"
                    + " `classVal_1` int NOT NULL,"
                    + " `classVal_2` int NOT NULL,"
                    + " `classVal_3` int NOT NULL,"
                    + " `classVal_4` int NOT NULL,"
                    + " `classVal_5` int NOT NULL,"
                    + " `total` int NOT NULL,"
                    + " `class` int NOT NULL,"
                    + " PRIMARY KEY (`starID`(15))"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=latin1;");

        } catch (SQLException ex) {
            System.err.println("StartupListener failed to create classifiedList table exception: " + ex);
        }

        //Stop JDBC
        bean.stopJDBC();

        System.out.println("Exiting StartupListener - createQueryTable");
    }
}
