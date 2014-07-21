package test.org.ukiuni.report.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ukiuni.report.entity.Account;
import org.ukiuni.report.entity.Comment;
import org.ukiuni.report.entity.Fold;
import org.ukiuni.report.entity.Report;
import org.ukiuni.report.service.AccountService;
import org.ukiuni.report.service.ReportService;
import org.ukiuni.report.util.DBUtil;

import test.org.ukiuni.report.tools.DBTestUtil;

public class TestReportService {
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
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		ReportService reportService = new ReportService();
		reportService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String name = "myName";
		Account account = accountService.findByName(name);
		Report report = reportService.create(account);
		assertEquals(name, report.getAccount().getName());
		assertNotNull("key is not generated", report.getKey());
		assertNotNull("pk is not generated", report.getPk());
		assertNotNull("createdAt not generate", report.getCreatedAt());
		assertEquals(Report.STATUS_DRAFT, report.getStatus());
	}

	@Test
	public void testFindByAccount() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		ReportService reportService = new ReportService();
		reportService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String name = "myName";
		Account account = accountService.findByName(name);

		List<Report> reports = reportService.findByAccount(account);
		assertEquals(10, reports.size());
		for (Report report : reports) {
			assertEquals(name, report.getAccount().getName());
			assertNotNull("key is not generated", report.getKey());
			assertNotNull("pk is not generated", report.getPk());
			assertNotNull("createdAt not generate", report.getCreatedAt());
			assertEquals(Report.STATUS_DRAFT, report.getStatus());
		}
	}

	@Test
	public void testFind() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		ReportService reportService = new ReportService();
		reportService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		Report report = reportService.find(103, 2);
		assertNotNull("report is not finded", report);
		assertEquals(103, report.getPk().getId());
		assertEquals(2, report.getPk().getVersion());
		assertNotNull(Report.STATUS_DRAFT, report.getStatus());
		assertEquals("db9ec8e9-8039-41a8-bee7-fb3594db6060", report.getKey());
	}

	@Test
	public void testFindByKey() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		ReportService reportService = new ReportService();
		reportService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String key = "db9ec8e9-8039-41a8-bee7-fb3594db6060";
		Report report = reportService.findByKey(key);
		assertNotNull("report is not finded", report);
		assertEquals(103, report.getPk().getId());
		assertEquals(2, report.getPk().getVersion());
		assertEquals(Report.STATUS_DRAFT, report.getStatus());
		assertEquals(key, report.getKey());
	}

	@Test
	public void testUpate() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		ReportService reportService = new ReportService();
		reportService.dbUtil = DBUtil.create(DB_FACTORY_NAME);

		String key = "db9ec8e9-8039-41a8-bee7-fb3594db6060";
		String title = "myTitle";
		String content = "# report?\n* is tested.\n* and tested.";

		Report report = reportService.findByKey(key);

		report.setStatus(Report.STATUS_PUBLISHED);
		report.setTitle(title);
		report.setContent(content);

		reportService.update(report);

		report = reportService.findByKey(key);
		assertEquals(103, report.getPk().getId());
		assertEquals(3, report.getPk().getVersion());
		assertEquals(Report.STATUS_PUBLISHED, report.getStatus());
		assertEquals(key, report.getKey());
		assertEquals(title, report.getTitle());
		assertEquals(content, report.getContent());
		assertNotNull("update date is not generated.", report.getUpdatedAt());
	}

	@Test
	public void testDelete() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		ReportService reportService = new ReportService();
		reportService.dbUtil = DBUtil.create(DB_FACTORY_NAME);

		String key = "f150e2c4-f98b-4b65-ba7f-7672bacd4b29";

		Report report = reportService.findByKey(key);
		assertNotNull("report is not exist", report);
		reportService.delete(report);

		report = reportService.findByKey(key);
		assertNull("report is not deleted", report);
	}

	@Test
	public void testComment() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		ReportService reportService = new ReportService();
		reportService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);

		String name = "myName";
		Account account = accountService.findByName(name);
		String key = "e8fea4ff-e624-40b1-bf9d-357cead00f82";
		Report report = reportService.findByKey(key);
		String message = "this is test comment";
		reportService.comment(account, report, message);
		List<Comment> comments = reportService.findComments(key);
		assertEquals(1, comments.size());
		assertEquals(message, comments.get(0).getMessage());
		assertNotNull(message, comments.get(0).getCreatedAt());
	}

	@Test
	public void testCommentMany() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		ReportService reportService = new ReportService();
		reportService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);

		String name = "myName";
		Account account = accountService.findByName(name);
		String key = "e8fea4ff-e624-40b1-bf9d-357cead00f82";
		Report report = reportService.findByKey(key);
		for (int i = 0; i < 10; i++) {
			String message = "this is test comment" + i;
			reportService.comment(account, report, message);
		}
		List<Comment> comments = reportService.findComments(key);
		assertEquals(10, comments.size());
		for (int i = 0; i < 10; i++) {
			String message = "this is test comment" + i;
			assertEquals(message, comments.get(i).getMessage());
			assertNotNull(message, comments.get(i).getCreatedAt());
		}
	}

	@Test
	public void testUpdateComment() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		ReportService reportService = new ReportService();
		reportService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String key = "f150e2c4-f98b-4b65-ba7f-7672bacd4b29";
		List<Comment> comments = reportService.findComments(key);

		Comment comment = comments.get(0);
		String message = "this is updated comment";
		comment.setMessage(message);
		reportService.updateComment(comment);

		comments = reportService.findComments(key);
		comment = comments.get(0);

		assertEquals(message, comment.getMessage());
		assertNotNull("update at is not generated.", comment.getUpdatedAt());
	}

	@Test
	public void testDeleteComment() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		ReportService reportService = new ReportService();
		reportService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		String key = "f150e2c4-f98b-4b65-ba7f-7672bacd4b29";
		List<Comment> comments = reportService.findComments(key);
		int commentsSize = comments.size();
		Comment deletedComment = comments.get(0);
		long deletedId = deletedComment.getId();
		reportService.deleteComment(deletedComment);

		comments = reportService.findComments(key);

		assertEquals(commentsSize - 1, comments.size());
		boolean deletedCommentIsExist = false;
		for (Comment comment : comments) {
			if (comment.getId() == deletedId) {
				deletedCommentIsExist = true;
				break;
			}
		}
		assertFalse("deleted comment is exist.", deletedCommentIsExist);
	}

	@Test
	public void testFold() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		ReportService reportService = new ReportService();
		reportService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);

		String name = "myName2";
		Account account = accountService.findByName(name);
		String key = "e8fea4ff-e624-40b1-bf9d-357cead00f82";
		Report report = reportService.findByKey(key);

		long foldedToReportCount = reportService.countFolded(report);
		reportService.fold(account, report);

		assertEquals(foldedToReportCount + 1, reportService.countFolded(report));
	}

	@Test
	public void testUnfold() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		ReportService reportService = new ReportService();
		reportService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);

		String name = "myName";
		Account account = accountService.findByName(name);
		String key = "e8fea4ff-e624-40b1-bf9d-357cead00f82";
		Report report = reportService.findByKey(key);

		long foldedToReportCount = reportService.countFolded(report);
		reportService.unfold(account, report);

		assertEquals(foldedToReportCount - 1, reportService.countFolded(report));
	}

	@Test
	public void testFindFolds() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		ReportService reportService = new ReportService();
		reportService.dbUtil = DBUtil.create(DB_FACTORY_NAME);

		String reportKey = "e8fea4ff-e624-40b1-bf9d-357cead00f82";
		List<Fold> folds = reportService.findFolds(reportKey);

		assertEquals(1, folds.size());
	}
}
