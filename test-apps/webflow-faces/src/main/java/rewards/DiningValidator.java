package rewards;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import accounts.AccountRepository;
import accounts.NoSuchAccountException;

import common.Validator;

public class DiningValidator implements Validator<Dining> {
	
	private static final String FIELD_CREDIT_CARD_NUMBER = "creditCardNumber";
	
	private AccountRepository accountRepository;
	
	public DiningValidator(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	/**
	 * Validate the supplied <code>dining</code>.
	 * @param dining the object that is to be validated (can be <code>null</code>) 
	 * @param errors contextual state about the validation process (never <code>null</code>) 
	 * @see ValidationUtils
	 */
	public void validate(Dining dining, Errors errors){
		
		String creditCardNumber = dining.getCreditCardNumber();
		if (StringUtils.hasText(creditCardNumber)){
			
			if (creditCardNumber.length() != 16){
				errors.rejectValue(FIELD_CREDIT_CARD_NUMBER, "error.creditCard.invalidNumber");
				
			} else {
				try {
					accountRepository.findByCreditCard(creditCardNumber);
					
				} catch (NoSuchAccountException e) {
					errors.rejectValue(FIELD_CREDIT_CARD_NUMBER, "error.account.unknownCreditCard");
				}
			}
		}
	}
	
}
