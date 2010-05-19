package accounts.internal;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import accounts.Account;
import accounts.AccountRepository;
import accounts.AccountSearchCriteria;
import accounts.NoSuchAccountException;

import common.money.Percentage;
import common.repository.GenericHibernateRepository;

/**
 * Responsible for finding and modifying account objects using the 
 * Hibernate API. 
 * 
 * NOTE: normally the transaction boundaries are placed on the business 
 * service layer. However since the AccountRepository is quite simple 
 * and does not need a business service layer the transactional boundaries 
 * are declared on the repository itself.
 */
@Transactional
public class HibernateAccountRepository
	extends GenericHibernateRepository<Account, Long> implements AccountRepository {

	/**
	 * Creates an new hibernate-based account repository.
	 * @param sessionFactory the Hibernate session factory required to obtain sessions
	 */
	public HibernateAccountRepository(SessionFactory sessionFactory) {
		super(sessionFactory, Account.class);
	}
	
	@Transactional(readOnly = true)
	public Account findByCreditCard(String creditCardNumber) {
		Account account = (Account) getCurrentSession().
			createQuery("from Account a  where a.creditCardNumber = ?").
			setString(0, creditCardNumber).uniqueResult();
		if (account == null) {
			throw new NoSuchAccountException("No account found for credit card: " + creditCardNumber);
		}
		return account;
	}

	@Transactional(readOnly = true)
	public Account findByAccountNumber(String accountNumber) {
		Object account = getCurrentSession().
			createQuery("from Account a  where a.number = ?").
			setString(0, accountNumber).uniqueResult();
		if (account == null) {
			throw new NoSuchAccountException("No account found for account number: " + accountNumber);
		}
		return (Account) account;
	}

	public void update(Account account) {
		getCurrentSession().update(account);
	}

	public void updateBeneficiaryAllocationPercentages(Long accountId, Map<String, Percentage> allocationPercentages) {
		Account account = findById(accountId);
		for (Entry<String, Percentage> entry : allocationPercentages.entrySet()) {
			account.getBeneficiary(entry.getKey()).setAllocationPercentage(entry.getValue());
		}
	}

	public void addBeneficiary(Long accountId, String beneficiaryName) {
		findById(accountId).addBeneficiary(beneficiaryName, Percentage.zero());
	}

	public void removeBeneficiary(Long accountId, String beneficiaryName, Map<String, Percentage> allocationPercentages) {
		findById(accountId).removeBeneficiary(beneficiaryName);
		updateBeneficiaryAllocationPercentages(accountId, allocationPercentages);
	}

	@SuppressWarnings("unchecked")
	public List<Account> searchByAccountNumber(AccountSearchCriteria searchCriteria) {
		Criteria criteria = getCurrentSession().createCriteria(Account.class);
		criteria.add(Restrictions.like("number", searchCriteria.getAccountString(), MatchMode.START));
		criteria.setFirstResult(searchCriteria.getPage() * searchCriteria.getPageSize());
		criteria.setMaxResults(searchCriteria.getPageSize());
		return criteria.list();
	}

}