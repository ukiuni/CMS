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
import javax.persistence.criteria.Root;

import org.eclipse.persistence.internal.jpa.EntityManagerFactoryImpl;
import org.eclipse.persistence.sessions.server.ServerSession;

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
					openedFactoryMap.put(factoryName, factory);
				}
			}
		}
		this.factory = openedFactoryMap.get(factoryName);
	}

	private EntityManager createEntityManager() {
		return this.factory.createEntityManager();
	}

	public void persist(Object object) {
		EntityManager em = createEntityManager();
		EntityTransaction entityTransaction = em.getTransaction();
		entityTransaction.begin();
		em.persist(object);
		entityTransaction.commit();
	}

	public <T> T query(Query<T> query) {
		EntityManager em = createEntityManager();
		EntityTransaction entityTransaction = em.getTransaction();
		entityTransaction.begin();
		T result = query.query(em);
		entityTransaction.commit();
		return result;
	}

	public <T> T findSingleEquals(final Class<T> clazz, final String param, final Object equalsTarget) {
		Query<T> query = new Query<T>() {
			public T query(EntityManager em) {
				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<T> cq = cb.createQuery(clazz);
				Root<T> r = cq.from(clazz);
				cq.where(cb.equal(r.get(param).as(equalsTarget.getClass()), equalsTarget));
				return em.createQuery(cq.select(r)).getSingleResult();
			}
		};
		return query(query);
	}

	public <T> T find(Class<T> clazz, Object obj) {
		return createEntityManager().find(clazz, obj);
	}

	public <T> List<T> findAll(final Class<T> clazz) {
		Query<List<T>> query = new Query<List<T>>() {
			public List<T> query(EntityManager em) {
				CriteriaBuilder cb = em.getCriteriaBuilder();
				CriteriaQuery<T> cq = cb.createQuery(clazz);
				Root<T> r = cq.from(clazz);
				return em.createQuery(cq.select(r)).getResultList();
			}
		};
		return query(query);
	}

	public static void closeAll() {
		for (EntityManagerFactory factory : openedFactoryMap.values()) {
			try {
				((EntityManagerFactoryImpl) factory).unwrap(ServerSession.class).disconnect();
			} catch (Throwable e) {
			}
		}

	}

	public static interface Query<T> {
		public T query(EntityManager em);
	}
}