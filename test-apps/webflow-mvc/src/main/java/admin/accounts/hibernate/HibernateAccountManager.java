package admin.accounts.hibernate;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import admin.accounts.Account;
import admin.accounts.AccountManager;
import admin.accounts.AccountSearchCriteria;
import admin.accounts.NoSuchAccountException;

import common.money.Percentage;

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
	public List<Account> getAllAccounts() {
		return getCurrentSession().createQuery("from admin.accounts.Account order by name").list();
	}

	public Account getAccount(Long id) {
		return (Account) getCurrentSession().get(Account.class, id);
	}

	public void update(Account account) {
		getCurrentSession().update(account);
	}

	public void updateBeneficiaryAllocationPercentages(Long accountId, Map<String, Percentage> allocationPercentages) {
		Account account = getAccount(accountId);
		for (Entry<String, Percentage> entry : allocationPercentages.entrySet()) {
			account.getBeneficiary(entry.getKey()).setAllocationPercentage(entry.getValue());
		}
	}

	public void addBeneficiary(Long accountId, String beneficiaryName) {
		getAccount(accountId).addBeneficiary(beneficiaryName, Percentage.zero());
	}

	public void removeBeneficiary(Long accountId, String beneficiaryName, Map<String, Percentage> allocationPercentages) {
		getAccount(accountId).removeBeneficiary(beneficiaryName);
		updateBeneficiaryAllocationPercentages(accountId, allocationPercentages);
	}

	/**
	 * Returns the session associated with the ongoing reward transaction.
	 * @return the transactional session
	 */
	protected Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}
	
	@SuppressWarnings("unchecked")
	public List<Account> findAccounts(AccountSearchCriteria searchCriteria) {
		StringBuilder searchString = new StringBuilder("%")
			.append(searchCriteria.getSearchString().toUpperCase()).append("%");
		return getCurrentSession().createQuery("from admin.accounts.Account a where upper(a.name) like :name order by a.name")
			.setString("name", searchString.toString())
			.setFirstResult(searchCriteria.getPage() * searchCriteria.getPageSize())
			.setMaxResults(searchCriteria.getPageSize())
			.list();
	}

	public Account findByCreditCard(String creditCardNumber) throws NoSuchAccountException {
		Account account = (Account) getCurrentSession().
			createQuery("from admin.accounts.Account a  where a.creditCardNumber = ?").
			setString(0, creditCardNumber).uniqueResult();
		if (account == null) {
			throw new NoSuchAccountException("Credit card number: " + creditCardNumber);
		}
		return account;
	}
	
}