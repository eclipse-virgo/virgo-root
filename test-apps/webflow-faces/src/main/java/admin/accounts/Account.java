package admin.accounts;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

import common.datetime.SimpleDate;

/**
 * An account for a member of the reward network. An account has one or more beneficiaries 
 * whose allocations must add up to 100%.
 * 
 * An account can make contributions to its beneficiaries. Each contribution is distributed 
 * among the beneficiaries based on an allocation.
 * 
 * An entity. An aggregate.
 */
public class Account implements Serializable {

	@SuppressWarnings("unused")
	private Long entityId;

	private String number;

	private String name;
	
	private String creditCardNumber;

	private SimpleDate dateOfBirth;
	
	private String email;
	
	private boolean receiveNewsletter;
	
	private boolean receiveMonthlyEmailUpdate;

	private Set<Beneficiary> beneficiaries = new LinkedHashSet<Beneficiary>();
	
	@SuppressWarnings("unused")
	private Account() {
	}

	public Account(String number, String name) {
		this.number = number;
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SimpleDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(SimpleDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public Date getDateOfBirthAsDate() {
		return dateOfBirth.getDate();
	}

	public void setDateOfBirthAsDate(Date dateOfBirth) {
		this.dateOfBirth = new SimpleDate(dateOfBirth.getTime());
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isReceiveNewsletter() {
		return receiveNewsletter;
	}

	public void setReceiveNewsletter(boolean receiveNewsletter) {
		this.receiveNewsletter = receiveNewsletter;
	}

	public boolean isReceiveMonthlyEmailUpdate() {
		return receiveMonthlyEmailUpdate;
	}

	public void setReceiveMonthlyEmailUpdate(boolean receiveMonthlyEmailUpdate) {
		this.receiveMonthlyEmailUpdate = receiveMonthlyEmailUpdate;
	}

	public String getCreditCardNumber() {
		return creditCardNumber;
	}

	public void setCreditCardNumber(String creditCardNumber) {
		this.creditCardNumber = creditCardNumber;
	}
	
	public Set<Beneficiary> getBeneficiaries() {
		return Collections.unmodifiableSet(beneficiaries);
	}
	
	public List<Beneficiary> getBeneficiariesAsList() {
		return Collections.unmodifiableList(new LinkedList<Beneficiary>(beneficiaries));
	}
	
	public void setBeneficiaries(Set<Beneficiary> beneficiaries) {
		this.beneficiaries = beneficiaries;
	}

	public void validate(Errors errors) {
		if (StringUtils.hasText(email)) {
			if (!email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}")) {
				errors.rejectValue("email", "error.email.invalid");
			}
		}
	}
	
	public String toString() {
		return "Number = '" + number + "', name = " + name;
	}
}