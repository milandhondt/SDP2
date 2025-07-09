package util;

import interfaces.RequiredElement;

public enum RequiredElementReport implements RequiredElement
{
    MAINTENANCE_REQUIRED(I18n.get("requiredelementintervention-maintenance")),
    TECHNICIAN_REQUIRED(I18n.get("requiredelementintervention-technician")),
    STARTDATE_REQUIRED(I18n.get("requiredelementintervention-startdate")),
    STARTTIME_REQUIRED(I18n.get("requiredelementintervention-starttime")),
    ENDDATE_REQUIRED(I18n.get("requiredelementintervention-enddate")),
    ENDTIME_REQUIRED(I18n.get("requiredelementintervention-endtime")),
    REASON_REQUIRED(I18n.get("requiredelementintervention-reason")),
    SITE_REQUIRED(I18n.get("requiredelementintervention-site")),
    END_DATE_BEFORE_START(I18n.get("requiredelementintervention-end_date_before_start")),
    END_TIME_BEFORE_START(I18n.get("requiredelementintervention-end_time_before_start"));

	private final String message;

	RequiredElementReport(String message)
	{
		this.message = message;
	}

	public String getMessage()
	{
		return message;
	}
}