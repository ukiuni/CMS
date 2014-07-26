package org.ukiuni.report.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ukiuni.report.entity.Account;
import org.ukiuni.report.entity.News;
import org.ukiuni.report.util.DBUtil;
import org.ukiuni.report.util.StreamUtil;

public class NewsService {
	public DBUtil dbUtil = DBUtil.create("org.ukiuni.report");

	public List<News> find(Account account) {
		List<Object[]> queryResult = dbUtil.findListWithNativeQuery(StreamUtil.toString(NewsService.class.getResourceAsStream("./sql/findNews.sql")), account.getId(), account.getId(), account.getId(), account.getId(), account.getId());
		List<News> newsList = new ArrayList<News>();
		for (Object[] objects : queryResult) {
			newsList.add(new News((String) objects[0], (Long) objects[1], (String) objects[2], (String) objects[3], (String) objects[4], (String) objects[5], (Long) objects[6], (String) objects[7], (String) objects[8], ((Date) objects[9])));
		}
		return newsList;
	}
}
