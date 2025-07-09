package domain;

import java.util.List;

import dto.NotificationDTO;
import repository.NotificationDao;
import repository.NotificationDaoJpa;

/**
 * Controller class for managing notifications. Provides functionality to
 * retrieve read/unread notifications and mark notifications as read. Acts as an
 * intermediary between the repository layer and DTOs.
 */
public class NotificationController
{
	private NotificationDao notificationRepo;
	private List<Notification> notificationList;

	/**
	 * Constructs a new NotificationController and initializes the notification
	 * repository. Uses NotificationDaoJpa as the concrete implementation.
	 */
	public NotificationController()
	{
		notificationRepo = new NotificationDaoJpa();
	}

	/**
	 * Retrieves all read notifications and converts them to DTOs.
	 *
	 * @return a List of NotificationDTO objects representing all read notifications
	 */
	public List<NotificationDTO> getAllRead()
	{
		return notificationRepo.getAllRead().stream().map(this::toDTO).toList();
	}

	/**
	 * Retrieves all unread notifications and converts them to DTOs.
	 *
	 * @return a List of NotificationDTO objects representing all unread
	 *         notifications
	 */
	public List<NotificationDTO> getAllUnread()
	{
		return notificationRepo.getAllUnread().stream().map(this::toDTO).toList();
	}

	/**
	 * Converts a Notification entity to a NotificationDTO.
	 *
	 * @param n the Notification entity to convert
	 * @return a NotificationDTO containing the data from the Notification entity
	 */
	private NotificationDTO toDTO(Notification n)
	{
		return new NotificationDTO(n.getId(), n.getTime(), n.getMessage(), n.isRead());
	}

	/**
	 * Marks a specific notification as read in the repository.
	 *
	 * @param id the ID of the notification to mark as read
	 */
	public void markAsRead(int id)
	{
		notificationRepo.markAsRead(id);
	}
}