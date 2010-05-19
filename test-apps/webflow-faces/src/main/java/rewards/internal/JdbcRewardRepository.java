package rewards.internal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

import accounts.AccountContribution;

import rewards.Dining;
import rewards.NoSuchRewardException;
import rewards.Reward;
import rewards.RewardConfirmation;

import common.datetime.SimpleDate;
import common.money.MonetaryAmount;

/**
 * JDBC implementation of a reward repository that records the result of a reward transaction by inserting a reward
 * confirmation record.
 */
public class JdbcRewardRepository extends SimpleJdbcDaoSupport implements RewardRepository {

	/**
	 * Maps rows in returned JDBC result sets to Reward objects.
	 */
	private ParameterizedRowMapper<Reward> rewardMapper = new RewardMapper();

	/**
	 * Insert a confirmation row in the database for the specified dining event and 
	 * contribution amount.
	 */
	public RewardConfirmation confirmReward(AccountContribution contribution, Dining dining) {
		String sql = "insert into T_REWARD (CONFIRMATION_NUMBER, REWARD_AMOUNT, REWARD_DATE, ACCOUNT_NUMBER, DINING_MERCHANT_NUMBER, DINING_DATE, DINING_AMOUNT) values (?, ?, ?, ?, ?, ?, ?)";
		String confirmationNumber = nextConfirmationNumber();
		getSimpleJdbcTemplate().update(sql, confirmationNumber, contribution.getAmount().getBigDecimal(),
				SimpleDate.today().getDate(), contribution.getAccountNumber(), dining.getMerchantNumber(),
				dining.getDate().getDate(), dining.getAmount().getBigDecimal());
		return new RewardConfirmation(confirmationNumber, contribution);
	}

	/**
	 * Corrects the amount for the given reward.
	 * @param reward the reward to be corrected
	 */
	public int correctReward(Reward reward) {
		return getSimpleJdbcTemplate().update(
				"update T_REWARD set REWARD_AMOUNT = ? where CONFIRMATION_NUMBER = ?", 
				reward.getAmount().getDouble(),
				reward.getConfirmationNumber());
	}

	/**
	 * Find rewards.
	 */
	public List<Reward> findRewards(String merchantNumber) {
		return getSimpleJdbcTemplate()
				.query("select * from T_REWARD where DINING_MERCHANT_NUMBER = ?",
						rewardMapper, merchantNumber);
	}

	/**
	 * Find a specific reward given a confirmation number.
	 * @param confirmationNumber the confirmation number
	 * @return a matching reward
	 * @throws NoSuchRewardException - thrown if no matching exception was found
	 */
	public Reward findReward(String confirmationNumber) throws NoSuchRewardException {
		Reward reward;
		try {
			reward = getSimpleJdbcTemplate().queryForObject(
					"select * from T_REWARD where CONFIRMATION_NUMBER = ?", rewardMapper, confirmationNumber);
		} catch (DataAccessException e) {
			throw new NoSuchRewardException("Invalid findReward" + confirmationNumber, confirmationNumber);
		}
		return reward;
	}
	
	private String nextConfirmationNumber() {
		String sql = "select next value for S_REWARD_CONFIRMATION_NUMBER from DUAL_REWARD_CONFIRMATION_NUMBER";
		return getSimpleJdbcTemplate().queryForObject(sql, String.class);
	}

	/**
	 * Encapsulates the logic to map a row in a ResultSet to a Reward object.
	 */
	private static class RewardMapper implements ParameterizedRowMapper<Reward> {
		public Reward mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new Reward(
					rs.getString("CONFIRMATION_NUMBER"),
					rs.getString("ACCOUNT_NUMBER"),
					rs.getString("DINING_MERCHANT_NUMBER"), 
					SimpleDate.valueOf(rs.getDate("DINING_DATE")),
					new MonetaryAmount(rs.getDouble("REWARD_AMOUNT")));
		}
	}

}