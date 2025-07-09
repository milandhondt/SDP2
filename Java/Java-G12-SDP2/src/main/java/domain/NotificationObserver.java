package domain;

import java.time.LocalDateTime;

import interfaces.Observer;
import repository.NotificationDao;
import repository.NotificationDaoJpa;

/**
 * An observer implementation that handles notification creation when updates
 * occur. This class observes changes in subjects and persists notifications to
 * the database whenever an update is received.
 */
public class NotificationObserver implements Observer
{
	private NotificationDao notificationRepo;

	/**
	 * Constructs a new NotificationObserver and initializes the notification
	 * repository. Uses NotificationDaoJpa as the concrete implementation for
	 * database operations.
	 */
	public NotificationObserver()
	{
		this.notificationRepo = new NotificationDaoJpa();
	}

	/**
	 * Handles update notifications from observed subjects. Creates and persists a
	 * new notification with the received message.
	 * 
	 * @param message the update message received from the observed subject
	 * @implNote This method wraps the database operations in a transaction: 1.
	 *           Starts a new transaction 2. Inserts the new notification 3. Commits
	 *           the transaction
	 */
	@Override
	public void update(String message)
	{
		Notification notification = new Notification(false, message, LocalDateTime.now());

		notificationRepo.startTransaction();
		notificationRepo.insert(notification);
		notificationRepo.commitTransaction();
	}
}