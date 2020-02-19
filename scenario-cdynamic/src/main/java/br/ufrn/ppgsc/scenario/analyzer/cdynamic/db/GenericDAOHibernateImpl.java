package br.ufrn.ppgsc.scenario.analyzer.cdynamic.db;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class GenericDAOHibernateImpl<T extends Serializable> implements GenericDAO<T> {

	// Session is a static attribute
	private static Session s;
	private static StandardServiceRegistry registry;
	private static SessionFactory sessionFactory;

	// Only one session for all application
	public GenericDAOHibernateImpl() {
		/**
		 * Implementação para hibernate 3.6.4
		 */
//		if (s == null) {
//			InputStream stream = GenericDAOHibernateImpl.class.getClass().getResourceAsStream("/sa_hibernate.cfg.xml");
//			SessionFactory sf = new Configuration().configure(Utils.newDocumentFromInputStream(stream)).buildSessionFactory();
//			s = sf.openSession();
//		}
		/**
		 * Implementação para hibernate 5
		 */
		if (sessionFactory == null) {
			try {
				// Create registry
				registry = new StandardServiceRegistryBuilder().configure().build();
				// Create MetadataSources
				MetadataSources sources = new MetadataSources(registry);
				// Create Metadata
				Metadata metadata = sources.getMetadataBuilder().build();
				// Create SessionFactory
				sessionFactory = metadata.getSessionFactoryBuilder().build();
				s = sessionFactory.openSession();
			} catch (Exception e) {
				e.printStackTrace();
				if (registry != null) {
					StandardServiceRegistryBuilder.destroy(registry);
				}
			}
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
