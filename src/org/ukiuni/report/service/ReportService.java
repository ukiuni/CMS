package org.ukiuni.report.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.NoResultException;

import org.ukiuni.report.entity.Account;
import org.ukiuni.report.entity.Report;
import org.ukiuni.report.entity.Report.ReportPK;
import org.ukiuni.report.entity.Report.Status;
import org.ukiuni.report.util.DBUtil;

public class ReportService {
	public DBUtil dbUtil = DBUtil.create("org.ukiuni.report");

	public List<Report> loadByAccount(Account account) {
		return dbUtil.findList(Report.class, "account", account, "updatedAt");
	}

	public Report find(long id, long version) {
		ReportPK reportPK = new ReportPK();
		reportPK.setId(id);
		reportPK.setVersion(version);
		try {
			return dbUtil.find(Report.class, reportPK);
		} catch (NoResultException e) {
			return null;
		}
	}

	public Report create(Account account) {
		Report report = new Report();
		report.setCreatedAt(new Date());
		report.setKey(UUID.randomUUID().toString());
		report.setStatus(Status.DRAFT);
		report.setAccount(account);
		ReportPK reportPK = new ReportPK();
		reportPK.setVersion(1);
		report.setPk(reportPK);
		dbUtil.persist(report);
		return report;
	}

	public Report findByKey(String key) {
		return dbUtil.findSingleEquals(Report.class, "key", key);
	}

	public void update(final Report report) {
		report.setUpdatedAt(new Date());
		dbUtil.update(report.getPk(), report);
	}

	public void delete(Report report) {
		dbUtil.delete(Report.class, report.getPk());

	}
}
