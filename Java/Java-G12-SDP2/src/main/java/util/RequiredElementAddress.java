package util;

import interfaces.RequiredElement;

public enum RequiredElementAddress implements RequiredElement {
    STREET_REQUIRED(I18n.get("requiredelementaddress-street")),
    NUMBER_REQUIRED(I18n.get("requiredelementaddress-number")),
    POSTAL_CODE_REQUIRED(I18n.get("requiredelementaddress-postal_code")),
    CITY_REQUIRED(I18n.get("requiredelementaddress-city"));

	private final String message;

	RequiredElementAddress(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
