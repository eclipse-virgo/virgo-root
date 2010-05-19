package admin.rewards;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Component;

import common.datetime.SimpleDate;
import common.money.MonetaryAmount;

@Component("rewardFinder")
public class JdbcRewardFinder {

	private SimpleJdbcTemplate jdbcTemplate;

	private RewardMapper rewardMapper = new RewardMapper();
	
	@Autowired
	public JdbcRewardFinder(DataSource dataSource) {
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}
	
	public Reward findReward(String confirmationNumber) {
		return jdbcTemplate.queryForObject(
				"select * from T_REWARD where CONFIRMATION_NUMBER = ?", rewardMapper, confirmationNumber);
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