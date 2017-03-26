package runner;

import listeners.StartupListenerTest;
import model.ResultSetTest;
import model.StarClassifierTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Nate
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(value={StarClassifierTest.class, ResultSetTest.class, StartupListenerTest.class})
public class TestSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
}
