package rewards.internal;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import restaurants.Restaurant;
import restaurants.RestaurantRepository;
import rewards.Dining;
import rewards.InvalidCreditCardException;
import rewards.NoSuchRewardException;
import rewards.Reward;
import rewards.RewardConfirmation;
import rewards.RewardNetwork;
import rewards.RewardNetworkException;
import accounts.Account;
import accounts.AccountContribution;
import accounts.AccountRepository;
import accounts.NoSuchAccountException;

import common.money.MonetaryAmount;

/**
 * Rewards an Account for Dining at a Restaurant.
 * 
 * The sole Reward Network implementation. This object is an application-layer service responsible for coordinating with
 * the domain-layer to carry out the process of rewarding benefits to accounts for dining.
 * 
 * Said in other words, this class implements the "reward account for dining" use case.
 */
public class RewardNetworkImpl implements RewardNetwork {

	private AccountRepository accountRepository;

	private RestaurantRepository restaurantRepository;

	private RewardRepository rewardRepository;

	/**
	 * Creates a new reward network.
	 * @param accountRepository the repository for loading accounts to reward
	 * @param restaurantRepository the repository for loading restaurants that determine how much to reward
	 * @param rewardRepository the repository for recording a record of successful reward transactions
	 */
	public RewardNetworkImpl(AccountRepository accountRepository, RestaurantRepository restaurantRepository,
			RewardRepository rewardRepository) {
		this.accountRepository = accountRepository;
		this.restaurantRepository = restaurantRepository;
		this.rewardRepository = rewardRepository;
	}

    @Transactional
    public RewardConfirmation rewardAccountFor(Dining dining) throws RewardNetworkException {
            AccountContribution contribution = calculateContributionFor(dining);
            return rewardRepository.confirmReward(contribution, dining);
    }

    @Transactional(readOnly=true)
    public AccountContribution calculateContributionFor(Dining dining) throws InvalidCreditCardException {
    		Account account = null;
    		try {
                account = accountRepository.findByCreditCard(dining.getCreditCardNumber());
            } catch (NoSuchAccountException e) {
            	throw new InvalidCreditCardException(dining.getCreditCardNumber());
            }
            Restaurant restaurant = restaurantRepository.findByMerchantNumber(dining.getMerchantNumber());
            MonetaryAmount amount = restaurant.calculateBenefitFor(account, dining.getAmount());
            return account.makeContribution(amount);
    }
    
	public void cancelReward(String confirmationNumber) throws NoSuchRewardException {
		Reward reward = rewardRepository.findReward(confirmationNumber);
		Account account = accountRepository.findByAccountNumber(reward.getAccountNumber());
		account.reduceContribution(reward.getAmount());
		reward.reduce(reward.getAmount());
		rewardRepository.correctReward(reward);
	}
	
	public List<Reward> findRewardsByMerchant(String merchantNumber) {
		return rewardRepository.findRewards(merchantNumber);
	}
	
	public Reward findReward(String confirmationNumber) throws NoSuchRewardException {
		return rewardRepository.findReward(confirmationNumber);
	}

}