package common.money;

import org.springframework.binding.convert.converters.StringToObject;
import org.springframework.util.StringUtils;

public class StringToMonetaryAmount extends StringToObject {

	public StringToMonetaryAmount() {
		super(MonetaryAmount.class);
	}

	@Override
	protected Object toObject(String string, Class targetClass) throws Exception {
		if (StringUtils.hasText(string)) {
			return MonetaryAmount.valueOf(string);
		} else {
			return null;
		}
	}

	@Override
	protected String toString(Object object) throws Exception {
		MonetaryAmount amount = (MonetaryAmount) object;
		if (amount == null) {
			return "";
		} else {
			return amount.toString();
		}
	}

}
