package common.repository;

import java.io.Serializable;

/**
 * A base class for all entities that use a internal long identifier for tracking entity identity.
 */
public class Entity implements Serializable {

	private Long entityId;

	/**
	 * Returns the entity identifier used to interally distinguish this entity among other entities of the same type in
	 * the system. Should typically only be called by privleged data access infrastructure code such as an Object
	 * Relational Mapper (ORM) and not by applicaiton code.
	 * @return the internal entity identifier
	 */
	public Long getEntityId() {
		return entityId;
	}

	/**
	 * Sets the internal entity identifier - should only be called by priveleged data access code (repositories that
	 * work with an Object Relational Mapper (ORM)). Should never be set by application code explicitly.
	 * @param entityId the internal entity identifier
	 */
	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}
}