package test.org.ukiuni.report.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ukiuni.report.entity.Account;
import org.ukiuni.report.entity.AccountAccessKey;
import org.ukiuni.report.service.AccountService;
import org.ukiuni.report.util.DBUtil;

import test.org.ukiuni.report.tools.DBTestUtil;

public class TestAccountService {
	private static final String DB_FACTORY_NAME = "org.ukiuni.report.unitTest";

	@BeforeClass
	public static void beforeClass() {
		// INIT db if not exist.
		DBUtil dbUtil = DBUtil.create(DB_FACTORY_NAME);
		dbUtil.findAll(Account.class);
		DBUtil.closeAll();
	}

	@Before
	public void before() throws Exception {
	}

	@After
	public void after() throws Exception {
		DBUtil.closeAll();
	}

	@Test
	public void testSave() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "emptyData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String name = "myName";
		String password = "myPassword";
		String mail = "myMail@example.com";
		Account account = accountService.create(name, mail, password);
		assertEquals(name, account.getName());
		assertEquals(mail, account.getMail());
		assertNotNull("password-hashed not generate", account.getPasswordHashed());
		assertNotNull("createdAt not generate", account.getCreatedAt());
	}

	@Test
	public void testUpdate() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "oneAccountData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String name = "myName";
		Account account = accountService.findByName(name);
		String replacedMail = "replaced@example.com";
		account.setMail(replacedMail);
		accountService.update(account);
		Account reloadedAccount = accountService.findByName(name);
		assertEquals(name, reloadedAccount.getName());
		assertEquals(replacedMail, reloadedAccount.getMail());
	}

	@Test
	public void testGenerateAccessKey() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "oneAccountData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String name = "myName";
		Account account = accountService.findByName(name);
		AccountAccessKey accessKey = accountService.generateAccessKey(account);
		assertNotNull(accessKey.getCreatedAt());
		assertNotNull(accessKey.getHash());
		assertEquals(AccountAccessKey.Status.CREATED, accessKey.getStatus());
	}

	@Test
	public void testExistsName() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "oneAccountData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String name = "myName";
		assertTrue(accountService.existsName(name));
	}

	@Test
	public void testNotExistsName() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "oneAccountData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String name = "NotMyName";
		assertFalse(accountService.existsName(name));
	}

	@Test
	public void testFindByName() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "oneAccountData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String name = "myName";
		Account account = accountService.findByName(name);
		assertNotNull(account);
		assertEquals(name, account.getName());
	}

	@Test
	public void testExistsMail() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "oneAccountData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String mail = "myMail@example.com";
		assertTrue(accountService.existsMail(mail));
	}

	@Test
	public void testNotExistsMail() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "oneAccountData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String mail = "notMyMail@example.com";
		assertFalse(accountService.existsMail(mail));
	}

	@Test
	public void testFindByAccessKey() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "oneAccountData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String accountAccessKey = "4e76a3d4-360d-4d92-b2eb-78674098d824";
		Account account = accountService.findByAccessKey(accountAccessKey);
		assertNotNull(account);
		assertEquals("myName", account.getName());
	}

	@Test
	public void testFindByNotExistAccessKey() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "oneAccountData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String accountAccessKey = "notExist";
		Account account = accountService.findByAccessKey(accountAccessKey);
		assertNull(account);
	}
}
