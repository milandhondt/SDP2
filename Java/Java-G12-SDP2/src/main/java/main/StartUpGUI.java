package main;

import gui.MainLayout;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class StartUpGUI extends Application
{

	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void start(Stage primaryStage)
	{
		MainLayout mainLayout = new MainLayout(primaryStage);

		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/favicon-32x32.png")));
		primaryStage.setTitle("Shopfloor application");

		primaryStage.show();

	}

}