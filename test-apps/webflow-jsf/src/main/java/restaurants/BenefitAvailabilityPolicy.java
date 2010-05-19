package restaurants;

import common.money.MonetaryAmount;

import accounts.Account;

/**
 * Determines if benefit is available for an account for dining.
 * 
 * A value object. A strategy. Scoped by the Resturant aggregate.
 */
public interface BenefitAvailabilityPolicy {

	/**
	 * Calculates if an account is eligible to receive benefits for a dining.
	 * @param account the account of the member who dined
	 * @param diningAmount the amount charged for the dining
	 * @return benefit availability status
	 */
	public boolean isBenefitAvailableFor(Account account, MonetaryAmount diningAmount);
}
