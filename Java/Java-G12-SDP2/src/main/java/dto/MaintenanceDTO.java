package dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import util.MaintenanceStatus;

public record MaintenanceDTO(int id, LocalDate executionDate, LocalDateTime startDate, LocalDateTime endDate,
		UserDTO technician, String reason, String comments, MaintenanceStatus status, MachineDTO machine)
{
}
