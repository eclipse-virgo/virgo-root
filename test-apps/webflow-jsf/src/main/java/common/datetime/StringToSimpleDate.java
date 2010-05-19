package common.datetime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.springframework.binding.convert.converters.StringToObject;

public class StringToSimpleDate extends StringToObject {

	public StringToSimpleDate() {
		super(SimpleDate.class);
	}

	@Override
	protected Object toObject(String string, Class targetClass) throws Exception {
		return SimpleDate.valueOf(getDateFormat().parse(string));
	}

	@Override
	protected String toString(Object object) throws Exception {
		SimpleDate date = (SimpleDate) object;
		if (date == null) {
			return "";
		} else {
			return getDateFormat().format(date.getDate());
		}
	}

	protected DateFormat getDateFormat() {
		return new SimpleDateFormat("MM/dd/yyyy");		
	}

}
