package common.repository;

import java.io.Serializable;
import java.util.List;

/**
 * An interface defining generic CRUD (create-read-update-delete) operations.
 * @param <T> the persistent type
 * @param <ID> the identifier type
 */
public interface GenericRepository<T, ID extends Serializable> {

	/**
	 * Find a persistent entity by its identifier
	 * @param id the identifier
	 * @return the persistent entity
	 */
	T findById(ID id);

	/**
	 * Find all persistent entities of this type
	 * @return a list of retrieved entities 
	 */
	List<T> findAll();

}