
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ test.org.ukiuni.report.service.AllTests.class, integration.test.org.ukiuni.report.AllTests.class })
public class AllTests {

}
