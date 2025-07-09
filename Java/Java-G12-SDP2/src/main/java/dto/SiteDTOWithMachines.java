package dto;

import java.util.Set;
import domain.Address;
import domain.User;
import util.Status;

public record SiteDTOWithMachines(int id, String siteName, UserDTO verantwoordelijke, Set<MachineDTO> machines,
		Status status, AddressDTO address) {
}