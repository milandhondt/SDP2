package domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import exceptions.InformationRequiredExceptionAddress;
import interfaces.RequiredElement;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import util.RequiredElementAddress;
import util.RequiredElementSite;

/**
 * Represents an address entity used for users and sites.
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "addresses")
public class Address implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * The unique identifier of the address (primary key).
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	/**
	 * The name of the street.
	 */
	private String street;

	/**
	 * The house number.
	 */
	private int number;

	/**
	 * The postal code.
	 */
	private int postalcode;

	/**
	 * The city name.
	 */
	private String city;

	/**
	 * The list of users associated with this address.
	 */
	@OneToMany(mappedBy = "address")
	private List<User> users;

	/**
	 * The list of sites associated with this address.
	 */
	@OneToMany(mappedBy = "address")
	private List<Site> sites;

	/**
	 * Constructs a new Address with the specified street, number, postal code, and
	 * city.
	 *
	 * @param street     the street name
	 * @param number     the house number
	 * @param postalcode the postal code
	 * @param city       the city name
	 */
	private Address(Builder builder)
	{
		this.street = builder.street;
		this.number = builder.number;
		this.postalcode = builder.postalcode;
		this.city = builder.city;
	}
	
	/**
     * Builder class for constructing Address objects with validation.
     */
    public static class Builder {
        private String street;
        private int number;
        private int postalcode;
        private String city;

        /**
         * Sets the street name in the builder.
         *
         * @param street the street name
         * @return the builder instance for method chaining
         */
        public Builder buildStreet(String street) {
            this.street = street;
            return this;
        }

        /**
         * Sets the house number in the builder.
         *
         * @param number the house number
         * @return the builder instance for method chaining
         */
        public Builder buildNumber(int number) {
            this.number = number;
            return this;
        }

        /**
         * Sets the postal code in the builder.
         *
         * @param postalcode the postal code
         * @return the builder instance for method chaining
         */
        public Builder buildPostalcode(int postalcode) {
            this.postalcode = postalcode;
            return this;
        }

        /**
         * Sets the city name in the builder.
         *
         * @param city the city name
         * @return the builder instance for method chaining
         */
        public Builder buildCity(String city) {
            this.city = city;
            return this;
        }

        /**
         * Builds and validates the Address instance.
         *
         * @return the constructed Address instance
         * @throws InformationRequiredExceptionAddress if required fields are missing
         */
        public Address build() throws InformationRequiredExceptionAddress
        {
            validateRequiredFields();
            return new Address(this);
        }

        /**
         * Validates that all required fields are present.
         *
         * @throws InformationRequiredExceptionAddress if any required fields are missing
         */
        private void validateRequiredFields() throws InformationRequiredExceptionAddress
        {
            Map<String, RequiredElement> requiredElements = new HashMap<>();

            if (street == null || street.isEmpty())
                requiredElements.put("street", RequiredElementAddress.STREET_REQUIRED);

            if (number == 0)
                requiredElements.put("number", RequiredElementAddress.NUMBER_REQUIRED);

            if (postalcode == 0)
                requiredElements.put("postalCode", RequiredElementAddress.POSTAL_CODE_REQUIRED);

            if (city == null || city.isEmpty())
                requiredElements.put("city", RequiredElementAddress.CITY_REQUIRED);

            if (!requiredElements.isEmpty())
                throw new InformationRequiredExceptionAddress(requiredElements);
        }
    }
}

