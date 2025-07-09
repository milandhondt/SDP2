package util;

public enum MaintenanceStatus
{
	VOLTOOID, IN_UITVOERING, INGEPLAND;
	
    @Override
    public String toString() {
        return switch (this) {
            case VOLTOOID       -> "Voltooid";
            case IN_UITVOERING  -> "In uitvoering";
            case INGEPLAND      -> "Ingepland";
        };
    }
}
