package admin.accounts;

/**
 * Base class for all account application exceptions.
 */
public abstract class AccountException extends RuntimeException {

	/**
	 * Creates a new account application exception.
	 * @param message the message
	 */
	public AccountException(String message) {
		super(message);
	}

	/**
	 * Creates a new account application exception.
	 * @param message the message
	 * @param cause the cause
	 */
	public AccountException(String message, Throwable cause) {
		super(message, cause);
	}
}
