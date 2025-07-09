package exceptions;

import java.util.Map;

import interfaces.RequiredElement;

public class InformationRequiredExceptionAddress extends InformationRequired {
    private static final long serialVersionUID = 1L;
    private static final String MESSAGE = "Adres kan niet worden aangemaakt omdat niet alle info is ingevuld";
    private final Map<String, RequiredElement> requiredElements;
    
    public InformationRequiredExceptionAddress(Map<String, RequiredElement> requiredElements) {
    	super(MESSAGE);
        this.requiredElements = requiredElements;
    }

    public Map<String, RequiredElement> getRequiredElements() {
        return requiredElements;
    }
}