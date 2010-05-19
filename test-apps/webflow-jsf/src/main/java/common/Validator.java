package common;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

public interface Validator<T> {

	/**
	 * Validate the supplied <code>target</code> object.
	 * <p>The supplied {@link Errors errors} instance can be used to report
	 * any resulting validation errors.
	 * @param target the object that is to be validated (can be <code>null</code>) 
	 * @param errors contextual state about the validation process (never <code>null</code>) 
	 * @see ValidationUtils
	 */
	void validate(T target, Errors errors);
	
}
