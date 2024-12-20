package application;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class MainController {

    @FXML
    private void handleLogout() {
        // Call the method to switch scenes
        switchToScene("login.fxml");
    }

    private void switchToScene(String fxmlFile) {
        try {
            // Get the current active stage
            Stage stage = (Stage) Stage.getWindows().stream()
                .filter(window -> window.isShowing())
                .findFirst()
                .orElse(null);

            if (stage != null) {
                // Load the new FXML file
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                Scene scene = new Scene(loader.load(), 600, 400);

                // Add the stylesheet if it exists
                String stylesheet = getClass().getResource("styles.css") != null
                        ? getClass().getResource("styles.css").toExternalForm()
                        : null;

                if (stylesheet != null) {
                    scene.getStylesheets().add(stylesheet);
                }

                // Set the scene to the stage
                stage.setScene(scene);
            } else {
                System.out.println("No active stage found!");
            }
        } catch (Exception e) {
            System.out.println("Error loading FXML: " + fxmlFile);
            e.printStackTrace();
        }
    }
}
