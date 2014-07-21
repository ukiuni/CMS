package integration.test.org.ukiuni.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.catalina.Context;
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

public class IntegrationTest {
	private static final String DB_FACTORY_NAME = "org.ukiuni.report.integrationTest";
	private static final String HOST_NAME = "localhost";
	private static final String APPLICATION_NAME = "report";
	private static final int PORT = 18080;
	private static final int WAIT_FOR_TIME = 5000;
	private Server server;
	private WebDriver driver;
	private Context context;

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
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_INTEGRATION_TEST, "basicData.xml");
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(PORT);
		tomcat.setBaseDir("./test/server");
		context = tomcat.addWebapp("/" + APPLICATION_NAME, new File("./WebContent").getAbsolutePath());

		tomcat.start();
		server = tomcat.getServer();

		driver = driver();
	}

	public static void main(String[] args) throws LifecycleException, ServletException {
		new IntegrationTest().bootTomcat();
	}

	@After
	public void shutdownTomcat() throws LifecycleException {
		if (null != context) {
			context.stop();
			context.destroy();
		}
		if (null != server) {
			server.stop();
			server.destroy();
			server.await();
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

	@Test
	public void testLogin() {
		driver.get(url());

		login("myName", "myPassword");
		waitForMyPage();
		
		WebElement element;
		assertNotNull((element = driver.findElement(By.className("btn"))));
		assertEquals("Edit Profile", element.getText());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoginError() {
		driver.get(url());
		WebElement loginForm = driver.findElement(By.name("loginForm"));
		WebElement nameForm = loginForm.findElement(By.name("name"));
		WebElement passwordForm = loginForm.findElement(By.name("password"));
		WebElement loginButton = loginForm.findElement(By.cssSelector(".btn"));
		nameForm.sendKeys("myName");
		passwordForm.sendKeys("notMyPassword");
		loginButton.click();
		waitFor(driver, new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				WebElement element;
				return (null != (element = driver.findElement(By.id("alert"))) && element.isDisplayed());
			}
		});
		assertTrue(driver.findElement(By.id("alert")).isDisplayed());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEditProfile() {
		driver.get(url());
		login("myName", "myPassword");

		click("Edit Profile");
		waitForEditProfilePage();

		WebElement profileNameForm = driver.findElement(By.id("inputLoginName"));
		WebElement profilePasswordForm = driver.findElement(By.id("inputPassword"));
		WebElement profileMailForm = driver.findElement(By.id("inputEmail"));
		WebElement profileFullNameForm = driver.findElement(By.id("inputFullName"));
		WebElement profileProfileForm = driver.findElement(By.id("textareaProfile"));

		String myName = "myNewName";
		String myMail = "myNewEmail@example.com";
		String myPassword = "myNewPassword";
		String myFullName = "myNewFullName";
		String myProfile = "myNewProfile";
		profileNameForm.clear();
		profilePasswordForm.clear();
		profileMailForm.clear();
		profileFullNameForm.clear();
		profileProfileForm.clear();
		profileNameForm.sendKeys(myName);
		profilePasswordForm.sendKeys(myPassword);
		profileMailForm.sendKeys(myMail);
		profileFullNameForm.sendKeys(myFullName);
		profileProfileForm.sendKeys(myProfile);

		click("Save");
		waitForMyPage();

		click("Edit Profile");
		waitForEditProfilePage();

		assertEquals(myName, driver.findElement(By.id("inputLoginName")).getAttribute("value"));
		assertEquals(myMail, driver.findElement(By.id("inputEmail")).getAttribute("value"));
		assertEquals(myFullName, driver.findElement(By.id("inputFullName")).getAttribute("value"));
		assertEquals(myProfile, driver.findElement(By.id("textareaProfile")).getAttribute("value"));

		driver.findElement(By.className("dropdown-toggle")).click();
		waitForElementById("logoutButton");
		clickWithId("logoutButton");
		waitForTopPage();

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
				return (null != (element = driver.findElement(By.id("alert"))) && element.isDisplayed());
			}
		});
		assertTrue(driver.findElement(By.id("alert")).isDisplayed());
	}

	@Test
	public void testEditProfileWithoutPassword() {
		driver.get(url());
		login("myName", "myPassword");

		click("Edit Profile");
		waitForEditProfilePage();

		WebElement profileNameForm = driver.findElement(By.id("inputLoginName"));
		WebElement profileMailForm = driver.findElement(By.id("inputEmail"));
		WebElement profileFullNameForm = driver.findElement(By.id("inputFullName"));
		WebElement profileProfileForm = driver.findElement(By.id("textareaProfile"));

		String myName = "myNewName";
		String myMail = "myNewEmail@example.com";
		String myFullName = "myNewFullName";
		String myProfile = "myNewProfile";
		profileNameForm.clear();
		profileMailForm.clear();
		profileFullNameForm.clear();
		profileProfileForm.clear();
		profileNameForm.sendKeys(myName);
		profileMailForm.sendKeys(myMail);
		profileFullNameForm.sendKeys(myFullName);
		profileProfileForm.sendKeys(myProfile);

		click("Save");
		waitForMyPage();

		click("Edit Profile");
		waitForEditProfilePage();

		assertEquals(myName, driver.findElement(By.id("inputLoginName")).getAttribute("value"));
		assertEquals(myMail, driver.findElement(By.id("inputEmail")).getAttribute("value"));
		assertEquals(myFullName, driver.findElement(By.id("inputFullName")).getAttribute("value"));
		assertEquals(myProfile, driver.findElement(By.id("textareaProfile")).getAttribute("value"));

		driver.findElement(By.className("dropdown-toggle")).click();
		waitForElementById("logoutButton");
		clickWithId("logoutButton");
		waitForTopPage();

		WebElement loginForm = driver.findElement(By.name("loginForm"));
		WebElement nameForm = loginForm.findElement(By.name("name"));
		WebElement passwordForm = loginForm.findElement(By.name("password"));
		WebElement loginButton = loginForm.findElement(By.cssSelector(".btn"));
		nameForm.sendKeys(myName);
		passwordForm.sendKeys("myPassword");
		loginButton.click();

		WebElement element;
		assertNotNull((element = driver.findElement(By.className("btn"))));
		assertEquals("Edit Profile", element.getText());
	}

	@Test
	public void testCreateReport() {
		driver.get(url());
		login("myName", "myPassword");

		int reportSize = driver.findElements(By.className("reportRow")).size();

		click("Create New Report");
		waitForEditContentPage();

		WebElement titleInput = driver.findElement(By.tagName("input"));
		WebElement contentArea = driver.findElement(By.tagName("textarea"));

		String newTitle = "myIntegrationTestTitle";
		String myContent = "* myContent";

		titleInput.sendKeys(newTitle);
		contentArea.sendKeys(myContent);

		driver.findElement(By.id("setActionButton")).click();
		waitForElementById("setDraftButton");
		driver.findElement(By.id("doActionButton")).click();

		waitForMyPage();

		List<WebElement> reports = driver.findElements(By.className("reportRow"));
		assertEquals(reportSize + 1, reports.size());
		assertEquals(newTitle, reports.get(0).findElement(By.tagName("h4")).getText());
	}

	@Test
	public void testUpdateReport() {
		driver.get(url());
		login("myName", "myPassword");

		int reportSize = driver.findElements(By.className("reportRow")).size();

		driver.findElements(By.className("reportRow")).get(0).click();
		waitForEditContentPage();

		WebElement titleInput = driver.findElement(By.tagName("input"));
		WebElement contentArea = driver.findElement(By.tagName("textarea"));

		String newTitle = "myIntegrationTestTitle";
		String myContent = "* myContent";

		titleInput.clear();
		contentArea.clear();
		
		titleInput.sendKeys(newTitle);
		contentArea.sendKeys(myContent);

		driver.findElement(By.id("setActionButton")).click();
		waitForElementById("setDraftButton");
		driver.findElement(By.id("doActionButton")).click();

		waitForMyPage();

		List<WebElement> reports = driver.findElements(By.className("reportRow"));
		assertEquals(reportSize, reports.size());
		assertEquals(newTitle, reports.get(0).findElement(By.tagName("h4")).getText());
	}

	private void login(String name, String password) {
		WebElement loginForm = driver.findElement(By.name("loginForm"));
		WebElement nameForm = loginForm.findElement(By.name("name"));
		WebElement passwordForm = loginForm.findElement(By.name("password"));
		WebElement loginButton = loginForm.findElement(By.cssSelector(".btn"));
		nameForm.sendKeys(name);
		passwordForm.sendKeys(password);
		loginButton.click();
		waitForMyPage();
	}

	private void clickWithId(String id) {
		driver.findElement(By.id(id)).click();
	}

	private void click(String value) {
		for (WebElement element : driver.findElements(By.className("btn"))) {
			if (value.equals(element.getText())) {
				element.click();
				break;
			}
		}
	}

	private void waitForEditProfilePage() {
		waitForElementByClassNameAndValue("btn", "Save");
	}

	private void waitForEditContentPage() {
		waitForElementById("doActionButton");
	}

	@SuppressWarnings("unchecked")
	private void waitForElementById(final String id) {
		waitFor(driver, new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					List<WebElement> elements = driver.findElements(By.id(id));
					for (WebElement element : elements) {
						if (null != (element) && element.isDisplayed()) {
							return true;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void waitForElementByClassNameAndValue(final String className, final String value) {
		waitFor(driver, new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					List<WebElement> elements = driver.findElements(By.className(className));
					for (WebElement element : elements) {
						if (value.equals(element.getText()) && element.isDisplayed()) {
							return true;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
		});
	}

	private void waitForMyPage() {
		waitForElementByClassNameAndValue("btn", "Edit Profile");
	}

	private void waitForTopPage() {
		waitForElementByClassNameAndValue("btn", "Login");
	}
}
