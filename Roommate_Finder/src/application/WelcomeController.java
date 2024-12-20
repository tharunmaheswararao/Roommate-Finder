package application;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.fxml.FXMLLoader;

public class WelcomeController {
	
	public static int screenWidth;
	
	public static int screenHeight;

    @FXML
    private void handleLoginRedirect() {
        switchToScene("login.fxml");
    }

    @FXML
    private void handleSignupRedirect() {
        switchToScene("signup.fxml");
    }

    private void switchToScene(String fxmlFile) {
        try {
            // Get the currently active stage
            Stage stage = (Stage) Stage.getWindows().stream()
                .filter(window -> window.isShowing())
                .findFirst()
                .orElse(null);
            
            Screen screen = Screen.getPrimary();
            screenWidth = (int) screen.getVisualBounds().getWidth();
            screenHeight = (int) screen.getVisualBounds().getHeight();
            
            // Load the new FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load(), screenWidth, screenHeight);

            if (stage != null) {
                // Add the stylesheet if it exists
                String stylesheet = getClass().getResource("styles.css") != null
                        ? getClass().getResource("styles.css").toExternalForm()
                        : null;
                if (stylesheet != null) {
                    scene.getStylesheets().add(stylesheet);
                }

                // Set the new scene to the stage
                stage.setScene(scene);
//                stage.setMaximized(true);
//                stage.setMaxWidth(Double.MAX_VALUE);
                stage.show();
//                stage.widthProperty().addListener((obs, oldVal, newVal) -> {
//                    System.out.println("Stage Width: " + newVal);
//                });
//                stage.heightProperty().addListener((obs, oldVal, newVal) -> {
//                    System.out.println("Stage Height: " + newVal);
//                });
            } else {
                System.out.println("No active stage found!");
            }
        } catch (Exception e) {
            System.out.println("Error loading FXML: " + fxmlFile);
            e.printStackTrace();
        }
    }
}
