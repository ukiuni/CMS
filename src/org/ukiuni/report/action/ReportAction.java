package org.ukiuni.report.action;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.beanutils.BeanUtils;
import org.ukiuni.report.entity.Account;
import org.ukiuni.report.entity.Report;
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
		System.out.println("accessKey = " + accessKey);
		Account account = accountService.loadByAccessKey(accessKey);
		if (null == account) {
			throw new NotFoundException("account not found");
		}
		return reportService.loadByAccount(account);
	}

	@GET
	@Path("create")
	@Produces(MediaType.APPLICATION_JSON)
	public Report create(@QueryParam("accessKey") String accessKey) {
		Account account = accountService.loadByAccessKey(accessKey);
		if (null == account) {
			throw new NotFoundException("account not found");
		}
		return reportService.create(account);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ReportDto saveReport(ReportDto reportDto) throws IllegalAccessException, InvocationTargetException {
		System.out.println(reportDto);
		Report report = reportService.find(reportDto.pk.id, reportDto.pk.version);
		BeanUtils.copyProperties(report, reportDto);
		BeanUtils.copyProperties(reportDto, report);
		return reportDto;
	}

	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public void deleteTodos(String body) {
	}

	public static class ReportDto {
		private String accountAccessKey;
		private ReportPKDto pk;
		private String title;
		private String content;
		private String key;
		private String status;

		public String getAccountAccessKey() {
			return accountAccessKey;
		}

		public void setAccountAccessKey(String accountAccessKey) {
			this.accountAccessKey = accountAccessKey;
		}

		public ReportPKDto getPk() {
			return pk;
		}

		public void setPk(ReportPKDto pk) {
			this.pk = pk;
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

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		@Override
		public String toString() {
			return "ReportDto [accountAccessKey=" + accountAccessKey + ", pk=" + pk + ", title=" + title + ", content=" + content + ", key=" + key + ", status=" + status + "]";
		}
	}

	public static class ReportPKDto {
		private long id;
		private long version;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public long getVersion() {
			return version;
		}

		public void setVersion(long version) {
			this.version = version;
		}

		@Override
		public String toString() {
			return "ReportPKDto [id=" + id + ", version=" + version + "]";
		}
	}

}
