package test.org.ukiuni.report.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.ukiuni.report.entity.Account;
import org.ukiuni.report.entity.News;
import org.ukiuni.report.service.AccountService;
import org.ukiuni.report.service.NewsService;
import org.ukiuni.report.service.ReportService;
import org.ukiuni.report.util.DBUtil;

import test.org.ukiuni.report.tools.DBTestUtil;

public class TestNewsService {
	private static final String DB_FACTORY_NAME = "org.ukiuni.report.unitTest";

	@Test
	public void testNewsLoad() {
		DBTestUtil.setUpDbWithXML(DBTestUtil.MODE_UNIT_TEST, "basicData.xml");
		NewsService newsService = new NewsService();
		newsService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		AccountService accountService = new AccountService();
		accountService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		ReportService reportService = new ReportService();
		reportService.dbUtil = DBUtil.create(DB_FACTORY_NAME);
		
		Account account = accountService.findByName("myName");
		Account account2 = accountService.findByName("myName2");
		Account account3 = accountService.findByName("myName3");

		accountService.follow(account3, account2.getId());
		accountService.follow(account2, account.getId());
		reportService.fold(account3, reportService.findByKey("e8fea4ff-e624-40b1-bf9d-357cead00f82"));
		reportService.create(account3);
		List<News> newsList = newsService.find(account);
		Assert.assertEquals(11, newsList.size());
	}
}
