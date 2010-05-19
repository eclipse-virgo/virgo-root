package common.datetime;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * A formatter for Simple date properties. Converts object values to well-formatted strings and strings back to
 * values. Usable by a data binding framework for binding user input to the model.
 */
public class SimpleDateEditor extends PropertyEditorSupport {

	@Override
	public String getAsText() {
		SimpleDate date = (SimpleDate) getValue();
		if (date == null) {
			return "";
		} else {
			return getDateFormat().format(date.getDate());
		}
	}
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		try {
			setValue(SimpleDate.valueOf(getDateFormat().parse(text)));
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}

	protected DateFormat getDateFormat() {
		return new SimpleDateFormat("MM/dd/yyyy");		
	}
}