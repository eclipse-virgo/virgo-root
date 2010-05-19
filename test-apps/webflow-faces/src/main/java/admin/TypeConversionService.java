package admin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;

import org.springframework.binding.convert.converters.StringToObject;
import org.springframework.binding.convert.service.DefaultConversionService;
import org.springframework.stereotype.Component;

import common.datetime.SimpleDate;
import common.money.MonetaryAmount;
import common.money.Percentage;

@Component("conversionService")
public class TypeConversionService extends DefaultConversionService {

	@Override
	protected void addDefaultConverters() {
		super.addDefaultConverters();
		addConverter(new StringToMonetaryAmount());
		addConverter(new StringToPercentage());
		addConverter(new StringToSimpleDate());
	}

	public static class StringToPercentage extends JsfConverterAdapter {
		public StringToPercentage() {
			super(Percentage.class);
		}

		@Override
		protected Object toObject(String string, Class targetClass) throws Exception {
			return Percentage.valueOf(string);
		}

		@Override
		protected String toString(Object object) throws Exception {
			Percentage amount = (Percentage) object;
			return amount.toString();
		}		
	}
	
	public static class StringToMonetaryAmount extends JsfConverterAdapter {

		public StringToMonetaryAmount() {
			super(MonetaryAmount.class);
		}

		@Override
		protected Object toObject(String string, Class targetClass) throws Exception {
			return MonetaryAmount.valueOf(string);
		}

		@Override
		protected String toString(Object object) throws Exception {
			MonetaryAmount amount = (MonetaryAmount) object;
			return amount.toString();
		}

	}
	
	public static class StringToSimpleDate extends JsfConverterAdapter {

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
			return getDateFormat().format(date.getDate());
		}

		protected DateFormat getDateFormat() {
			return new SimpleDateFormat("MM/dd/yyyy");		
		}

	}
	
	public abstract static class JsfConverterAdapter extends StringToObject implements javax.faces.convert.Converter {
		public JsfConverterAdapter(Class objectClass) {
			super(objectClass);
		}

		public Object getAsObject(FacesContext context, UIComponent comp,
				String value) throws ConverterException {
			try {
				return convertSourceToTargetClass(value, getTargetClass());
			} catch (Exception e) {
				throw new ConverterException(e);
			}
		}

		public String getAsString(FacesContext context, UIComponent comp,
				Object value) throws ConverterException {
			try {
				return (String) convertTargetToSourceClass(value, getSourceClass());
			} catch (Exception e) {
				throw new ConverterException(e);
			}
		}		
	}

}