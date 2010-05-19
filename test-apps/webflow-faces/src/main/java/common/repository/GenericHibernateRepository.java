package common.repository;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for Hibernate based repository classes.
 */
public class GenericHibernateRepository<T,ID extends Serializable> implements GenericRepository<T, ID> {

	private SessionFactory sessionFactory;
	private Class<T> persistentClass;

	public GenericHibernateRepository(SessionFactory sessionFactory, Class<T> type) {
		this.sessionFactory = sessionFactory;
		this.persistentClass = type;
	}

	/**
	 * Returns the session associated with the ongoing reward transaction.
	 * @return the transactional session
	 */
	protected Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}

	public Class<T> getPersistentClass() {
		return persistentClass;
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public T findById(ID id) {
		return (T) getCurrentSession().load(getPersistentClass(), id);
	}

	@Transactional(readOnly=true)
	public List<T> findAll() {
		return findByCriteria();
	}
	
	@SuppressWarnings("unchecked")
	protected List<T> findByCriteria(Criterion... criterion) {
		Criteria criteria = getCurrentSession().createCriteria(persistentClass);
		for (Criterion c : criterion) {
			criteria.add(c);
		}
		return criteria.list();
	}
	
}