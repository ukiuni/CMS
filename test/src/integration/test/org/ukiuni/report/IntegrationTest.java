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
	private static final String DB_FACTORY_NAME = "org.ukiuni.report";
	private static final String HOST_NAME = "localhost";
	private static final String APPLICATION_NAME = "report";
	private static final int PORT = 18080;
	private static final int WAIT_FOR_TIME = 5000;
	private Server server;
	private WebDriver driver;
	private Context context;

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(DBUtil.SYSTEM_ENV_FACTORYNAME_POSTFIX, ".integrationTest");
		// INIT db if not exist.
		DBUtil dbUtil = DBUtil.create(DB_FACTORY_NAME);
		dbUtil.findAll(Account.class);
		DBUtil.closeAll();
	}

	@Before
	public void bootTomcat() throws LifecycleException, ServletException {
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
	}

	@Test
	public void testCreateAccount() {
		driver.get(url());
		String name = "myCreateAccountName";
		String password = "myCreateAccountPassword";
		String mail = "myCreateAccount@example.com";
		WebElement loginForm = driver.findElement(By.name("accountCreateForm"));
		WebElement nameForm = loginForm.findElement(By.name("name"));
		WebElement passwordForm = loginForm.findElement(By.name("password"));
		WebElement mailForm = loginForm.findElement(By.name("mail"));
		WebElement loginButton = loginForm.findElement(By.className("btn"));
		nameForm.sendKeys(name);
		passwordForm.sendKeys(password);
		mailForm.sendKeys(mail);
		loginButton.click();

		waitForMyPage();

		logout();

		login(name, password);
		waitForMyPage();

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLoginError() {
		driver.get(url());
		waitForTopPage();
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

		logout();

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

	private void logout() {
		driver.findElement(By.className("dropdown-toggle")).click();
		waitForElementById("logoutButton");
		clickWithId("logoutButton");
		waitForTopPage();
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

		logout();

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

		waitForElementByTag("input");
		while (!"myTitle7".equals(driver.findElement(By.tagName("input")).getAttribute("value"))) {
			sleep(1000);
		}
		WebElement titleInput = driver.findElement(By.tagName("input"));
		WebElement contentArea = driver.findElement(By.tagName("textarea"));

		String newTitle = "myIntegrationTestTitle";
		String myContent = "* myContent";
		titleInput.clear();
		contentArea.clear();

		titleInput.sendKeys(newTitle);
		contentArea.sendKeys(myContent);
		waitForElementByClassNameAndValue("btn", "save as draft");
		driver.findElement(By.id("setActionButton")).click();
		waitForElementById("setDraftButton");
		driver.findElement(By.id("doActionButton")).click();

		waitForMyPage();

		List<WebElement> reports = driver.findElements(By.className("reportRow"));
		assertEquals(reportSize, reports.size());
		assertEquals(newTitle, reports.get(0).findElement(By.tagName("h4")).getText());
	}

	@Test
	public void testFold() {
		driver.get(url());
		login("myName", "myPassword");
		waitForMyPage();

		driver.get(url("report?key=e8fea4ff-e624-40b1-bf9d-357cead00f82"));
		waitForContentPage();

		WebElement foldButton = null;
		waitForElementByClassNameAndValue("btn", "Fold");
		for (WebElement btn : driver.findElements(By.className("btn"))) {
			if ("Fold".equals(btn.getText())) {
				foldButton = btn;
				break;
			}
		}
		foldButton.click();
		waitForElementByClassNameAndValue("btn", "Unfold");

		foldButton = null;
		waitForElementByClassNameAndValue("btn", "Unfold");
		for (WebElement btn : driver.findElements(By.className("btn"))) {
			if ("Unfold".equals(btn.getText())) {
				foldButton = btn;
				break;
			}
		}
		foldButton.click();
		waitForElementByClassNameAndValue("btn", "Fold");
	}

	@Test
	public void testShowReportDirectly() {
		driver.get(url("report?key=b564857c-b762-4eaf-a95e-299fa41e25b9"));
		waitForContentPage();
	}

	@Test
	public void testFoldStartsWithNotLogin() {
		driver.get(url("report?key=b564857c-b762-4eaf-a95e-299fa41e25b9"));
		waitForContentPage();

		WebElement foldButton = null;
		for (WebElement btn : driver.findElements(By.className("btn"))) {
			if ("Fold".equals(btn.getText())) {
				foldButton = btn;
				break;
			}
		}
		foldButton.click();
		waitForLoginPage();

		loginWithoutWait("myName", "myPassword");
		waitForContentPage();
		click("Fold");
		waitForElementByClassNameAndValue("btn", "Unfold");
	}

	@Test
	public void testComment() {
		driver.get(url());
		login("myName", "myPassword");
		waitForMyPage();
		while (driver.findElements(By.className("reportRow")).size() < 5) {
			sleep(1000);
		}
		driver.findElements(By.className("reportRow")).get(3).findElement(By.className("label")).click();

		waitForContentPage();

		final int commentCount = driver.findElements(By.className("comment")).size();

		String comment = "Integration Test Comment";
		WebElement contentArea = driver.findElement(By.tagName("textarea"));
		contentArea.sendKeys(comment);

		click("Comment");

		waitFor(driver, new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver arg0) {
				return driver.findElements(By.className("comment")).size() > commentCount;
			}
		});
		List<WebElement> comments = driver.findElements(By.className("message"));
		assertEquals(comment, comments.get(comments.size() - 1).getText());
	}

	private void sleep(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testCommentStartsWithNotLogin() {
		driver.get(url("report?key=b564857c-b762-4eaf-a95e-299fa41e25b9"));
		waitForContentPage();

		click("Login or Create Account");

		waitForLoginPage();

		loginWithoutWait("myName", "myPassword");
		waitForContentPage();

		final int commentCount = driver.findElements(By.className("comment")).size();

		String comment = "Integration Test Comment";
		WebElement contentArea = driver.findElement(By.tagName("textarea"));
		contentArea.sendKeys(comment);

		click("Comment");

		waitFor(driver, new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver arg0) {
				return driver.findElements(By.className("comment")).size() > commentCount;
			}
		});
		List<WebElement> comments = driver.findElements(By.className("message"));
		assertEquals(comment, comments.get(comments.size() - 1).getText());
	}

	@Test
	public void testDeleteComment() {
		testComment();
		int commentSize = driver.findElements(By.className("message")).size();
		driver.findElement(By.className("glyphicon-remove-circle")).click();
		waitForElementByClassNameAndValue("btn", "Delete");
		click("Delete");
		assertEquals(commentSize - 1, driver.findElements(By.className("message")).size());
	}

	@Test
	public void testUpdateComment() {
		testComment();
		int commentSize = driver.findElements(By.className("message")).size();
		driver.findElement(By.className("glyphicon-pencil")).click();
		waitForElementByClassNameAndValue("btn", "Update");
		String newComment = "updatedComment";
		driver.findElements(By.id("updateCommentTextArea")).get(0).sendKeys(newComment);
		click("Update");
		assertEquals(commentSize, driver.findElements(By.className("message")).size());
	}

	@Test
	public void testFollow() {
		driver.get(url());
		login("myName", "myPassword");
		waitForMyPage();

		driver.get(url("report?key=e8fea4ff-e624-40b1-bf9d-357cead00f82"));
		waitForContentPage();

		WebElement button = null;
		waitForElementByClassNameAndValue("btn", "Follow");
		for (WebElement btn : driver.findElements(By.className("btn"))) {
			if ("Follow".equals(btn.getText())) {
				button = btn;
				break;
			}
		}
		button.click();
		waitForElementByClassNameAndValue("btn", "Unfollow");

		button = null;
		waitForElementByClassNameAndValue("btn", "Unfollow");
		for (WebElement btn : driver.findElements(By.className("btn"))) {
			if ("Unfollow".equals(btn.getText())) {
				button = btn;
				break;
			}
		}
		button.click();
		waitForElementByClassNameAndValue("btn", "Follow");
	}

	@Test
	public void testShowNewsTab() throws InterruptedException {
		driver.get(url());
		waitForTopPage();
		login("myName", "myPassword");
		waitForMyPage();
		driver.findElement(By.id("newsTabHeader")).findElement(By.tagName("a")).click();
		waitForElement(By.className("news"));
		List<WebElement> newsElements = driver.findElements(By.className("news"));
		assertEquals(3, newsElements.size());
		for (WebElement webElement : newsElements) {
			assertTrue("news is unvisible", webElement.isDisplayed());
		}
	}

	@Test
	public void testShowFoldTab() throws InterruptedException {
		driver.get(url());
		waitForTopPage();
		login("myName", "myPassword");
		waitForMyPage();
		driver.findElement(By.id("foldsTabHeader")).findElement(By.tagName("a")).click();
		waitForElement(By.className("myfold"));
		List<WebElement> newsElements = driver.findElements(By.className("myfold"));
		assertEquals(1, newsElements.size());
		for (WebElement webElement : newsElements) {
			assertTrue("myFold is unvisible", webElement.isDisplayed());
		}
	}

	private void login(String name, String password) {
		waitForTopPage();
		loginWithoutWait(name, password);
		waitForMyPage();
	}

	private void loginWithoutWait(String name, String password) {
		WebElement loginForm = driver.findElement(By.name("loginForm"));
		WebElement nameForm = loginForm.findElement(By.name("name"));
		WebElement passwordForm = loginForm.findElement(By.name("password"));
		WebElement loginButton = loginForm.findElement(By.className("btn"));
		nameForm.sendKeys(name);
		passwordForm.sendKeys(password);
		loginButton.click();
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

	private void waitForContentPage() {
		waitForElementByClassNameAndValue("btn", "Fold");
	}

	private void waitForLoginPage() {
		waitForElementByClassNameAndValue("btn", "Login");
	}

	@SuppressWarnings("unchecked")
	private void waitForElementByTag(final String tagName) {
		waitFor(driver, new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					List<WebElement> elements = driver.findElements(By.tagName(tagName));
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
	private void waitForElement(final By by) {
		waitFor(driver, new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				try {
					List<WebElement> elements = driver.findElements(by);
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

	private void waitForElementById(final String id) {
		waitForElement(By.id(id));
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
