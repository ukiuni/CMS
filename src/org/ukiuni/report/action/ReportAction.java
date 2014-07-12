package org.ukiuni.report.action;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.beanutils.BeanUtils;
import org.ukiuni.report.entity.Account;
import org.ukiuni.report.entity.Report;
import org.ukiuni.report.entity.Report.Status;
import org.ukiuni.report.service.AccountService;
import org.ukiuni.report.service.ReportService;

@Path("/report")
public class ReportAction {
	public AccountService accountService = new AccountService();
	public ReportService reportService = new ReportService();

	@GET
	@Path("loadByAccessKey")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Report> loadByAccessKey(@QueryParam("accessKey") @NotNull String accessKey) {
		Account account = accountService.findByAccessKey(accessKey);
		if (null == account) {
			throw new NotFoundException("account not found");
		}
		return reportService.loadByAccount(account);
	}

	@GET
	@Path("create")
	@Produces(MediaType.APPLICATION_JSON)
	public Report create(@QueryParam("accessKey") String accessKey) {
		Account account = accountService.findByAccessKey(accessKey);
		if (null == account) {
			throw new NotFoundException("account not found");
		}
		return reportService.create(account);
	}

	@GET
	@Path("load/{reportKey}")
	@Produces(MediaType.APPLICATION_JSON)
	public Report load(@QueryParam("accessKey") String accessKey, @PathParam("reportKey") String reportKey) {
		Account account = accountService.findByAccessKey(accessKey);
		if (null == account) {
			throw new NotFoundException("account not found");
		}
		Report report = reportService.findByKey(reportKey);
		return report;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ReportDto saveReport(ReportDto reportDto) throws IllegalAccessException, InvocationTargetException {
		Account account = accountService.findByAccessKey(reportDto.accountAccessKey);
		Report report = reportService.findByKey(reportDto.key);
		if (!report.getAccount().equals(account)) {
			throw new BadRequestException("report is not yours");
		}
		BeanUtils.copyProperties(report, reportDto);
		if (reportDto.draft) {
			report.setStatus(Status.DRAFT);
		} else if (reportDto.publish) {
			report.setStatus(Status.PUBLISHED);
		} else {
			report.setStatus(Status.PRIVATE);
		}
		reportService.update(report);
		BeanUtils.copyProperties(reportDto, report);
		return reportDto;
	}

	@PUT
	@Path("delete")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void delete(ReportDto reportDto) {
		Account account = accountService.findByAccessKey(reportDto.getAccountAccessKey());
		if (null == account) {
			throw new NotFoundException("account not found");
		}
		Report report = reportService.findByKey(reportDto.getKey());
		if (null == report) {
			throw new NotFoundException("report not found");
		}
		if (report.getAccount().getId() != account.getId()) {
			throw new BadRequestException("this report is not yours");
		}
		report.setStatus(Status.DELETED);
		reportService.update(report);
	}

	public static class ReportDto {
		private String accountAccessKey;
		private String title;
		private String content;
		private String key;
		private boolean draft;
		private boolean publish;

		public String getAccountAccessKey() {
			return accountAccessKey;
		}

		public void setAccountAccessKey(String accountAccessKey) {
			this.accountAccessKey = accountAccessKey;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public boolean isDraft() {
			return draft;
		}

		public void setDraft(boolean draft) {
			this.draft = draft;
		}

		@Override
		public String toString() {
			return "ReportDto [accountAccessKey=" + accountAccessKey + ", title=" + title + ", content=" + content + ", key=" + key + ", draft=" + draft + "]";
		}

		public boolean isPublish() {
			return publish;
		}

		public void setPublish(boolean publish) {
			this.publish = publish;
		}
	}
}
