package exceptions;

import java.util.Collections;
import java.util.Map;

import interfaces.RequiredElement;
import util.I18n;
import util.RequiredElementUser;

public class InformationRequiredExceptionUser extends InformationRequired
{

	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = I18n.get("user-info-required");

	private Map<String, RequiredElement> informationRequired;

	public InformationRequiredExceptionUser(Map<String, RequiredElement> itemsRequired)
	{
		super(MESSAGE);
		informationRequired = itemsRequired;
	}

	public Map<String, RequiredElement> getRequiredElements() 
	{
		return Collections.unmodifiableMap(informationRequired);
	}

}