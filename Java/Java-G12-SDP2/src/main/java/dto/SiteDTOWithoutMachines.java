package dto;

import util.Status;

public record SiteDTOWithoutMachines(int id, String siteName, UserDTO verantwoordelijke, Status status,
		AddressDTO address) {
}