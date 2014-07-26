package org.ukiuni.report.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.ukiuni.report.entity.Account;
import org.ukiuni.report.entity.Comment;
import org.ukiuni.report.entity.Fold;
import org.ukiuni.report.entity.Report;
import org.ukiuni.report.entity.Report.ReportPK;
import org.ukiuni.report.util.DBUtil;
import org.ukiuni.report.util.DBUtil.Order.SequenceTo;
import org.ukiuni.report.util.DBUtil.WhereCondition.Match;
import org.ukiuni.report.util.DBUtil.WhereCondition.Rule;

public class ReportService {
	public DBUtil dbUtil = DBUtil.create("org.ukiuni.report");

	public List<Report> findByAccount(Account account) {
		return dbUtil.findList(Report.class, new DBUtil.WhereCondition[] { new DBUtil.WhereCondition("account", account, Match.EQ), new DBUtil.WhereCondition("status", Report.STATUS_DELETED, Match.NOT, Rule.AND) }, new DBUtil.Order("updatedAt", SequenceTo.DESC));
	}

	public Report create(Account account) {
		Report report = new Report();
		report.setCreatedAt(new Date());
		report.setKey(UUID.randomUUID().toString());
		report.setStatus(Report.STATUS_DRAFT);
		report.setAccount(account);
		ReportPK reportPK = new ReportPK();
		reportPK.setVersion(1);
		report.setPk(reportPK);
		dbUtil.persist(report);
		return report;
	}

	public Report find(long id, long version) {
		ReportPK reportPK = new ReportPK();
		reportPK.setId(id);
		reportPK.setVersion(version);
		return dbUtil.find(Report.class, reportPK);
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

	public void fold(Account account, Report report) {
		List<Fold> folds = findFolds(account, report);
		for (Fold fold : folds) {
			if (Fold.STATUS_CREATED.equals(fold.getStatus())) {
				return;
			}
		}

		Fold fold = new Fold();
		fold.setAccount(account);
		fold.setReportKey(report.getKey());
		fold.setStatus(Fold.STATUS_CREATED);
		fold.setCreatedAt(new Date());
		dbUtil.persist(fold);
	}

	public void unfold(Account account, Report report) {
		List<Fold> folds = findFolds(account, report);
		for (Fold fold : folds) {
			fold.setStatus(Fold.STATUS_DELETED);
			fold.setUpdatedAt(new Date());
			dbUtil.update(fold.getId(), fold);
		}
	}

	private List<Fold> findFolds(Account account, Report report) {
		return dbUtil.findList(Fold.class, new DBUtil.WhereCondition[] { new DBUtil.WhereCondition("reportKey", report.getKey()), new DBUtil.WhereCondition("account", account), new DBUtil.WhereCondition("status", Fold.STATUS_CREATED) });
	}

	public List<Fold> findFolds(String reportKey) {
		return dbUtil.findList(Fold.class, new DBUtil.WhereCondition[] { new DBUtil.WhereCondition("reportKey", reportKey), new DBUtil.WhereCondition("status", Fold.STATUS_CREATED) }, new DBUtil.Order("createdAt", DBUtil.Order.SequenceTo.DESC));
	}

	public long countFolded(Report report) {
		return dbUtil.count(Fold.class, new DBUtil.WhereCondition[] { new DBUtil.WhereCondition("reportKey", report.getKey()), new DBUtil.WhereCondition("status", Fold.STATUS_CREATED) });
	}

	public boolean hasFold(Account account, Report report) {
		return !findFolds(account, report).isEmpty();
	}

	public Comment comment(Account account, Report report, String message) {
		Comment comment = new Comment();
		comment.setAccount(account);
		comment.setReportKey(report.getKey());
		comment.setStatus(Comment.STATUS_CREATED);
		comment.setCreatedAt(new Date());
		comment.setMessage(message);
		dbUtil.persist(comment);
		return comment;

	}

	public List<Comment> findComments(String reportKey) {
		return dbUtil.findList(Comment.class, new DBUtil.WhereCondition[] { new DBUtil.WhereCondition("reportKey", reportKey), new DBUtil.WhereCondition("status", Comment.STATUS_DELETED, DBUtil.WhereCondition.Match.NOT) }, new DBUtil.Order("createdAt", DBUtil.Order.SequenceTo.ASC));
	}

	public Comment findCommentByKey(long commentId) {
		return dbUtil.find(Comment.class, commentId);
	}

	public void updateComment(Comment comment) {
		comment.setUpdatedAt(new Date());
		dbUtil.update(comment.getId(), comment);
	}

	public void deleteComment(Comment comment) {
		comment.setStatus(Comment.STATUS_DELETED);
		updateComment(comment);
	}
}
