package admin.accounts.hibernate;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import admin.accounts.Account;
import admin.accounts.AccountManager;
import admin.accounts.AccountSearchCriteria;

/**
 * An account manager that uses Hibernate to find and update accounts.
 */
@Repository("accountManager")
public class HibernateAccountManager implements AccountManager {

	private SessionFactory sessionFactory;

	/**
	 * Creates a new Hibernate account manager.
	 * @param sessionFactory the Hibernate session factory
	 */
	@Autowired
	public HibernateAccountManager(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public Account findAccount(String number) {
		return (Account) getCurrentSession().
			createQuery("from admin.accounts.Account a left join fetch a.beneficiaries where a.number = ?").
			setString(0, number).uniqueResult();
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public List<Account> findAccounts(AccountSearchCriteria searchCriteria) {
		StringBuilder searchString = new StringBuilder("%")
			.append(searchCriteria.getSearchString().toUpperCase()).append("%");
		return getCurrentSession().createQuery("from admin.accounts.Account a left join fetch a.beneficiaries where upper(a.name) like :name order by a.name")
			.setString("name", searchString.toString())
			.setFirstResult(searchCriteria.getPage() * searchCriteria.getPageSize())
			.setMaxResults(searchCriteria.getPageSize())
			.list();
	}

	@SuppressWarnings("unchecked")
	public List<Account> getAllAccounts() {
		return getCurrentSession().createQuery("from admin.accounts.Account order by name").list();
	}

	public void update(Account account) {
		getCurrentSession().update(account);
	}

	/**
	 * Returns the session associated with the ongoing reward transaction.
	 * @return the transactional session
	 */
	protected Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}
	
}