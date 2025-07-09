package gui;

import dto.NotificationDTO;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import util.I18n;

public class NotificationDetailComponent extends VBox
{

	public NotificationDetailComponent(MainLayout layout, NotificationDTO notification)
	{
		this.setPadding(new Insets(20));
		this.setSpacing(10);

		Label title = new Label(I18n.get("notification-details-label"));
		title.getStyleClass().add("title");

		Label message = new Label(I18n.get("notification-details-message") + " " + notification.message());
		Label time = new Label(I18n.get("notification-details-time") + " " + notification.time().toString());
		Label status = new Label(I18n.get("notification-details-status") + " " + (notification.isRead() ? 
				I18n.get("notification-details-read") : 
				I18n.get("notification-details-unread") ));

		this.getChildren().addAll(title, message, time, status);

	}
}
