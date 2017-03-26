package model;

import static org.hamcrest.CoreMatchers.instanceOf;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.rules.ExpectedException;
import static org.mockito.Mockito.mock;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.core.WekaException;

/**
 *
 * @author Nate
 */
public class StarClassifierTest {
    
    public StarClassifierTest() {
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
     * Test of set and get Classifier method, of class StarClassifier.
     */
    @Test
    public void testSetAndGetClassifier() {
        System.out.println("setClassifier");
        StarClassifier instance = new StarClassifier();
        FilteredClassifier classifier = mock(FilteredClassifier.class);
        
        instance.setClassifier(classifier);
        
        assertNotNull(instance.getClassifier());
        assertThat(instance.getClassifier(), instanceOf(FilteredClassifier.class));
    }

    /**
     * Test of set and get Evaluation method, of class StarClassifier.
     */
    @Test
    public void testSetAndGetEvaluation() {
        System.out.println("getEvaluation");
        StarClassifier instance = new StarClassifier();
        Evaluation evaluation = mock(Evaluation.class);
       
        instance.setEvaluation(evaluation);
        
        assertNotNull(instance.getEvaluation());
        assertThat(instance.getEvaluation(), instanceOf(Evaluation.class));
    }

    /**
     * Test of set and get ResultSet method, of class StarClassifier.
     */
    @Test
    public void testSetAndGetResultSet() {
        System.out.println("getResultSet");
        StarClassifier instance = new StarClassifier();
        ResultSet result = mock(ResultSet.class);
        
        instance.setResultSet(result);
        
        assertNotNull(instance.getResultSet());
        assertThat(instance.getResultSet(), instanceOf(ResultSet.class));
    }

    /**
     * Tests of buildClassifier method, of class StarClassifier.
     */
    //Valid data
    @Test
    public void testBuildClassifier1() {
        System.out.println("buildClassifier");        
        StarClassifier instance = new StarClassifier();
        
        Instances mockData = mock(Instances.class);
        instance.buildClassifier(mockData);
        assertNotNull(instance.getClassifier());
        assertThat(instance.getClassifier(), instanceOf(FilteredClassifier.class));        
    }

    //Invalid data
    @Test
    public void testBuildClassifier2() {
        System.out.println("buildClassifier");        
        StarClassifier instance = new StarClassifier();
        
        Instances mockData = null;        
        ExpectedException exception = ExpectedException.none();
        exception.expect(WekaException.class);
        instance.buildClassifier(mockData);        
    }
    
    /**
     * Tests of evaluateClassifier method, of class StarClassifier.
     */
    //Valid classifier and valid data
    @Test
    public void testEvaluateClassifier1() {
        System.out.println("evaluateClassifier");
        StarClassifier instance = new StarClassifier();
        
        FilteredClassifier mockClassifier = mock(FilteredClassifier.class);
        instance.setClassifier(mockClassifier);
        
        Instances mockData = mock(Instances.class);
        boolean anonymiseFlag = true;
        
        boolean expResult = true;
        boolean result = instance.evaluateClassifier(mockData, anonymiseFlag);
        assertEquals(expResult, result);
    }
    
    //Invalid data
    @Test
    public void testEvaluateClassifier2() {
        System.out.println("evaluateClassifier");
        StarClassifier instance = new StarClassifier();
        
        FilteredClassifier mockClassifier = mock(FilteredClassifier.class);
        instance.setClassifier(mockClassifier);
        
        Instances mockData = null;
        boolean anonymiseFlag = true;
        
        ExpectedException exception = ExpectedException.none();
        exception.expect(NullPointerException.class);
        instance.evaluateClassifier(mockData, anonymiseFlag);       
    }
    
    //Invalid classifier
    @Test
    public void testEvaluateClassifier3() {
        System.out.println("evaluateClassifier");
        StarClassifier instance = new StarClassifier();
        
        Instances mockData = mock(Instances.class);
        boolean anonymiseFlag = true;
        
        boolean expResult = false;
        boolean result = instance.evaluateClassifier(mockData, anonymiseFlag);
        assertEquals(expResult, result);       
    }
    
    /**
     * Test of saveResults method, of class StarClassifier.
     */
    //Valid data and invalid evaluation
    @Test
    public void testSaveResults1() throws Exception {
        System.out.println("saveResults");
        StarClassifier instance = new StarClassifier();
        
        Instances mockData = mock(Instances.class);
        String resultSetName = "Test";

        boolean expResult = false;
        boolean result = instance.saveResults(mockData, resultSetName);
        assertEquals(expResult, result);
    }
    
    //Invalid data and invalid evaluation
    @Test
    public void testSaveResults2() throws Exception {
        System.out.println("saveResults");
        StarClassifier instance = new StarClassifier();
        
        Instances mockData = null;
        String resultSetName = "Test";

        boolean expResult = false;
        boolean result = instance.saveResults(mockData, resultSetName);
        assertEquals(expResult, result);
    }
}
