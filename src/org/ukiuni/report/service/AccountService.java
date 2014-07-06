package org.ukiuni.report.service;

import javax.persistence.NoResultException;

import org.apache.commons.beanutils.BeanUtils;
import org.ukiuni.report.entity.Account;
import org.ukiuni.report.util.DBUtil;

public class AccountService {
	public DBUtil dbUtil = DBUtil.create("org.ukiuni.report");

	public void save(Account account) {
		dbUtil.persist(account);
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
}
