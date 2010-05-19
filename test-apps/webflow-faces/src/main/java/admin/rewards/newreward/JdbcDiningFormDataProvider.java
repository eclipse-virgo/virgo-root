package admin.rewards.newreward;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;

@Component("diningFormDataProvider")
public class JdbcDiningFormDataProvider implements DiningFormDataProvider {

	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	public JdbcDiningFormDataProvider(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	public List<SelectItem> findAllRestaurants() {
		final List<SelectItem> items = new ArrayList<SelectItem>();
		jdbcTemplate.query("SELECT MERCHANT_NUMBER, NAME FROM T_RESTAURANT ORDER BY NAME",
				new RowCallbackHandler() {
					public void processRow(ResultSet rs) throws SQLException {
						items.add(new SelectItem(rs.getString("MERCHANT_NUMBER"), rs.getString("NAME")));
					}
				});
		return items;
	}
	
}
