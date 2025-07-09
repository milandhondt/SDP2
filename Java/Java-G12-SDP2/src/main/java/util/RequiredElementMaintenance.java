package util;

import interfaces.RequiredElement;

public enum RequiredElementMaintenance implements RequiredElement {
    EXECUTION_DATE_REQUIRED(I18n.get("requiredelementmaintenance-execution_date")),
    START_DATE_REQUIRED(I18n.get("requiredelementmaintenance-start_date")),
    END_DATE_REQUIRED(I18n.get("requiredelementmaintenance-end_date")),
    TECHNICIAN_REQUIRED(I18n.get("requiredelementmaintenance-technician")),
    REASON_REQUIRED(I18n.get("requiredelementmaintenance-reason")),
    MAINTENANCESTATUS_REQUIRED(I18n.get("requiredelementmaintenance-maintenancestatus")),
    MACHINE_REQUIRED(I18n.get("requiredelementmaintenance-machine")),
    END_DATE_BEFORE_START(I18n.get("requiredelementmaintenance-end_date_before_start"));

    private final String message;

    RequiredElementMaintenance(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}