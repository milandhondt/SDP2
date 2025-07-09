package gui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import util.I18n;

public class LoadingPane extends StackPane {

    public LoadingPane() {
    	this.getStylesheets().add(getClass().getResource("/css/loading.css").toExternalForm());
        Rectangle background = new Rectangle();
        background.setFill(Color.rgb(0, 0, 0, 0.5));
        background.widthProperty().bind(widthProperty());
        background.heightProperty().bind(heightProperty());

        ProgressIndicator indicator = new ProgressIndicator();
        indicator.getStyleClass().add("custom-spinner");
        Label label = new Label(I18n.get("loading"));
        label.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        VBox content = new VBox(10, indicator, label);
        content.setAlignment(Pos.CENTER);

        getChildren().addAll(background, content);
        setAlignment(Pos.CENTER);
    }
}
