package model;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nate
 */
public class ResultSetTest {
    
    public ResultSetTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of set and get Name method, of class ResultSet.
     */
    @Test
    public void testSetAndGetName() {
        System.out.println("getName");
        ResultSet instance = new ResultSet();
        String name = "TestName";
        
        instance.setName(name);
        
        assertNotNull(instance.getName());
        assertEquals(instance.getName(), name);
    }

    /**
     * Test of set and get Correct method, of class ResultSet.
     */
    @Test
    public void testSetAndGetCorrect() {
        System.out.println("getCorrect");
        ResultSet instance = new ResultSet();
        double correct = 0.0;
        
        instance.setCorrect(correct);
        
        double expResult = 0.0;
        double result = instance.getCorrect();
        assertNotNull(instance.getCorrect());
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of set and get CorrectPercent method, of class ResultSet.
     */
    @Test
    public void testSetAndGetCorrectPercent() {
        System.out.println("getCorrectPercent");
        ResultSet instance = new ResultSet();
        double correctPsnt = 10.0;
        
        instance.setCorrectPercent(correctPsnt);
        
        double expResult = 10.0;
        double result = instance.getCorrectPercent();
        assertNotNull(instance.getCorrectPercent());
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of set and get Incorrect method, of class ResultSet.
     */
    @Test
    public void testSetAndGetIncorrect() {
        System.out.println("getIncorrect");
        ResultSet instance = new ResultSet();
        double incorrect = 0.0;
        
        instance.setIncorrect(incorrect);
        
        double expResult = 0.0;
        double result = instance.getIncorrect();
        assertNotNull(instance.getIncorrect());
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of set and get IncorrectPercent method, of class ResultSet.
     */
    @Test
    public void testSetAndGetIncorrectPercent() {
        System.out.println("getIncorrectPercent");
        ResultSet instance = new ResultSet();       
        double incorrectPsnt = 10.0;
        
        instance.setIncorrectPercent(incorrectPsnt);
        
        double expResult = 10.0;
        double result = instance.getIncorrectPercent();
        assertNotNull(instance.getIncorrectPercent());
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of set and get TotalNumInstances method, of class ResultSet.
     */
    @Test
    public void testSetAndGetTotalNumInstances() {
        System.out.println("getTotalNumInstances");
        ResultSet instance = new ResultSet();
        double total = 5.0;
        
        instance.setTotalNumInstances(total);
        
        double expResult = 0.0;
        double result = instance.getTotalNumInstances();
        assertNotNull(instance.getTotalNumInstances());
        assertEquals(expResult, result, 5.0);
    }

    /**
     * Test of set and get ConfusionMatrix method, of class ResultSet.
     */
    @Test
    public void testSetAndGetConfusionMatrix() {
        System.out.println("getConfusionMatrix");
        ResultSet instance = new ResultSet();
        double[][] matrix = new double[1][1];
        
        instance.setConfusionMatrix(matrix);
        
        double[][] expResult = new double[1][1];
        double[][] result = instance.getConfusionMatrix();
        assertNotNull(instance.getConfusionMatrix());
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of getPredictions method, of class ResultSet.
     */
    @Test
    public void testGetPredictions() {
        System.out.println("getPredictions");
        ResultSet instance = new ResultSet();
        ArrayList<PredictionSet> predictions = new ArrayList<>();
        
        instance.setPredictions(predictions);
        
        ArrayList<PredictionSet> expResult = new ArrayList<>();
        ArrayList<PredictionSet> result = instance.getPredictions();
        assertNotNull(instance.getPredictions());
        assertEquals(expResult, result);
    }

    /**
     * Test of generateQueryList method, of class ResultSet.
     */
    //Valid prediction set
    @Test
    public void testGenerateQueryList1() {
        System.out.println("generateQueryList");
        
        //Create prediction set
        PredictionSet setA = new PredictionSet(1, 1, 0, 0.5, 0.5, true);
        PredictionSet setB = new PredictionSet(1, 1, 0, 0.6, 0.4, true);
        PredictionSet setC = new PredictionSet(1, 1, 0, 0.7, 0.3, true);
        PredictionSet setD = new PredictionSet(1, 1, 1, 0.5, 0.5, true);
        PredictionSet setE = new PredictionSet(1, 1, 1, 0.4, 0.6, true);
        PredictionSet setF = new PredictionSet(1, 1, 1, 0.3, 0.7, true);
        ArrayList<PredictionSet> predictions = new ArrayList<>();
        predictions.add(setA);
        predictions.add(setB);
        predictions.add(setC);
        predictions.add(setD);
        predictions.add(setE);
        predictions.add(setF);
        
        double confidenceThreshold = 0.6;
        ResultSet instance = new ResultSet();
        instance.setPredictions(predictions);
        
        int expSize = 2;
        ArrayList result = instance.generateQueryList(confidenceThreshold);
        assertEquals(expSize, result.size());      
    }
    
    //Invalid prediction set
    @Test
    public void testGenerateQueryList2() {
        System.out.println("generateQueryList");
        
        double confidenceThreshold = 0.6;
        ResultSet instance = new ResultSet();
        
        ArrayList expResult = null;
        ArrayList result = instance.generateQueryList(confidenceThreshold);
        assertEquals(expResult, result);
    }
}
