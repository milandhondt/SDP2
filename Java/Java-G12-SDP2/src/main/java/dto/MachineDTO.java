package dto;

import java.time.LocalDate;
import java.util.Objects;

import util.MachineStatus;
import util.ProductionStatus;

public record MachineDTO(int id, SiteDTOWithoutMachines site, UserDTO technician, String code,
		MachineStatus machineStatus, ProductionStatus productionStatus, String location, String productInfo,
		LocalDate lastMaintenance, LocalDate futureMaintenance, int numberDaysSinceLastMaintenance,
		double upTimeInHours)
{
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof MachineDTO other))
			return false;
		return id == other.id && code.equals(other.code);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(id, code);
	}
}