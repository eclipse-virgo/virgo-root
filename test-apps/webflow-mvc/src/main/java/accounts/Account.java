package accounts;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import accounts.AccountContribution.Distribution;

import common.money.MonetaryAmount;
import common.money.Percentage;
import common.repository.Entity;

/**
 * An account for a member of the reward network. An account has one or more beneficiaries whose allocations must add up
 * to 100%.
 * 
 * An account can make contributions to its beneficiaries. Each contribution is distributed among the beneficiaries
 * based on an allocation.
 * 
 * An entity. An aggregate.
 */
public class Account extends Entity {

	private String number;

	private String name;

	private SortedSet<Beneficiary> beneficiaries = new TreeSet<Beneficiary>(new BeneficiaryComparator());

	private String creditCardNumber;

	@SuppressWarnings("unused")
	private Account() {
	}

	/**
	 * Create a new account.
	 * @param number the account number
	 * @param name the name on the account
	 */
	public Account(String number, String name) {
		this.number = number;
		this.name = name;
	}

	/**
	 * Getter for the credit card number for this account.
	 * 
	 * @return the credit card number for this account as a 16-character String.
	 */
	public String getCreditCardNumber() {
		return creditCardNumber;
	}

	/**
	 * Setter for the credit card number for this account.
	 * 
	 * @param creditCardNumber
	 */
	public void setCreditCardNumber(String creditCardNumber) {
		this.creditCardNumber = creditCardNumber;
	}

	/**
	 * Returns the number used to uniquely identify this account.
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Sets the number used to uniquely identify this account.
	 * @param number The number for this account
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * Returns the name on file for this account.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name on file for this account.
	 * @param name The name for this account
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Add a single beneficiary with a 0% allocation percentage.
	 * @param beneficiaryName the name of the beneficiary (should be unique)
	 */
	public Beneficiary addBeneficiary(String beneficiaryName) {
		return addBeneficiary(beneficiaryName, Percentage.zero());
	}

	/**
	 * Add a single beneficiary with the specified allocation percentage.
	 * @param beneficiaryName the name of the beneficiary (should be unique)
	 * @param allocationPercentage the beneficiary's allocation percentage within this account
	 */
	public Beneficiary addBeneficiary(String beneficiaryName, Percentage allocationPercentage) {
		Beneficiary beneficiary = new Beneficiary(beneficiaryName, allocationPercentage);
		beneficiaries.add(beneficiary);
		return beneficiary;
	}

	/**
	 * Validation check that returns true only if the total beneficiary allocation adds up to 100%.
	 */
	public boolean isValid() {
		if (beneficiaries.size() == 0) {
			return true;
		}
		Percentage totalPercentage = Percentage.zero();
		for (Beneficiary b : beneficiaries) {
			try {
				totalPercentage = totalPercentage.add(b.getAllocationPercentage());
			} catch (IllegalArgumentException e) {
				// total would have been over 100% - return invalid
				return false;
			}
		}
		if (totalPercentage.equals(Percentage.oneHundred())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Make a monetary contribution to this account. The contribution amount is distributed among the account's
	 * beneficiaries based on each beneficiary's allocation percentage.
	 * @param amount the total amount to contribute
	 * @param contribution the contribution summary
	 */
	public AccountContribution makeContribution(MonetaryAmount amount) {
		if (!isValid()) {
			throw new IllegalStateException(
					"Cannot make contributions to this account: it has invalid beneficiary allocations");
		}
		Set<Distribution> distributions = distribute(amount);
		return new AccountContribution(getNumber(), amount, distributions);
	}

	/**
	 * Reverse a monetary contribution previously to this account. The contribution amount is
	 * reversed according to the allocations set for each beneficiary.
	 * @param amount the total amount to reverse
	 * @param contribution the contribution summary
	 */
	public AccountContribution reduceContribution(MonetaryAmount amount) {
		return makeContribution(amount.multiplyBy(BigDecimal.valueOf(-1)));
	}

	/**
	 * Distribute the contribution amount among this account's beneficiaries.
	 * @param amount the total contribution amount
	 * @return the individual beneficiary distributions
	 */
	private Set<Distribution> distribute(MonetaryAmount amount) {
		Set<Distribution> distributions = new HashSet<Distribution>(beneficiaries.size());
		for (Beneficiary beneficiary : beneficiaries) {
			MonetaryAmount distributionAmount = amount.multiplyBy(beneficiary.getAllocationPercentage());
			beneficiary.credit(distributionAmount);
			Distribution distribution = new Distribution(beneficiary.getName(), distributionAmount, beneficiary
					.getAllocationPercentage(), beneficiary.getSavings());
			distributions.add(distribution);
		}
		return distributions;
	}

	/**
	 * Returns the beneficiaries for this account. Callers should not attempt to hold on or modify the returned set.
	 * This method should only be used transitively; for example, called to facilitate account reporting.
	 * @return the beneficiaries of this account
	 */
	public SortedSet<Beneficiary> getBeneficiaries() {
		return Collections.unmodifiableSortedSet(beneficiaries);
	}

	/**
	 * Returns a single account beneficiary. Callers should not attempt to hold on or modify the returned object. This
	 * method should only be used transitively; for example, called to facilitate reporting or testing.
	 * @param name the name of the beneficiary e.g "Annabelle"
	 * @return the beneficiary object
	 */
	public Beneficiary getBeneficiary(String name) {
		for (Beneficiary b : beneficiaries) {
			if (b.getName().equals(name)) {
				return b;
			}
		}
		throw new IllegalArgumentException("No such beneficiary with name '" + name + "'");
	}

	/**
	 * Used to restore an allocated beneficiary. Should only be called by the repository responsible for reconstituting
	 * this account.
	 * @param beneficiary the beneficiary
	 */
	void restoreBeneficiary(Beneficiary beneficiary) {
		beneficiaries.add(beneficiary);
	}


	/**
	 * Removes a single beneficiary from this account.
	 * @param beneficiaryName the name of the beneficiary (should be unique)
	 */
	public void removeBeneficiary(String beneficiaryName) {
		beneficiaries.remove(getBeneficiary(beneficiaryName));
	}
	
	/**
	 * Removes a single beneficiary from this account.
	 * @param beneficiary the beneficiary (should be unique)
	 */
	public void removeBeneficiary(Beneficiary beneficiary) {
		beneficiaries.remove(beneficiary);
	}
	
	public String toString() {
		return "Number = '" + number + "', name = " + name + "', beneficiaries = " + beneficiaries;
	}

}