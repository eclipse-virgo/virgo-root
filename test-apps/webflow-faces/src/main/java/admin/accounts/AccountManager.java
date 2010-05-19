package admin.accounts;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Manages access to member account information.
 */
@Service
public interface AccountManager {

	/**
	 * Search for accounts given an AccountSearchCriteria instance.
	 * @param searchCriteria
	 * @return
	 */
	@Transactional(readOnly=true)
	List<Account> findAccounts(AccountSearchCriteria searchCriteria);

	/**
	 * Finds an account given an account number
	 * @param number the account number
	 * @return the account or null
	 */
	@Transactional(readOnly=true)
	Account findAccount(String number);
	
	/**
	 * Takes a changed account and persists any changes made to it.
	 * @param account The account with changes
	 */
	@Transactional
	void update(Account account);

}