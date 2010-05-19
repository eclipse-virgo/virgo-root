package restaurants.internal;

import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import common.repository.GenericHibernateRepository;

import restaurants.Restaurant;
import restaurants.RestaurantRepository;

/**
 * Responsible for finding and modifying restaurant objects using the 
 * Hibernate API. 
 * 
 * NOTE: normally the transaction boundaries are placed on the business 
 * service layer. However since the RestaurantRepository is quite simple 
 * and does not need a business service layer the transactional boundaries 
 * are declared on the repository itself.
 */
@Transactional
public class HibernateRestaurantRepository 
	extends GenericHibernateRepository<Restaurant, Long> implements RestaurantRepository {

	/**
	 * Creates an new hibernate-based restaurant repository.
	 * @param sessionFactory the Hibernate session factory required to obtain sessions
	 */
	public HibernateRestaurantRepository(SessionFactory sessionFactory) {
		super(sessionFactory, Restaurant.class);
	}

	@Transactional(readOnly = true)
	public Restaurant findByMerchantNumber(String merchantNumber) {
		return (Restaurant) getCurrentSession().createQuery(
				"from Restaurant r where r.number = :merchantNumber")
				.setString("merchantNumber", merchantNumber).uniqueResult();
	}

}