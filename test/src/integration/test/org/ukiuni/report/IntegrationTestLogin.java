package integration.test.org.ukiuni.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
import org.apache.catalina.startup.Tomcat;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.ukiuni.report.entity.Account;
import org.ukiuni.report.util.DBUtil;

import test.org.ukiuni.report.tools.DBTestUtil;

import com.google.common.base.Function;

public class IntegrationTestLogin {
	private static final String DB_FACTORY_NAME = "org.ukiuni.report.integrationTest";
	private static final String HOST_NAME = "localhost";
	private static final String APPLICATION_NAME = "report";
	private static final int PORT = 18080;
	private static final int WAIT_FOR_TIME = 5000;
	private Server server;
	private WebDriver driver;

	@BeforeClass
	public static void beforeClass() {
		// INIT db if not exist.
		DBUtil dbUtil = DBUtil.create(DB_FACTORY_NAME);
		dbUtil.findAll(Account.class);
		DBUtil.closeAll();
	}

	@Before
	public void bootTomcat() throws LifecycleException, ServletException {
		System.setProperty(DBUtil.SYSTEM_ENV_FACTORYNAME_POSTFIX, ".integrationTest");
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_INTEGRATION_TEST, "oneAccountData.xml");
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(PORT);
		tomcat.setBaseDir("./test/server");
		tomcat.addWebapp("/" + APPLICATION_NAME, new File("./WebContent").getAbsolutePath());
		tomcat.start();
		server = tomcat.getServer();

		driver = driver();
	}

	public static void main(String[] args) throws LifecycleException, ServletException {
		new IntegrationTestLogin().bootTomcat();
	}

	@After
	public void shutdownTomcat() throws LifecycleException {
		if (null != server) {
			server.stop();
		}
		if (null != driver) {
			driver.close();
		}
		DBUtil.closeAll();
	}

	public String url(String... paths) {
		String uri = "http://" + HOST_NAME + ":" + PORT + "/" + APPLICATION_NAME;
		for (String path : paths) {
			uri += "/" + path;
		}
		if (0 == paths.length) {
			uri = uri + "/";
		}
		return uri;
	}

	public WebDriver driver() {
		return new FirefoxDriver();
	}

	public void waitFor(WebDriver driver, ExpectedCondition<?>... condition) {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_FOR_TIME);
		for (ExpectedCondition<?> expectedCondition : condition) {
			wait.until(expectedCondition);
		}
	}

	public void waitFor(WebDriver driver, Function<? super WebDriver, ?>... functions) {
		WebDriverWait wait = new WebDriverWait(driver, WAIT_FOR_TIME);
		for (Function<? super WebDriver, ?> func : functions) {
			wait.until(func);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateAccount() {
		driver.get(url());
		WebElement loginForm = driver.findElement(By.name("loginForm"));
		WebElement nameForm = loginForm.findElement(By.name("name"));
		WebElement passwordForm = loginForm.findElement(By.name("password"));
		WebElement loginButton = loginForm.findElement(By.cssSelector(".btn"));
		nameForm.sendKeys("myName");
		passwordForm.sendKeys("myPassword");
		loginButton.click();
		waitFor(driver, new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				WebElement element;
				if (null != (element = driver.findElement(By.className("btn"))) && "Edit Profile".equals(element.getText())) {
					return true;
				}
				return false;
			}
		});
		WebElement element;
		assertNotNull((element = driver.findElement(By.className("btn"))));
		assertEquals("Edit Profile", element.getText());
	}
}
