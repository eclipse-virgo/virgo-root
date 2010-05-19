package rewards;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import accounts.AccountContribution;

/**
 * Rewards a member account for dining at a restaurant.
 * 
 * A reward takes the form of a monetary contribution made to an account that is distributed among the account's
 * beneficiaries. The contribution amount is typically a function of several factors such as the dining amount and
 * restaurant where the dining occurred.
 * 
 * Example: Papa Keith spends $100.00 at Apple Bee's resulting in a $8.00 contribution to his account that is
 * distributed evenly among his beneficiaries Annabelle and Corgan.
 * 
 * This is the central application-boundary for the "rewards" application. This is the public interface users call to
 * invoke the application. This is the entry-point into the Application Layer.
 */
public interface RewardNetwork {

	/**
	 * Reward an account for dining.
	 * 
	 * For a dining to be eligible for reward: - It must have been paid for by a registered credit card of a valid
	 * member account in the network. - It must have taken place at a restaurant participating in the network.
	 * 
	 * @param dining a charge made to a credit card for dining at a restaurant
	 * @return confirmation of the reward
	 */
	@Transactional
	RewardConfirmation rewardAccountFor(Dining dining) throws RewardNetworkException;

	/**
	 * Sets up account contributions for the given dining information. 
	 * @param dining details about the dining
	 * @return an AccountContribution instance with distributions.
	 */
	@Transactional(readOnly=true)
	AccountContribution calculateContributionFor(Dining dining) throws RewardNetworkException;

	/**
	 * Finds a reward by the given confirmationNumber reverses its amount and also
	 * reduces the account distributions accordingly. 
	 * @param confirmationNumber the confirmation number identifying the reward
	 * @throws NoSuchRewardException thrown if no matching reward could be found
	 */
	@Transactional
	void cancelReward(String confirmationNumber) throws NoSuchRewardException;
	
	/**
	 * Finds rewards.
	 * @param merchantNumber the merchant number where the dining occurred
	 * @return a list of matching rewards or an empty list
	 */
	@Transactional(readOnly=true)
	List<Reward> findRewardsByMerchant(String merchantNumber);

	/**
	 * Finds a reward.
	 * @param confirmationNumber the number uniquely identifying the reward
	 * @return a matching reward
	 * @exception thrown if the confirmation number did not match to anything
	 */
	@Transactional(readOnly=true)
	Reward findReward(String confirmationNumber) throws NoSuchRewardException;

}