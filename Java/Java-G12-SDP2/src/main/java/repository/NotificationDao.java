package repository;

import java.util.List;

import domain.Notification;

public interface NotificationDao extends GenericDao<Notification>{
	public List<Notification> getAllRead();
	public List<Notification> getAllUnread();
	void markAsRead(int notificationId);
}
