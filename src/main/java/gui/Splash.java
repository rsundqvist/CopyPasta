package gui;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Splash extends Preloader {
  private static final Scene splashScene = createSplashScene();
  private Stage stage;

  private static Scene createSplashScene() {
    ImageView imageView =
        new ImageView(new Image(Splash.class.getResourceAsStream("/img/splash.png"))); // 700x540
    BorderPane root = new BorderPane(imageView);
    root.setStyle("-fx-background-color: transparent;");
    root.setMouseTransparent(true);
    return new Scene(root, 700, 540, Color.TRANSPARENT);
  }

  @Override
  public void init() {}

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setScene(splashScene);
    primaryStage.centerOnScreen();
    primaryStage.initStyle(StageStyle.TRANSPARENT);
    primaryStage.show();
    this.stage = primaryStage;
  }

  @Override
  public void handleStateChangeNotification(StateChangeNotification evt) {
    if (evt.getType() == StateChangeNotification.Type.BEFORE_INIT) {
      Main main = (Main) evt.getApplication();
      main.setSplashStage(stage);
    }
  }
}
