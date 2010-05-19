package common.repository;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

/**
 * A base class for user types that map immutable values (objects that cannot be changed after construction and may be
 * safely shared).
 */
public abstract class ImmutableValueUserType implements UserType {

	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return cached;
	}

	public Object deepCopy(Object value) throws HibernateException {
		return value;
	}

	public Serializable disassemble(Object value) throws HibernateException {
		return (Serializable) value;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		return x.equals(y);
	}

	public int hashCode(Object x) throws HibernateException {
		return x.hashCode();
	}

	public boolean isMutable() {
		return false;
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}

	// subclasses must implement the following methods

	public abstract Class returnedClass();

	public abstract int[] sqlTypes();

	public abstract Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException,
			SQLException;

	public abstract void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException,
			SQLException;

}
