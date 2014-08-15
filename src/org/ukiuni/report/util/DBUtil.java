package org.ukiuni.report.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryImpl;
import org.eclipse.persistence.sessions.server.ServerSession;
import org.ukiuni.report.util.DBUtil.WhereCondition.Match;
import org.ukiuni.report.util.DBUtil.WhereCondition.Rule;

public class DBUtil {
	public static final String SYSTEM_ENV_FACTORYNAME_POSTFIX = "org.ukiuni.db.factoryName.postfix";
	private static final Map<String, EntityManagerFactory> openedFactoryMap = new HashMap<String, EntityManagerFactory>();
	private EntityManagerFactory factory;
	private static Map<String, String> parameterMap = null;
	static {
		Map<String, String> parameterMap = new HashMap<String, String>();
		List<String> loadEnvs = Arrays.asList(new String[] { "javax.persistence.jdbc.driver", "javax.persistence.jdbc.url", "javax.persistence.jdbc.user", "javax.persistence.jdbc.password" });
		for (String envKey : loadEnvs) {
			if (null != System.getenv(envKey)) {
				parameterMap.put(envKey, System.getenv(envKey));
			}
		}
		if (!parameterMap.isEmpty()) {
			DBUtil.parameterMap = parameterMap;
		}
	}

	public static DBUtil create(String factoryName) {
		DBUtil instance = new DBUtil();
		instance.init(factoryName);
		return instance;
	}

	public void init(String factoryName) {
		String postfix = System.getProperty(SYSTEM_ENV_FACTORYNAME_POSTFIX, "");
		if ("".equals(postfix)) {
			String systemPostfix = System.getenv(SYSTEM_ENV_FACTORYNAME_POSTFIX);
			if (null != systemPostfix) {
				postfix = systemPostfix;
			}
		}
		factoryName = factoryName + postfix;
		if (!openedFactoryMap.containsKey(factoryName)) {
			synchronized (DBUtil.class) {
				if (!openedFactoryMap.containsKey(factoryName)) {
					EntityManagerFactory factory = createEntityManagerFactory(factoryName);
					if (null == factory) {
						throw new IllegalArgumentException("factoryName [\"" + factoryName + "\"] cant be created.");
					}
					openedFactoryMap.put(factoryName, factory);
				}
			}
		}
		this.factory = openedFactoryMap.get(factoryName);
	}

	protected EntityManagerFactory createEntityManagerFactory(String factoryName) {
		if (null != parameterMap) {
			return Persistence.createEntityManagerFactory(factoryName, parameterMap);
		} else {
			return Persistence.createEntityManagerFactory(factoryName);
		}
	}

	private EntityManager createEntityManager() {
		if (null != parameterMap) {
			return this.factory.createEntityManager(parameterMap);
		} else {
			return this.factory.createEntityManager();
		}
	}

	public Object persist(final Object object) {
		return execute(new Work<Object>() {
			public Object execute(EntityManager em) {
				em.persist(object);
				return object;
			}
		});
	}

	public <T> T execute(Work<T> executer) {
		EntityManager em = createEntityManager();
		try {
			EntityTransaction entityTransaction = em.getTransaction();
			entityTransaction.begin();
			T result = executer.execute(em);
			entityTransaction.commit();
			return result;
		} finally {
			em.close();
		}
	}

	public <T> T update(final Object id, final Object obj) {
		return this.execute(new Work<T>() {
			@SuppressWarnings("unchecked")
			@Override
			public T execute(EntityManager em) {
				Object target = em.find(obj.getClass(), id);
				try {
					BeanUtils.copyProperties(target, obj);
					em.merge(target);
				} catch (Exception e) {
					new RuntimeException(e);
				}
				return (T) target;
			}
		});
	}

	@SuppressWarnings("unchecked")
	private <T> Predicate prepareWhereCondition(CriteriaBuilder cb, Root<T> r, final WhereCondition... whereConditions) {
		Predicate fullPredicate = null;
		for (WhereCondition whereCondition : whereConditions) {
			Predicate predicate;
			if (whereCondition.match.equals(Match.GE)) {
				predicate = cb.ge(r.get(whereCondition.key).as((Class<? extends Number>) whereCondition.argument.getClass()), (Number) whereCondition.argument);
			} else if (whereCondition.match.equals(Match.GT)) {
				predicate = cb.gt(r.get(whereCondition.key).as((Class<? extends Number>) whereCondition.argument.getClass()), (Number) whereCondition.argument);
			} else if (whereCondition.match.equals(Match.LE)) {
				predicate = cb.le(r.get(whereCondition.key).as((Class<? extends Number>) whereCondition.argument.getClass()), (Number) whereCondition.argument);
			} else if (whereCondition.match.equals(Match.LT)) {
				predicate = cb.lt(r.get(whereCondition.key).as((Class<? extends Number>) whereCondition.argument.getClass()), (Number) whereCondition.argument);
			} else if (whereCondition.match.equals(Match.EQ)) {
				predicate = cb.equal(r.get(whereCondition.key).as(whereCondition.argument.getClass()), whereCondition.argument);
			} else if (whereCondition.match.equals(Match.LIKE)) {
				predicate = cb.like(r.get(whereCondition.key).as((Class<String>) whereCondition.argument.getClass()), (String) whereCondition.argument);
			} else {
				predicate = cb.notEqual(r.get(whereCondition.key).as(whereCondition.argument.getClass()), whereCondition.argument);
			}
			if (null == fullPredicate) {
				fullPredicate = predicate;
				continue;
			}
			if (whereCondition.rule.equals(Rule.AND)) {
				fullPredicate = cb.and(fullPredicate, predicate);
			} else if (whereCondition.rule.equals(Rule.OR)) {
				fullPredicate = cb.or(fullPredicate, predicate);
			}
		}
		return fullPredicate;
	}

	public <T> T findSingleEquals(final Class<T> clazz, final String key, final Object equalsTarget) {
		try {
			return findSingle(clazz, new WhereCondition[] { new WhereCondition(key, equalsTarget) });
		} catch (NoResultException e) {
			return null;
		}
	}

	public <T> T find(final Class<T> clazz, final Object obj) {
		try {
			return execute(new Work<T>() {
				public T execute(EntityManager em) {
					return em.find(clazz, obj);
				}
			});
		} catch (NoResultException e) {
			return null;
		}
	}

	public <T> List<T> findAll(final Class<T> clazz) {
		Work<List<T>> work = new Work<List<T>>() {
			public List<T> execute(EntityManager em) {
				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<T> cq = cb.createQuery(clazz);
				Root<T> r = cq.from(clazz);
				return em.createQuery(cq.select(r)).setLockMode(LockModeType.OPTIMISTIC).getResultList();
			}
		};
		return execute(work);
	}

	public static void closeAll() {
		for (EntityManagerFactory factory : openedFactoryMap.values()) {
			try {
				((EntityManagerFactoryImpl) factory).unwrap(ServerSession.class).disconnect();
				factory.close();
			} catch (Throwable e) {
			}
		}
		List<String> removeKeyList = new ArrayList<String>();
		for (String key : openedFactoryMap.keySet()) {
			removeKeyList.add(key);
		}
		for (Object key : removeKeyList) {
			openedFactoryMap.remove(key);
		}
	}

	public static interface Work<T> {
		public T execute(EntityManager em);
	}

	public <T> T findSingle(final Class<T> clazz, final WhereCondition... whereConditions) {
		Work<T> work = new Work<T>() {
			public T execute(EntityManager em) {
				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<T> cq = cb.createQuery(clazz);
				Root<T> r = cq.from(clazz);
				Predicate predicate = prepareWhereCondition(cb, r, whereConditions);
				if (null != predicate) {
					cq.where(predicate);
				}

				return em.createQuery(cq.select(r)).setLockMode(LockModeType.OPTIMISTIC).getSingleResult();
			}

		};
		return execute(work);
	}

	public List<Object[]> findListWithNativeQuery(final String nativeQuery, final Object... params) {
		Work<List<Object[]>> query = new Work<List<Object[]>>() {
			@SuppressWarnings("unchecked")
			public List<Object[]> execute(EntityManager em) {
				Query cb = em.createNativeQuery(nativeQuery);
				attachParameterToQuery(cb, params);
				return cb.getResultList();
			}
		};
		List<Object[]> objectList = execute(query);
		for (Object[] datas : objectList) {
			for (int i = 0; i < datas.length; i++) {
				if (null != datas[i] && datas[i] instanceof Timestamp) {
					Timestamp timestamp = ((Timestamp) datas[i]);
					datas[i] = new Date(timestamp.getTime());
				}
			}
		}
		return objectList;
	}

	private <T> void attachParameterToQuery(Query cb, Object... params) {
		for (int i = 0; i < params.length; i++) {
			if (params[i] instanceof Date) {
				cb.setParameter(i + 1, (Date) params[i], TemporalType.TIMESTAMP);
			} else {
				cb.setParameter(i + 1, params[i]);
			}
		}
	}

	public <T> List<T> findListWithQuery(final Class<T> clazz, final String nativeQuery, final Map<String, Object> params) {
		Work<List<T>> query = new Work<List<T>>() {
			public List<T> execute(EntityManager em) {
				TypedQuery<T> query = em.createQuery(nativeQuery, clazz);
				for (String key : params.keySet()) {
					query.setParameter(key, params.get(key));
				}
				return query.getResultList();
			}
		};
		return execute(query);
	}

	public <T> List<T> findList(Class<T> clazz, WhereCondition[] whereConditions, Order... orders) {
		return findList(clazz, whereConditions, null, orders);
	}

	public <T> List<T> findList(final Class<T> clazz, final WhereCondition[] whereConditions, final Paginate paginate, final Order... orders) {
		Work<List<T>> query = new Work<List<T>>() {
			public List<T> execute(EntityManager em) {
				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<T> cq = cb.createQuery(clazz);
				Root<T> r = cq.from(clazz);
				Predicate predicate = prepareWhereCondition(cb, r, whereConditions);
				if (null != predicate) {
					cq.where(predicate);
				}
				prepareOrder(cb, cq, r, orders);
				if (null != paginate) {
					return em.createQuery(cq.select(r)).setFirstResult(paginate.getOffset()).setMaxResults(paginate.getLimit()).setLockMode(LockModeType.OPTIMISTIC).getResultList();
				} else {
					return em.createQuery(cq.select(r)).setLockMode(LockModeType.OPTIMISTIC).getResultList();
				}
			}
		};
		return execute(query);
	}

	public <T> long count(final Class<T> clazz, String key, Object value) {
		return count(clazz, new WhereCondition[] { new WhereCondition(key, value) });
	}

	public <T> long count(final Class<T> clazz, final WhereCondition[] whereConditions) {
		Work<Long> work = new Work<Long>() {
			public Long execute(EntityManager em) {
				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<Long> cq = cb.createQuery(Long.class);
				Root<T> r = cq.from(clazz);
				Predicate predicate = prepareWhereCondition(cb, r, whereConditions);
				if (null != predicate) {
					cq.where(predicate);
				}
				return em.createQuery(cq.select(cb.count(r))).getSingleResult();
			}

		};
		return execute(work);
	}

	private <T> void prepareOrder(CriteriaBuilder cb, CriteriaQuery<T> cq, Root<T> r, final Order... orders) {
		for (Order order : orders) {
			if (Order.SequenceTo.ASC.equals(order.to)) {
				cq.orderBy(cb.asc(r.get(order.key)));
			}
			if (Order.SequenceTo.DESC.equals(order.to)) {
				cq.orderBy(cb.desc(r.get(order.key)));
			}
		}
	}

	public <T> T delete(final Class<T> clazz, final Object id) {
		return execute(new Work<T>() {
			public T execute(EntityManager em) {
				T obj = em.find(clazz, id);
				em.remove(obj);
				return obj;
			}
		});
	}

	public static class WhereCondition {
		public String key;
		public Object argument;
		public Match match;
		public Rule rule;

		public WhereCondition() {
		}

		public WhereCondition(String key, Object argument) {
			this(key, argument, Match.EQ);
		}

		public WhereCondition(String key, Object argument, Match match) {
			this(key, argument, match, Rule.AND);
		}

		public WhereCondition(String key, Object argument, Match match, Rule rule) {
			this.key = key;
			this.argument = argument;
			this.match = match;
			this.rule = rule;
		}

		public enum Match {
			EQ, NOT, GT, LT, GE, LE, LIKE
		}

		public enum Rule {
			AND, OR
		}
	}

	public static class Order {
		public String key;
		public SequenceTo to;

		public Order() {
		}

		public Order(String key) {
			this.key = key;
			to = SequenceTo.ASC;
		}

		public Order(String key, SequenceTo to) {
			super();
			this.key = key;
			this.to = to;
		}

		public enum SequenceTo {
			ASC, DESC
		}
	}

	public static class Paginate {
		public int offset;
		public int limit;

		public Paginate(int offset, int limit) {
			this.offset = offset;
			this.limit = limit;
		}

		public int getOffset() {
			return offset;
		}

		public void setOffset(int offset) {
			this.offset = offset;
		}

		public int getLimit() {
			return limit;
		}

		public void setLimit(int limit) {
			this.limit = limit;
		}
	}
}
