package dto;

import java.time.LocalDate;
import java.time.LocalTime;

import domain.Maintenance;
import domain.Site;
import domain.User;

public record ReportDTO(Maintenance maintenance, Site site, User technician, LocalDate startDate, LocalTime startTime,
		LocalDate endDate, LocalTime endTime, String reason, String comments)
{
}