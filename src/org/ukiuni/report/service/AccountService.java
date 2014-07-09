package org.ukiuni.report.service;

import java.util.Date;
import java.util.UUID;

import javax.persistence.NoResultException;

import org.apache.commons.beanutils.BeanUtils;
import org.ukiuni.report.entity.Account;
import org.ukiuni.report.entity.AccountAccessKey;
import org.ukiuni.report.util.DBUtil;

public class AccountService {
	public DBUtil dbUtil = DBUtil.create("org.ukiuni.report");

	public void save(Account account) {
		dbUtil.persist(account);
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

	public Account loadByName(String name) {
		return dbUtil.findSingleEquals(Account.class, "name", name);
	}

	public void update(Account account, String... properties) {
		Account registedAccount = dbUtil.find(Account.class, account.getId());
		try {
			if (properties == null || 0 == properties.length) {
				BeanUtils.copyProperty(account, "mail", registedAccount);
			} else {
				for (String property : properties) {
					BeanUtils.copyProperty(account, property, registedAccount);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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

	public Account loadByAccessKey(String accessKey) {
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
