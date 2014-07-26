package org.ukiuni.report.action;

import java.util.List;

import javassist.NotFoundException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.ukiuni.report.entity.Account;
import org.ukiuni.report.entity.News;
import org.ukiuni.report.service.AccountService;
import org.ukiuni.report.service.NewsService;

@Path("/news")
public class NewsAction {
	public AccountService accountService = new AccountService();
	public NewsService newsService = new NewsService();

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<News> news(@QueryParam("accessKey") String accessKey) throws NotFoundException {
		Account account;
		if (null == accessKey || null == (account = accountService.findByAccessKey(accessKey))) {
			throw new NotFoundException("account not found");
		}
		return newsService.find(account);
	}
}
