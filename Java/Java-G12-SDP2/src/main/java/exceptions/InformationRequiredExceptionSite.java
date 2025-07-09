package exceptions;

import java.util.Collections;
import java.util.Map;

import interfaces.RequiredElement;
import util.RequiredElementSite;

public class InformationRequiredExceptionSite extends InformationRequired
{

	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "De site kan niet worden aangemaakt omdat niet alle info is ingevuld";

	private Map<String, RequiredElement> informationRequired;

	public InformationRequiredExceptionSite(Map<String, RequiredElement> itemsRequired)
	{
		super(MESSAGE);
		informationRequired = itemsRequired;
	}

	public Map<String, RequiredElement> getRequiredElements()
	{
		return Collections.unmodifiableMap(informationRequired);
	}

}
