package org.ukiuni.report.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.commons.beanutils.BeanUtils;
import org.ukiuni.report.entity.Account;
import org.ukiuni.report.entity.Report;
import org.ukiuni.report.entity.Report.ReportPK;
import org.ukiuni.report.entity.Report.Status;
import org.ukiuni.report.util.DBUtil;
import org.ukiuni.report.util.DBUtil.Work;

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
		dbUtil.execute(new Work<Report>() {
			@Override
			public Report execute(EntityManager em) {
				Report registedReport = em.find(Report.class, report.getPk());
				try {
					BeanUtils.copyProperties(registedReport, report);
				} catch (Exception e) {
					new RuntimeException(e);
				}
				return null;
			}
		});
	}
}
