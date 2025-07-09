/**
 * JPA implementation of the UserDao interface.
 * Provides concrete database operations for User entities using Java Persistence API.
 */
package repository;

import java.util.List;

import domain.User;
import exceptions.UserNotFoundWithEmailException;

public class UserDaoJpa extends GenericDaoJpa<User> implements UserDao
{
	/**
	 * Constructs a new UserDaoJpa instance. Initializes the underlying
	 * GenericDaoJpa with the User class.
	 */
	public UserDaoJpa()
	{
		super(User.class);
	}

	/**
	 * Retrieves a user by their email address.
	 * 
	 * @param email The email address to search for (case sensitive)
	 * @return The User object with the specified email
	 * @throws UserNotFoundWithEmailException If no user exists with the specified
	 *                                        email
	 * @throws IllegalArgumentException       If email parameter is null or empty
	 */
	@Override
	public User getByEmail(String email)
	{
		try
		{
			return em.createNamedQuery("User.getByEmail", User.class).setParameter("email", email).getSingleResult();
		} catch (Exception e)
		{
			throw new UserNotFoundWithEmailException(email);
		}
	}

	/**
	 * Retrieves all users with the 'Technieker' role.
	 * 
	 * @return List of all User objects with role 'Technieker', or empty list if
	 *         none exist
	 */
	@Override
	public List<User> getAllTechniekers()
	{
		return em.createNamedQuery("User.getAllTechniekers", User.class).getResultList();
	}
}