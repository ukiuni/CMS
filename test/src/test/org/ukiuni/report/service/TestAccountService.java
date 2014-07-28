package test.org.ukiuni.report.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ukiuni.report.entity.Account;
import org.ukiuni.report.entity.AccountAccessKey;
import org.ukiuni.report.entity.Fold;
import org.ukiuni.report.entity.Follow;
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

	@After
	public void after() throws Exception {
		DBUtil.closeAll();
	}

	@Test
	public void testCreate() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "emptyData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String name = "myName4";
		String password = "myPassword";
		String mail = "myMail4@example.com";
		Account account = accountService.create(name, mail, password);
		assertEquals(name, account.getName());
		assertEquals(mail, account.getMail());
		assertNotNull("password-hashed is not generated", account.getPasswordHashed());
		assertNotNull("createdAt is not generated", account.getCreatedAt());
	}

	@Test
	public void testUpdate() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
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
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
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
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String name = "myName";
		assertTrue(accountService.existsName(name));
	}

	@Test
	public void testNotExistsName() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String name = "NotMyName";
		assertFalse(accountService.existsName(name));
	}

	@Test
	public void testFindByName() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String name = "myName";
		Account account = accountService.findByName(name);
		assertNotNull(account);
		assertEquals(name, account.getName());
	}

	@Test
	public void testExistsMail() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String mail = "myMail@example.com";
		assertTrue(accountService.existsMail(mail));
	}

	@Test
	public void testNotExistsMail() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String mail = "notMyMail@example.com";
		assertFalse(accountService.existsMail(mail));
	}

	@Test
	public void testFindByAccessKey() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String accountAccessKey = "4e76a3d4-360d-4d92-b2eb-78674098d824";
		Account account = accountService.findByAccessKey(accountAccessKey);
		assertNotNull(account);
		assertEquals("myName", account.getName());
	}

	@Test
	public void testFindByNotExistAccessKey() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String accountAccessKey = "notExist";
		Account account = accountService.findByAccessKey(accountAccessKey);
		assertNull(account);
	}

	@Test
	public void testFindFolds() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);

		String name = "myName";
		Account account = accountService.findByName(name);
		List<Fold> folds = accountService.findFolds(account);

		assertEquals(1, folds.size());
	}

	@Test
	public void testFollow() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		Account account = accountService.findByName("myName");
		Account account2 = accountService.findByName("myName2");
		int followersCount = accountService.findFollower(account2).size();
		accountService.follow(account, account2.getId());
		List<Follow> follower = accountService.findFollower(account2);

		assertEquals(followersCount + 1, follower.size());
	}

	@Test
	public void testUnfollow() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		Account account = accountService.findByName("myName");
		Account account2 = accountService.findByName("myName3");
		int followersCount = accountService.findFollower(account2).size();
		accountService.unfollow(account, account2.getId());
		List<Follow> follower = accountService.findFollower(account2);

		assertEquals(followersCount - 1, follower.size());
	}

	@Test
	public void testfollowing() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		Account account = accountService.findByName("myName");
		Account account2 = accountService.findByName("myName2");
		Account account3 = accountService.findByName("myName3");

		assertTrue("shuld be follow", accountService.following(account, account3));
		assertFalse("shuld not be follow", accountService.following(account, account2));
	}

	@Test
	public void testloadFold() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		Account account = accountService.findByName("myName");
		List<Fold> folds = accountService.findFolds(account);
		assertEquals(1, folds.size());
	}
}
