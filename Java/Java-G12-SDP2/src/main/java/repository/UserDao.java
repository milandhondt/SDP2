/**
 * Data Access Object interface for User entities.
 * Extends the generic DAO interface with user-specific operations.
 */
package repository;

import java.util.List;

import domain.User;

public interface UserDao extends GenericDao<User>
{
	/**
	 * Retrieves a user by their email address.
	 * 
	 * @param email The email address to search for (case sensitive)
	 * @return The User object with the specified email, or null if not found
	 * @throws IllegalArgumentException if email parameter is null or empty
	 */
	User getByEmail(String email);
	

	/**
	 * Retrieves all users with the 'Technieker' role.
	 * 
	 * @return A list of all User objects with role 'Technieker', or an empty list
	 *         if none exist
	 */
	List<User> getAllTechniekers();
}