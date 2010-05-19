package accounts;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

import common.Validator;

public class AccountSearchCriteriaValidator implements Validator<AccountSearchCriteria> {

	public void validate(AccountSearchCriteria target, Errors errors) {
		validateSearchForm(target, errors);
	}

	public void validateSearchForm(AccountSearchCriteria target, Errors errors) {
		String accountString = target.getAccountString();
		if (!StringUtils.hasText(accountString )) {
			errors.rejectValue("accountString", "error.account.numberSearchStringLength");
		} else if (accountString.trim().length() < 4){
			errors.rejectValue("accountString", "error.account.numberSearchStringLength");
		}
	}
	
}