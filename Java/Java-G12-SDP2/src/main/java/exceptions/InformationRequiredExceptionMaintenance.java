package exceptions;

import java.util.Collections;
import java.util.Map;

import interfaces.RequiredElement;

public class InformationRequiredExceptionMaintenance extends InformationRequired
{

	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "Het onderhoud kan niet worden ingepland omdat niet alle info is ingevuld";

	private Map<String, RequiredElement> informationRequired;

	public InformationRequiredExceptionMaintenance(Map<String, RequiredElement> itemsRequired)
	{
		super(MESSAGE);
		informationRequired = itemsRequired;
	}

	public Map<String, RequiredElement> getRequiredElements()
	{
		Map<String, RequiredElement> result = Collections.unmodifiableMap(informationRequired);
		return result;
	}

}
