package repository;

import java.util.List;

import domain.Notification;

public class NotificationDaoJpa extends GenericDaoJpa<Notification> implements NotificationDao {

	public NotificationDaoJpa() {
		super(Notification.class);
	}

	@Override
	public List<Notification> getAllRead() {
		return em.createNamedQuery("Notification.getAllRead", Notification.class).getResultList();
	}

	@Override
	public List<Notification> getAllUnread() {
		return em.createNamedQuery("Notification.getAllUnread", Notification.class).getResultList();
	}

	@Override
	public void markAsRead(int notificationId) {
		Notification notification = em.find(Notification.class, notificationId);
		if (notification != null && !notification.isRead()) {
			em.getTransaction().begin();
			notification.setRead(true);
			em.getTransaction().commit();
		}
	}

}
