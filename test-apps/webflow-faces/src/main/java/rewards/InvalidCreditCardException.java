package rewards;

public class InvalidCreditCardException extends RewardNetworkException {
	
	private static final long serialVersionUID = 3430877773930949021L;

	private String creditCardNumber;

	public InvalidCreditCardException(String creditCardNumber) {
		super("Invalid credit card '" + creditCardNumber + "'");
		this.creditCardNumber = creditCardNumber;
	}
	
	public String getCreditCardNumber() {
		return creditCardNumber;
	}
	
}
