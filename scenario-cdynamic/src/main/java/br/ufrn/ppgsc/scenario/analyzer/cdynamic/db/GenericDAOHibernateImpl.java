package br.ufrn.ppgsc.scenario.analyzer.cdynamic.db;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import br.ufrn.ppgsc.scenario.analyzer.cdynamic.util.Utils;

public class GenericDAOHibernateImpl<T extends Serializable> implements GenericDAO<T> {

	// Session is a static attribute
	private static Session s;

	// Only one session for all application
	public GenericDAOHibernateImpl() {
		if (s == null) {
			InputStream stream = GenericDAOHibernateImpl.class.getClass().getResourceAsStream("/sa_hibernate.cfg.xml");
			SessionFactory sf = new Configuration().configure(Utils.newDocumentFromInputStream(stream)).buildSessionFactory();
			s = sf.openSession();
		}
	}

	public void clearSession() {
		try {
			s.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public T save(T instance) {
		Transaction tx = null;

		try {
			tx = s.beginTransaction();
			System.out.println("Saving " + instance.toString());
			s.save(instance);
			System.out.println("Commiting " + instance.toString());
			tx.commit();
		} catch (RuntimeException e) {
			if (tx != null)
				tx.rollback();

			e.printStackTrace();
		}

		return instance;
	}

	public T read(Class<T> clazz, long id) {
		Object object = s.get(clazz, id);
		return clazz.cast(object);
	}

	public List<T> readAll(Class<T> clazz) {
		Query query = s.createQuery("from " + clazz.getName());

		@SuppressWarnings("unchecked")
		List<T> list = query.list();

		return list;
	}
	

}
