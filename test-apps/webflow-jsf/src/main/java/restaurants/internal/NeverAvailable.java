package restaurants.internal;

import common.money.MonetaryAmount;

import accounts.Account;
import restaurants.BenefitAvailabilityPolicy;

/**
 * A benefit availabilty policy that returns false at all times.
 */
public class NeverAvailable implements BenefitAvailabilityPolicy {
	static final BenefitAvailabilityPolicy INSTANCE = new NeverAvailable();

	public boolean isBenefitAvailableFor(Account account, MonetaryAmount diningAmount) {
		return false;
	}

	public String toString() {
		return "neverAvailable";
	}
}
