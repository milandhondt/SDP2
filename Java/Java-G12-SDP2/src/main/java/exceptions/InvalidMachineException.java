package exceptions;

import java.util.Collections;
import java.util.Set;

import util.RequiredElementMachine;

public class InvalidMachineException extends Exception{

	private static final long serialVersionUID = 1L;
	
	private static final String MESSAGE = 
    "Machine cannot be created because further information is required";
	
    private Set<RequiredElementMachine> informationRequired;
    
    public InvalidMachineException(Set<RequiredElementMachine> itemsRequired){
        super(MESSAGE);
        informationRequired = itemsRequired;
    }
    
    public Set<RequiredElementMachine> getInformationRequired(){
        return Collections.unmodifiableSet(informationRequired); 
    }
}

