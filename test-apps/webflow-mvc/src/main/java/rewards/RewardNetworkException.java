package rewards;

public abstract class RewardNetworkException extends RuntimeException {

	private static final long serialVersionUID = -6902700182445785492L;

	public RewardNetworkException(String message) {
		super(message);
	}

	public RewardNetworkException(String message, Throwable cause) {
		super(message, cause);
	}

}
