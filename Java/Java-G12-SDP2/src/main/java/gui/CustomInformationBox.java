package gui;

import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class CustomInformationBox extends HBox
{
	private FontIcon icon;
	private String text;

	public CustomInformationBox(String text)
	{
		this.icon = new FontIcon(BootstrapIcons.INFO_CIRCLE);
		this.text = text;
		buildGui();
	}

	public CustomInformationBox(FontIcon icon, String text)
	{
		this.icon = icon;
		this.text = text;
		buildGui();
	}

	private void buildGui()
	{
		Text label = new Text(text);
		label.setStyle("-fx-font: 12 arial;");

		this.setSpacing(8);
		this.setAlignment(Pos.CENTER_LEFT);

		icon.setIconSize(16);
		icon.setIconColor(Color.BLACK);
		this.getChildren().addAll(icon, label);

		this.setStyle(
				"-fx-border-color: black;" + "-fx-border-width: 1;" + "-fx-border-radius: 4;" + "-fx-padding: 6 12;");
	}
}
