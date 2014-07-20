package org.ukiuni.report.service;

import java.security.MessageDigest;
import java.util.Date;
import java.util.UUID;

import javax.persistence.NoResultException;

import net.arnx.jsonic.util.Base64;

import org.ukiuni.report.entity.Account;
import org.ukiuni.report.entity.AccountAccessKey;
import org.ukiuni.report.util.DBUtil;

public class AccountService {
	public DBUtil dbUtil = DBUtil.create("org.ukiuni.report");

	public Account create(String name, String mail, String password) {
		Account account = new Account();
		account.setName(name);
		account.setMail(mail);
		try {
			account.setPasswordHashed(Base64.encode(MessageDigest.getInstance("SHA1").digest(password.getBytes("UTF-8"))));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		account.setCreatedAt(new Date());
		dbUtil.persist(account);
		return account;
	}

	public void update(Account account) {
		dbUtil.update(account.getId(), account);
	}

	public AccountAccessKey generateAccessKey(Account account) {
		AccountAccessKey accountAccessKey = new AccountAccessKey();
		accountAccessKey.setAccount(account);
		accountAccessKey.setHash(UUID.randomUUID().toString());
		accountAccessKey.setCreatedAt(new Date());
		accountAccessKey.setStatus(org.ukiuni.report.entity.AccountAccessKey.Status.CREATED);
		dbUtil.persist(accountAccessKey);
		return accountAccessKey;
	}

	@SuppressWarnings("serial")
	public static class DuplicateAccountNameException extends Exception {
		public DuplicateAccountNameException(Throwable arg0) {
			super(arg0);
		}
	}

	public boolean existsName(String name) {
		return null != findByName(name);
	}

	public Account findByName(String name) {
		try {
			return dbUtil.findSingleEquals(Account.class, "name", name);
		} catch (NoResultException e) {
			return null;
		}
	}

	public boolean existsMail(String mail) {
		try {
			dbUtil.findSingleEquals(Account.class, "mail", mail);
			return true;
		} catch (NoResultException e) {
			return false;
		}
	}

	public Account findByAccessKey(String accessKey) {
		try {
			AccountAccessKey accountAccessKey = dbUtil.findSingleEquals(AccountAccessKey.class, "hash", accessKey);
			if (null == accountAccessKey) {
				return null;
			}
			return accountAccessKey.getAccount();
		} catch (NoResultException e) {
			return null;
		}
	}
}
