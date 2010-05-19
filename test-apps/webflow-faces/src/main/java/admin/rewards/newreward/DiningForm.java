package admin.rewards.newreward;

import java.io.Serializable;
import java.util.Date;

import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.util.StringUtils;

import rewards.Dining;

import common.datetime.SimpleDate;
import common.money.MonetaryAmount;

public class DiningForm implements Serializable {

	private MonetaryAmount amount;

	private String creditCardNumber;

	private String merchantNumber;

	private Date date;
	
	public MonetaryAmount getAmount() {
		return amount;
	}

	public void setAmount(MonetaryAmount amount) {
		this.amount = amount;
	}

	public String getCreditCardNumber() {
		return creditCardNumber;
	}

	public void setCreditCardNumber(String creditCardNumber) {
		this.creditCardNumber = creditCardNumber;
	}

	public String getMerchantNumber() {
		return merchantNumber;
	}

	public void setMerchantNumber(String merchantNumber) {
		this.merchantNumber = merchantNumber;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Dining createDining() {
		return new Dining(getAmount(), getCreditCardNumber(), getMerchantNumber(), SimpleDate.valueOf(date));
	}
	
	public void validateEnterDiningInformation(MessageContext messageContext){
		if (StringUtils.hasText(creditCardNumber)){
			if (creditCardNumber.length() != 16){
				messageContext.addMessage(new MessageBuilder().error()
						.source("creditCardNumber")
						.code("error.creditCard.invalidNumber").build());
			}
		}
	}
}
