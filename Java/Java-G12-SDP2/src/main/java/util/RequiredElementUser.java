package util;

import interfaces.RequiredElement;

public enum RequiredElementUser implements RequiredElement
{
    FIRST_NAME_REQUIRED(I18n.get("requiredelementuser-first_name")),
    LAST_NAME_REQUIRED(I18n.get("requiredelementuser-last_name")),
    EMAIL_REQUIRED(I18n.get("requiredelementuser-email")),
    BIRTH_DATE_REQUIRED(I18n.get("requiredelementuser-birth_date")),
    ADDRESS_REQUIRED(I18n.get("requiredelementuser-address")),
    ROLE_REQUIRED(I18n.get("requiredelementuser-role")),
    STATUS_REQUIRED(I18n.get("requiredelementuser-status"));
	
	private final String message;

	RequiredElementUser(String message)
	{
		this.message = message;
	}

	public String getMessage()
	{
		return message;
	}
}
