package domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a notification in the system.
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@NamedQueries({ @NamedQuery(name = "Notification.getAllRead", query = """
		SELECT n FROM Notification n
		WHERE n.isRead = 1
		"""), @NamedQuery(name = "Notification.getAllUnread", query = """
		SELECT n FROM Notification n
		WHERE n.isRead = 0
		""") })
public class Notification implements Serializable
{

	/**
	 * Default serial version UID for serialization.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The unique identifier of the notification.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	/**
	 * Indicates whether the notification has been read.
	 */
	private boolean isRead;

	/**
	 * The textual content of the notification.
	 */
	private String message;

	/**
	 * The date and time when the notification was created or sent.
	 */
	private LocalDateTime time;

	/**
	 * Constructs a new Notification with the given read status, message, and
	 * timestamp.
	 *
	 * @param isRead  whether the notification has been read
	 * @param message the content of the notification
	 * @param time    the timestamp of the notification
	 */
	public Notification(boolean isRead, String message, LocalDateTime time)
	{
		this.isRead = isRead;
		this.message = message;
		this.time = time;
	}
}
