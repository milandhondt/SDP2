package util;

import interfaces.RequiredElement;

public enum RequiredElementMachine implements RequiredElement
{
    CODE_REQUIRED(I18n.get("requiredelementmachine-code")),
    MACHINESTATUS_REQUIRED(I18n.get("requiredelementmachine-machinestatus")),
    PRODUCTIONSTATUS_REQUIRED(I18n.get("requiredelementmachine-productionstatus")),
    LOCATION_REQUIRED(I18n.get("requiredelementmachine-location")),
    PRODUCTINFO_REQUIRED(I18n.get("requiredelementmachine-productinfo")),
    SITE_REQUIRED(I18n.get("requiredelementmachine-site")),
    TECHNICIAN_REQUIRED(I18n.get("requiredelementmachine-technician")),
    LAST_MAINTENANCE_REQUIRED(I18n.get("requiredelementmachine-last_maintenance")),
    FUTURE_MAINTENANCE_REQUIRED(I18n.get("requiredelementmachine-future_maintenance"));
	
	private final String message;

	RequiredElementMachine(String message)
	{
		this.message = message;
	}

	public String getMessage()
	{
		return message;
	}
}
