package org.ukiuni.report.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
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
	private static Map<String, EntityManagerFactory> openedFactoryMap = new HashMap<String, EntityManagerFactory>();
	private EntityManagerFactory factory;

	public static DBUtil create(String factoryName) {
		DBUtil instance = new DBUtil();
		instance.init(factoryName);
		return instance;
	}

	public void init(String factoryName) {
		if (!openedFactoryMap.containsKey(factoryName)) {
			synchronized (DBUtil.class) {
				if (!openedFactoryMap.containsKey(factoryName)) {
					EntityManagerFactory factory = Persistence.createEntityManagerFactory(factoryName);
					if (null == factory) {
						throw new IllegalArgumentException("factoryName [\"" + factoryName + "\"] cant be created.");
					}
					openedFactoryMap.put(factoryName, factory);
				}
			}
		}
		this.factory = openedFactoryMap.get(factoryName);
	}

	private EntityManager createEntityManager() {
		return this.factory.createEntityManager();
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

	public <T> T findSingle(final Class<T> clazz, final WhereCondition... whereConditions) {
		Work<T> work = new Work<T>() {
			public T execute(EntityManager em) {
				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<T> cq = cb.createQuery(clazz);
				Root<T> r = cq.from(clazz);
				prepareWhereCondition(cb, cq, r, whereConditions);
				return em.createQuery(cq.select(r)).getSingleResult();
			}

		};
		return execute(work);
	}

	@SuppressWarnings("unchecked")
	private <T> void prepareWhereCondition(CriteriaBuilder cb, CriteriaQuery<T> cq, Root<T> r, final WhereCondition... whereConditions) {
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
		if (null != fullPredicate) {
			cq.where(fullPredicate);
		}
	}

	public <T> T findSingleEquals(final Class<T> clazz, final String key, final Object equalsTarget) {
		return findSingle(clazz, new WhereCondition[] { new WhereCondition(key, equalsTarget) });
	}

	public <T> T find(final Class<T> clazz, final Object obj) {
		return execute(new Work<T>() {
			public T execute(EntityManager em) {
				return em.find(clazz, obj);
			}
		});
	}

	public <T> List<T> findAll(final Class<T> clazz) {
		Work<List<T>> work = new Work<List<T>>() {
			public List<T> execute(EntityManager em) {
				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<T> cq = cb.createQuery(clazz);
				Root<T> r = cq.from(clazz);
				return em.createQuery(cq.select(r)).getResultList();
			}
		};
		return execute(work);
	}

	public static void closeAll() {
		for (EntityManagerFactory factory : openedFactoryMap.values()) {
			try {
				((EntityManagerFactoryImpl) factory).unwrap(ServerSession.class).disconnect();
			} catch (Throwable e) {
			}
		}

	}

	public static interface Work<T> {
		public T execute(EntityManager em);
	}

	public <T> List<T> findList(final Class<T> clazz, final WhereCondition[] whereConditions, final Order... orders) {
		Work<List<T>> query = new Work<List<T>>() {
			public List<T> execute(EntityManager em) {
				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<T> cq = cb.createQuery(clazz);
				Root<T> r = cq.from(clazz);
				cq.select(r);
				prepareWhereCondition(cb, cq, r, whereConditions);
				prepareOrder(cb, cq, r, orders);
				return em.createQuery(cq).getResultList();
			}
		};
		return execute(query);
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
}
