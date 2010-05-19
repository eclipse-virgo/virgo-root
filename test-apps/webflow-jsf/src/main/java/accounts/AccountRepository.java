package accounts;

import java.util.List;

import common.repository.GenericRepository;


/**
 * Loads account aggregates. Called by the reward network to find and reconstitute Account entities from an external
 * form such as a set of RDMS rows.
 * 
 * Objects returned by this repository are guaranteed to be fully-initialized and ready to use.
 */
public interface AccountRepository extends GenericRepository<Account, Long> {

	/**
	 * Load an account by its credit card.
	 * @param creditCardNumber the credit card number
	 * @return the account object
	 */
	public Account findByCreditCard(String creditCardNumber);

	/**
	 * Load an account by its account number.
	 * @param accountNumber the account number
	 * @return the account object
	 */
	Account findByAccountNumber(String accountNumber);	

	/**
	 * Search for an account
	 * @param searchCriteria the search criteria driving the search
	 * @return a list of matching accounts
	 */
	List<Account> searchByAccountNumber(AccountSearchCriteria searchCriteria);

}