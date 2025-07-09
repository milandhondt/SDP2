package dto;

import java.time.LocalDate;

import util.Role;
import util.Status;

public record UserDTO(int id, String firstName, String lastName, String email, String phoneNumber, LocalDate birDate,
		AddressDTO address, Role role, Status status, String password)
{
}
