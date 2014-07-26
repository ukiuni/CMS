package org.ukiuni.report.entity;

import java.util.Date;

public class News {
	public static final String EVENT_FOLLOW = "EVENT_FOLLOW";
	public static final String EVENT_REPORT = "EVENT_REPORT";
	public static final String EVENT_FOLD = "EVENT_FOLD";
	public String event;
	public long accountId;
	public String accountName;
	public String accountIconUrl;
	public long targetAccountId;
	public String targetAccountName;
	public String targetAccountIconUrl;
	public String targetReportKey;
	public String targetReportTitle;
	private Date createdAt;

	public News(String event, long accountId, String accountName, String accountIconUrl, Date createdAt) {
		this.event = event;
		this.targetAccountId = accountId;
		this.targetAccountName = accountName;
		this.targetAccountIconUrl = accountIconUrl;
		this.createdAt = createdAt;
	}

	public News(String event, long accountId, String accountName, String accountIconUrl, String reportKey, String reportTitle, long targetAccountId, String targetAccountName, String targetAccountIconUrl, Date createdAt) {
		this.event = event;
		this.accountId = accountId;
		this.accountName = accountName;
		this.accountIconUrl = accountIconUrl;
		this.targetReportTitle = reportTitle;
		this.targetReportKey = reportKey;
		this.targetAccountId = targetAccountId;
		this.targetAccountName = targetAccountName;
		this.targetAccountIconUrl = targetAccountIconUrl;
		this.createdAt = createdAt;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public long getTargetAccountId() {
		return targetAccountId;
	}

	public void setTargetAccountId(long targetAccountId) {
		this.targetAccountId = targetAccountId;
	}

	public String getTargetAccountName() {
		return targetAccountName;
	}

	public void setTargetAccountName(String targetAccountName) {
		this.targetAccountName = targetAccountName;
	}

	public String getTargetAccountIconUrl() {
		return targetAccountIconUrl;
	}

	public void setTargetAccountIconUrl(String targetAccountIconUrl) {
		this.targetAccountIconUrl = targetAccountIconUrl;
	}

	public String getTargetReportKey() {
		return targetReportKey;
	}

	public void setTargetReportKey(String targetReportKey) {
		this.targetReportKey = targetReportKey;
	}

	public String getTargetReportTitle() {
		return targetReportTitle;
	}

	public void setTargetReportTitle(String targetReportTitle) {
		this.targetReportTitle = targetReportTitle;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	@Override
	public String toString() {
		return "News [event=" + event + ", accountName=" + accountName + ", targetAccountId=" + targetAccountId + ", targetAccountName=" + targetAccountName + ", targetAccountIconUrl=" + targetAccountIconUrl + ", targetReportKey=" + targetReportKey + ", targetReportTitle=" + targetReportTitle + ", createdAt=" + createdAt + "]";
	}
}
