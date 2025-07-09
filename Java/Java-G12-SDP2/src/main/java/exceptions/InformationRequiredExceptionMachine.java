package exceptions;

import java.util.Collections;
import java.util.Map;

import interfaces.RequiredElement;
import util.RequiredElementMachine;

public class InformationRequiredExceptionMachine extends InformationRequired
{

	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "De machine kan niet worden aangemaakt omdat niet alle info is ingevuld";

	private Map<String, RequiredElementMachine> informationRequired;

	public InformationRequiredExceptionMachine(Map<String, RequiredElementMachine> itemsRequired)
	{
		super(MESSAGE);
		informationRequired = itemsRequired;
	}

	public Map<String, RequiredElement> getRequiredElements()
	{
		return Collections.unmodifiableMap(informationRequired);
	}

}
