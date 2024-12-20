package application;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    
    protected int user_id;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField showPasswordField;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private Text errorText;

    @FXML
    private void initialize() {
        // Bind the text of the showPasswordField and passwordField
        showPasswordField.managedProperty().bind(showPasswordField.visibleProperty());
        passwordField.managedProperty().bind(passwordField.visibleProperty());

        showPasswordField.visibleProperty().bind(togglePasswordButton.pressedProperty());
        passwordField.visibleProperty().bind(togglePasswordButton.pressedProperty().not());

        // Synchronize text between fields
        showPasswordField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.isVisible() ? passwordField.getText() : showPasswordField.getText();

        // Reset error text
        errorText.setText("");

        if (email.isEmpty() || password.isEmpty()) {
            errorText.setText("Please fill in both email and password.");
            return;
        }

        try (Connection connection = DatabaseUtil.getConnection()) {
            if (connection != null) {
                String query = "SELECT * FROM user_data WHERE email_id = ? AND password = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, password);

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    this.user_id = resultSet.getInt("user_id");
                    System.out.println("Login successful! Navigating... " + this.user_id);
                    switchToScene("MainListingPage.fxml"); // Correct path
                     
                } else {
                    errorText.setText("Invalid email or password.");
                }
            } else {
                errorText.setText("Unable to connect to the database.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errorText.setText("An unexpected error occurred.");
        }
    }

    @FXML
    private void handleForgetPasswordRedirect() {
        switchToScene("forgot_password.fxml"); // Correct path
    }

    @FXML
    private void handleSignupRedirect() {
        switchToScene("signup.fxml"); // Correct path
    }

    private void switchToScene(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof BaseController) {
                ((BaseController) controller).setUserId(this.user_id);
                System.out.println("User ID passed to " + fxmlFile + ": " + this.user_id);
            }

            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root, WelcomeController.screenWidth, WelcomeController.screenHeight);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            errorText.setText("Failed to load the page.");
        }
    }
}