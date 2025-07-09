package util;

import interfaces.RequiredElement;

public enum RequiredElementSite implements RequiredElement
{
    SITE_NAME_REQUIRED(I18n.get("requiredelementsite-site_name")),
    EMPLOYEE_REQUIRED(I18n.get("requiredelementsite-employee")),
    ADDRESS_REQUIRED(I18n.get("requiredelementsite-address")),
    STATUS_REQUIRED(I18n.get("requiredelementsite-status"));
	
	private final String message;

	RequiredElementSite(String message)
	{
		this.message = message;
	}

	public String getMessage()
	{
		return message;
	}
}
