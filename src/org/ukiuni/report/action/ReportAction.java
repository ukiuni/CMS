package org.ukiuni.report.action;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
	public ReportDto load(@QueryParam("accountAccessKey") String accessKey, @PathParam("reportKey") String reportKey) throws IllegalAccessException, InvocationTargetException {
		Report report = reportService.findByKey(reportKey);

		Account account = null;
		if (null != accessKey) {
			account = accountService.findByAccessKey(accessKey);
		}
		if (!Report.STATUS_PUBLISHED.equals(report.getStatus())) {
			if (null == accessKey) {
				throw new ForbiddenException("this report not accessible");
			}
			if (null == account) {
				throw new NotFoundException("account not found");
			} else if (report.getAccount().getId() != account.getId()) {
				throw new ForbiddenException("this report not accessible");
			}
		}
		ReportDto reportDto = new ReportDto();
		BeanUtils.copyProperties(reportDto, report);
		ReporterDto reporterDto = new ReporterDto();
		BeanUtils.copyProperties(reporterDto, report.getAccount());
		reportDto.setReporter(reporterDto);
		long foldedCount = reportService.loadFoldedCount(report);
		reportDto.setFoldedCount(foldedCount);
		if (null != account) {
			boolean hasHold = reportService.hasFold(account, report);
			reportDto.setFolded(hasHold);
		}
		return reportDto;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ReportDto saveReport(ReportDtoWithAccesskey reportDto) throws IllegalAccessException, InvocationTargetException {
		Account account = accountService.findByAccessKey(reportDto.accountAccessKey);
		Report report = reportService.findByKey(reportDto.getKey());
		if (!report.getAccount().equals(account)) {
			throw new BadRequestException("report is not yours");
		}
		BeanUtils.copyProperties(report, reportDto);
		reportService.update(report);
		BeanUtils.copyProperties(reportDto, report);
		return reportDto;
	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public void delete(@QueryParam("key") String reportKey, @QueryParam("accountAccessKey") String accountAccessKey) {
		Account account = accountService.findByAccessKey(accountAccessKey);
		if (null == account) {
			throw new NotFoundException("account not found");
		}
		Report report = reportService.findByKey(reportKey);
		if (null == report) {
			throw new NotFoundException("report not found");
		}
		if (report.getAccount().getId() != account.getId()) {
			throw new BadRequestException("this report is not yours");
		}
		report.setStatus(Report.STATUS_DELETED);
		reportService.update(report);
	}

	@POST
	@Path("fold")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void fold(ReportDtoWithAccesskey reportDto) {
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
		reportService.fold(account, report);
	}

	@DELETE
	@Path("fold")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void unfold(@QueryParam("key") String reportKey, @QueryParam("accountAccessKey") String accountAccessKey) {
		Account account = accountService.findByAccessKey(accountAccessKey);
		if (null == account) {
			throw new NotFoundException("account not found");
		}
		Report report = reportService.findByKey(reportKey);
		if (null == report) {
			throw new NotFoundException("report not found");
		}
		if (report.getAccount().getId() != account.getId()) {
			throw new BadRequestException("this report is not yours");
		}
		reportService.unfold(account, report);
	}

	public static class ReporterDto {
		private long id;
		private String name;
		private String fullName;
		private String profile;
		private String iconUrl;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getFullName() {
			return fullName;
		}

		public void setFullName(String fullName) {
			this.fullName = fullName;
		}

		public String getProfile() {
			return profile;
		}

		public void setProfile(String profile) {
			this.profile = profile;
		}

		public String getIconUrl() {
			return iconUrl;
		}

		public void setIconUrl(String iconUrl) {
			this.iconUrl = iconUrl;
		}
	}

	public static class ReportDtoWithAccesskey extends ReportDto {
		private String accountAccessKey;

		public String getAccountAccessKey() {
			return accountAccessKey;
		}

		public void setAccountAccessKey(String accountAccessKey) {
			this.accountAccessKey = accountAccessKey;
		}
	}

	public static class ReportDto {
		private String title;
		private String content;
		private String key;
		private String status;
		private ReporterDto reporter;
		private long foldedCount;
		private boolean folded;

		public ReporterDto getReporter() {
			return reporter;
		}

		public void setReporter(ReporterDto reporter) {
			this.reporter = reporter;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
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

		public long getFoldedCount() {
			return foldedCount;
		}

		public void setFoldedCount(long foldedCount) {
			this.foldedCount = foldedCount;
		}

		public boolean isFolded() {
			return folded;
		}

		public void setFolded(boolean folded) {
			this.folded = folded;
		}
	}
}
