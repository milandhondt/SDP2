package exceptions;

import java.util.Map;

import interfaces.RequiredElement;
import util.RequiredElementReport;

public class InformationRequiredExceptionReport extends InformationRequired
{
	private static final long serialVersionUID = 1L;

	private Map<String, RequiredElement> missingElements;

	public InformationRequiredExceptionReport(Map<String, RequiredElement> missingElements)
	{
		super("Required information missing for report creation");
		this.missingElements = missingElements;
	}

	public Map<String, RequiredElement> getRequiredElements()
	{
		return missingElements;
	}
}