package accounts;

import java.util.regex.Pattern;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

import common.Validator;

public class AccountValidator implements Validator<Account> {

	private AccountRepository accountRepository;
	private Pattern creditCardPattern;

	public AccountValidator(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
		this.creditCardPattern = Pattern.compile("\\d{16}");
	}

	public void validate(Account target, Errors errors) {
		validateEdit(target, errors);
	}
	
	public void validateEdit(Account target, Errors errors) {
		Account account = (Account) target;
		if (StringUtils.hasText(account.getNumber())) {
			try {
				Account existingAccount = accountRepository.findByAccountNumber(account.getNumber());
				if (! account.getEntityId().equals(existingAccount.getEntityId())) {
					errors.rejectValue("number", "error.account.numberInUse");
				}
			} catch (NoSuchAccountException e) { 
				// validation does not apply
			}
			if (! creditCardPattern.matcher(target.getCreditCardNumber()).matches()){
				errors.rejectValue("creditCardNumber", "error.creditCard.invalidNumber");
			}
		}
		if (! account.isValid()) {
			errors.reject("error.account.invalidAllocationPercentage");
		}
	}
	
}
