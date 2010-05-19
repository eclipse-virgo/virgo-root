package admin.accounts;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages access to member account information.
 */
@Service
@Transactional(readOnly=true)
public interface AccountManager {

	/**
	 * Finds an account given an account number
	 * @param number the account number
	 * @return the account or null
	 */
	Account findAccount(String number);

	/**
	 * Search for accounts given an AccountSearchCriteria instance.
	 * @param searchCriteria
	 * @return
	 */
	List<Account> findAccounts(AccountSearchCriteria searchCriteria);

	/**
	 * Load an account by its credit card.
	 * @param creditCardNumber the credit card number
	 * @return the account object
	 * @throws NoSuchAccountException if no matching account was found
	 */
	Account findByCreditCard(String creditCardNumber) throws NoSuchAccountException;

	/**
	 * Takes a changed account and persists any changes made to it.
	 * @param account The account with changes
	 */
	@Transactional
	void update(Account account);

	/**
	 * Returns all accounts
	 * @return a List of account instances.
	 */
	List<Account> getAllAccounts();

}