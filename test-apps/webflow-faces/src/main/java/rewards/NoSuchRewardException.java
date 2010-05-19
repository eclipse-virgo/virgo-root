package rewards;

/**
 * Thrown when there is no matching reward.
 */
public class NoSuchRewardException extends Exception {

	private static final long serialVersionUID = 1L;
	private String confirmationNumber;

	public NoSuchRewardException(String message, String confirmationNumber) {
		super(message);
		this.confirmationNumber = confirmationNumber;
	}

	public String getConfirmationNumber() {
		return confirmationNumber;
	}
}
