package admin.accounts.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import admin.accounts.AccountManager;

/**
 * A controller handling requests for showing and updating an Account.
 */
@Controller
public class AccountsController {

	private AccountManager accountManager;

	@Autowired
	public AccountsController(AccountManager accountManager) {
		this.accountManager = accountManager;
	}
	
	/**
	 * A request handling method for showing an account's details. 
	 * This method does not need to find the account because the findAccount method has already done that.
	 */
	@RequestMapping(method=RequestMethod.GET)
	public void list(Model model) {
		model.addAttribute("accounts", accountManager.getAllAccounts());
	}
	
	/**
	 * A request handling method for showing an account's details. 
	 * This method does not need to find the account because the findAccount method has already done that.
	 */
	@RequestMapping(method=RequestMethod.GET)
	public void show(@RequestParam("number") String number, Model model) {
		model.addAttribute("account", accountManager.findAccount(number));
	}
	
}
