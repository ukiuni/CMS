package test.org.ukiuni.report.service;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TestAccountService.class, TestReportService.class, TestNewsService.class })
public class AllTests {

}
