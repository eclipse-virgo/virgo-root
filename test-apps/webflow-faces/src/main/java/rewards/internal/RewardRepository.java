package rewards.internal;

import java.util.List;

import accounts.AccountContribution;

import rewards.Dining;
import rewards.NoSuchRewardException;
import rewards.Reward;
import rewards.RewardConfirmation;

/**
 * Handles creating records of reward transactions to track contributions made to accounts for dining at restaurants.
 */
public interface RewardRepository {

	/**
	 * Create a record of a reward that will track a contribution made to an account for dining.
	 * @param contribution the account contribution that was made
	 * @param dining the dining event that resulted in the account contribution
	 * @return a reward confirmation object that can be used for reporting and to lookup the reward details at a later
	 * date
	 */
	RewardConfirmation confirmReward(AccountContribution contribution, Dining dining);

	/**
	 * Finds rewards.
	 * @param merchantNumber the merchant number where the dining occurred
	 * @return a list of matching rewards or an empty list
	 */
	List<Reward> findRewards(String merchantNumber);

	/**
	 * Find a specific reward given a confirmation number.
	 * @param confirmationNumber the confirmation number
	 * @return a matching reward
	 * @throws NoSuchRewardException - thrown if no matching exception was found
	 */
	Reward findReward(String confirmationNumber) throws NoSuchRewardException;

	/**
	 * Corrects the amount for the given reward
	 * @param reward the reward to be corrected
	 */
	int correctReward(Reward reward);

}