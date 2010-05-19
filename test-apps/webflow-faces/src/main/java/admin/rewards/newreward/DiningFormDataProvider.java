package admin.rewards.newreward;

import java.util.List;

import javax.faces.model.SelectItem;

public interface DiningFormDataProvider {
	List<SelectItem> findAllRestaurants();
}
