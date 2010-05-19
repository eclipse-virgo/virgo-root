package common.money;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class PercentageConverter implements Converter {

	public static final String CONVERTER_ID = "common.money.Percentage";
	public static final String PERCENTAGE_ID = "common.money.PercentageConverter.PERCENTAGE";

	public Object getAsObject(FacesContext context, UIComponent component, String text) {
		Assert.notNull(context);
		Assert.notNull(component);

		try {
			if (StringUtils.hasText(text)) {
				return Percentage.valueOf(text);
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new ConverterException(getMessage(context, PERCENTAGE_ID, text), e);
		}
	}

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		Assert.notNull(context);
		Assert.notNull(component);

		Percentage percentage = (Percentage) value;
		if (percentage == null) {
			return "";
		} else {
			return percentage.toString();
		}
	}

	private FacesMessage getMessage(FacesContext context, String code, Object... args) {
		String baseName = context.getApplication().getMessageBundle();
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setBasename(baseName);

		String message = messageSource.getMessage(code, args, context.getViewRoot().getLocale());
		
		return new FacesMessage(FacesMessage.SEVERITY_ERROR,message,message);
	}
}