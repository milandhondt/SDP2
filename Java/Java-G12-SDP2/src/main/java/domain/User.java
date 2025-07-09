package domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exceptions.InformationRequiredExceptionAddress;
import exceptions.InformationRequiredExceptionUser;
import interfaces.Observer;
import interfaces.RequiredElement;
import interfaces.Subject;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import util.RequiredElementUser;
import util.Role;
import util.Status;

/**
 * Represents a user entity in the system. This class models all user-related
 * data and implements the Subject interface for observer pattern support.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@NamedQueries({
		@NamedQuery(name = "User.getAllWithAddress", query = "SELECT u FROM User u JOIN u.address a ORDER BY u.id"),
		@NamedQuery(name = "User.getAllTechniekers", query = "SELECT u FROM User u WHERE u.role = util.Role.TECHNIEKER"),
		@NamedQuery(name = "User.getByEmail", query = "SELECT u FROM User u WHERE u.email = :email ORDER BY u.id") })
public class User implements Serializable, Subject
{
	private static final long serialVersionUID = 1L;

	@Transient
	private List<Observer> observers = new ArrayList<>();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String firstName;
	private String lastName;
	private String email;
	private String phoneNumber;
	private String password;
	private LocalDate birthdate;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "address_id")
	private Address address;

	@Enumerated(EnumType.STRING)
	private Status status;

	@Enumerated(EnumType.STRING)
	private Role role;

	/**
	 * Constructs a new User with all required fields.
	 * 
	 * @param firstName   The user's first name
	 * @param lastName    The user's last name
	 * @param email       The user's email address
	 * @param phoneNumber The user's phone number
	 * @param password    The user's hashed password
	 * @param birthdate   The user's birthdate
	 * @param address     The user's address
	 * @param status      The user's system status
	 * @param role        The user's system role
	 */
	private User(Builder builder)
	{
		this.firstName = builder.firstName;
		this.lastName = builder.lastName;
		this.email = builder.email;
		this.phoneNumber = builder.phoneNumber;
		this.password = builder.password;
		this.birthdate = builder.birthdate;
		this.address = builder.address;
		this.status = builder.status;
		this.role = builder.role;
		this.address = builder.address;
	}

	/**
	 * Calculates the user's age based on birthdate.
	 * 
	 * @return The user's age in years
	 */
	public int getAge()
	{
		return Period.between(birthdate, LocalDate.now()).getYears();
	}

	/**
	 * Returns the user's full name.
	 * 
	 * @return Concatenation of first and last name
	 */
	public String getFullName()
	{
		return String.format("%s %s", firstName, lastName);
	}

	@Override
	public void addObserver(Observer o)
	{
		observers.add(o);
		notifyObservers("");
	}

	@Override
	public void removeObserver(Observer o)
	{
		observers.remove(o);
		notifyObservers("");
	}

	@Override
	public void notifyObservers(String message)
	{
		observers.forEach(o -> o.update(message));
	}

	/**
	 * Builder class for constructing User instances with validation. Implements a
	 * fluent interface for setting properties and includes validation of required
	 * fields before building the User object.
	 */
	public static class Builder
	{
		private String firstName;
		private String lastName;
		private String email;
		private String phoneNumber;
		private String password;
		private LocalDate birthdate;
		private Role role;
		private Status status;
		private Address address;
		Map<String, RequiredElement> requiredElements = new HashMap<>();

		/**
		 * Sets the user's first name.
		 * 
		 * @param firstName The first name to set
		 * @return The builder instance for method chaining
		 */
		public Builder buildFirstName(String firstName)
		{
			this.firstName = firstName;
			return this;
		}

		/**
		 * Sets the user's last name.
		 * 
		 * @param lastName The last name to set
		 * @return The builder instance for method chaining
		 */
		public Builder buildLastName(String lastName)
		{
			this.lastName = lastName;
			return this;
		}

		/**
		 * Sets the user's email address.
		 * 
		 * @param email The email to set
		 * @return The builder instance for method chaining
		 */
		public Builder buildEmail(String email)
		{
			this.email = email;
			return this;
		}

		/**
		 * Sets the user's phone number.
		 * 
		 * @param phoneNumber The phone number to set
		 * @return The builder instance for method chaining
		 */
		public Builder buildPhoneNumber(String phoneNumber)
		{
			this.phoneNumber = phoneNumber;
			return this;
		}

		/**
		 * Sets the user's birthdate.
		 * 
		 * @param birthdate The birthdate to set
		 * @return The builder instance for method chaining
		 */
		public Builder buildBirthdate(LocalDate birthdate)
		{
			this.birthdate = birthdate;
			return this;
		}

		/**
		 * Sets the user's role.
		 * 
		 * @param role The role to set
		 * @return The builder instance for method chaining
		 */
		public Builder buildRole(Role role)
		{
			this.role = role;
			return this;
		}

		/**
		 * Sets the user's status.
		 * 
		 * @param status The status to set
		 * @return The builder instance for method chaining
		 */
		public Builder buildStatus(Status status)
		{
			this.status = status;
			return this;
		}

		/**
		 * Sets the user's address.
		 * 
		 * @param address The address to set
		 * @return The builder instance for method chaining
		 */
		public Builder buildAddress(String street, int number, int postalcode, String city)
		{
			try {
				this.address = new Address.Builder()
						.buildStreet(street)
						.buildNumber(number)
						.buildPostalcode(postalcode)
						.buildCity(city)
						.build();
			} catch (InformationRequiredExceptionAddress ire) {
				ire.getRequiredElements().forEach((k,v) -> requiredElements.put(k, v));
			}
			return this;
		}
		
		public Builder buildPassword(String password) {
			this.password = password;
			return this;
		}

		/**
		 * Builds the User instance after validating all required fields. Automatically
		 * generates and hashes a secure password.
		 * 
		 * @return The constructed User instance
		 * @throws InformationRequiredException If any required fields are missing
		 */
		public User build() throws InformationRequiredExceptionUser
		{
			validateRequiredFields();
			return new User(this);
		}

		/**
		 * Validates that all required fields have been set.
		 * 
		 * @throws InformationRequiredException If any required fields are missing
		 */
		private void validateRequiredFields() throws InformationRequiredExceptionUser
		{
			

			if (firstName == null || firstName.isEmpty())
				requiredElements.put("firstName", RequiredElementUser.FIRST_NAME_REQUIRED);

			if (lastName == null || lastName.isEmpty())
				requiredElements.put("lastName", RequiredElementUser.LAST_NAME_REQUIRED);

			if (email == null || email.isEmpty())
				requiredElements.put("email", RequiredElementUser.EMAIL_REQUIRED);
			
			if (birthdate == null)
				requiredElements.put("birthDate", RequiredElementUser.BIRTH_DATE_REQUIRED);

			if (role == null)
				requiredElements.put("role", RequiredElementUser.ROLE_REQUIRED);

			if (status == null)
				requiredElements.put("status", RequiredElementUser.STATUS_REQUIRED);

			if (!requiredElements.isEmpty())
				throw new InformationRequiredExceptionUser(requiredElements);
		}

	}

}