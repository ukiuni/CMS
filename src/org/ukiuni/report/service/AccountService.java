package org.ukiuni.report.service;

import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import net.arnx.jsonic.util.Base64;

import org.ukiuni.report.entity.Account;
import org.ukiuni.report.entity.AccountAccessKey;
import org.ukiuni.report.entity.Fold;
import org.ukiuni.report.entity.Follow;
import org.ukiuni.report.util.DBUtil;
import org.ukiuni.report.util.DBUtil.Work;

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
		return dbUtil.findSingleEquals(Account.class, "name", name);
	}

	public boolean existsMail(String mail) {
		return null != dbUtil.findSingleEquals(Account.class, "mail", mail);
	}

	public Account findByAccessKey(String accessKey) {
		AccountAccessKey accountAccessKey = dbUtil.findSingleEquals(AccountAccessKey.class, "hash", accessKey);
		if (null == accountAccessKey) {
			return null;
		}
		return accountAccessKey.getAccount();
	}

	public List<Fold> findFolds(Account account) {
		return dbUtil.findList(Fold.class, new DBUtil.WhereCondition[] { new DBUtil.WhereCondition("account", account), new DBUtil.WhereCondition("status", Fold.STATUS_CREATED) }, new DBUtil.Order("createdAt", DBUtil.Order.SequenceTo.DESC));
	}

	public void follow(Account account, long targetAccountId) {
		Account targetAccount = dbUtil.find(Account.class, targetAccountId);
		boolean followed = checkFollowing(account, targetAccount);
		if (followed) {
			return;
		}
		if(targetAccountId == account.getId()){
			throw new IllegalArgumentException();
		}
		Follow follow = new Follow();
		follow.setFollower(account);
		follow.setFollows(targetAccount);
		follow.setStatus(Follow.STATUS_CREATED);
		follow.setCreatedAt(new Date());
		dbUtil.persist(follow);
	}

	private boolean checkFollowing(Account account, Account targetAccount) {
		List<Follow> follows = dbUtil.findList(Follow.class, new DBUtil.WhereCondition[] { new DBUtil.WhereCondition("follower", account), new DBUtil.WhereCondition("follows", targetAccount) });
		boolean followed = false;
		for (Follow follow : follows) {
			if (Follow.STATUS_CREATED.equals(follow.getStatus())) {
				followed = true;
			}
		}
		return followed;
	}

	public List<Follow> findFollower(Account account) {
		return dbUtil.findList(Follow.class, new DBUtil.WhereCondition[] { new DBUtil.WhereCondition("follows", account), new DBUtil.WhereCondition("status", Follow.STATUS_CREATED) }, new DBUtil.Order("createdAt", DBUtil.Order.SequenceTo.DESC));
	}

	public void unfollow(final Account account, long targetAccountId) {
		final Account targetAccount = dbUtil.find(Account.class, targetAccountId);
		dbUtil.execute(new Work<Void>() {
			@Override
			public Void execute(EntityManager em) {
				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<Follow> cq = cb.createQuery(Follow.class);
				Root<Follow> r = cq.from(Follow.class);
				cq.where(cb.and(cb.equal(r.get("follower"), account), cb.equal(r.get("follows"), targetAccount)));
				List<Follow> followses = em.createQuery(cq.select(r)).getResultList();
				for (Follow follow : followses) {
					follow.setStatus(Follow.STATUS_DELETED);
				}
				return null;
			}
		});
	}

	public boolean following(Account account, Account targetAccount) {
		return checkFollowing(account, targetAccount);
	}
}
