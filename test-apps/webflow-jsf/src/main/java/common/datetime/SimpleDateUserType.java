package common.datetime;

import java.io.Serializable;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import common.money.MonetaryAmount;

/**
 * A Hibernate user type for the MonetaryAmount type. This class enables Hibernate to map a MonetaryAmount object to and
 * from a double column type in a database.
 * @see MonetaryAmount
 */
public class SimpleDateUserType implements UserType {

	public Object assemble(Serializable cached, Object owner) throws HibernateException {
		return new SimpleDate(((Date) cached).getTime());
	}

	public Object deepCopy(Object value) throws HibernateException {
		SimpleDate amount = (SimpleDate) value;
		return new SimpleDate(amount.inMilliseconds());
	}

	public Serializable disassemble(Object value) throws HibernateException {
		SimpleDate amount = (SimpleDate) value;
		return amount.toString();
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

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
		Date value = rs.getDate(names[0]);
		if (value == null) {
			return null;
		} else {
			return new SimpleDate(value.getTime());
		}
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		if (value == null) {
			st.setNull(index, Types.DOUBLE);
		} else {
			SimpleDate amount = (SimpleDate) value;
			st.setDate(index, new Date(amount.inMilliseconds()));
		}
	}

	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}

	@SuppressWarnings("unchecked")
	public Class returnedClass() {
		return SimpleDate.class;
	}

	public int[] sqlTypes() {
		return new int[] { Types.DATE };
	}
}