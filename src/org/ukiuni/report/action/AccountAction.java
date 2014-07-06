package org.ukiuni.report.action;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ResourceBundle;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import net.arnx.jsonic.JSON;

import org.apache.commons.beanutils.BeanUtils;
import org.ukiuni.report.ResponseServerStatusException;
import org.ukiuni.report.entity.Account;
import org.ukiuni.report.service.AccountService;
import org.ukiuni.report.util.DBUtil;

@Path("/account")
public class AccountAction {
	public AccountService accountService = new AccountService();

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void create(SaveAccount saveAccount) throws IllegalAccessException, InvocationTargetException {
		System.out.println("create " + saveAccount);
		if (accountService.existsName(saveAccount.getName())) {
			System.out.println("account is exist name = " + saveAccount.getName());
			throw new ResponseServerStatusException(409, "name");
		}
		if (accountService.existsMail(saveAccount.getMail())) {
			System.out.println("account is exist mail = " + saveAccount.getMail());
			throw new ResponseServerStatusException(409, "email");
		}
		Account account = new Account();
		BeanUtils.copyProperties(account, saveAccount);
		System.out.println("account " + account);
		accountService.save(account);
	}

	@POST
	@Path("/update")
	public void update(String body) throws IllegalAccessException, InvocationTargetException {
		SaveAccount saveAccount = JSON.decode(body, SaveAccount.class);
		Account account = new Account();
		BeanUtils.copyProperties(account, saveAccount);
		accountService.save(account);
	}

	@GET
	public List<Account> list() {
		return DBUtil.create("org.ukiuni.report").findAll(Account.class);
	}

	@SuppressWarnings("serial")
	public static class SaveAccount implements Serializable {
		@Size(min = 6, max = 20, message = "An event's name must contain between 6 and 1000 characters")//効かない
		public String name;
		@NotNull
		public String mail;
		@NotNull
		public String password;
		@NotNull
		public String profile;
		public String iconUrl;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getMail() {
			return mail;
		}

		public void setMail(String mail) {
			this.mail = mail;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
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

		@Override
		public String toString() {
			return "SaveAccount [name=" + name + ", mail=" + mail + ", password" + password + ", profile=" + profile + ", iconUrl=" + iconUrl + "]";
		}
	}

}
