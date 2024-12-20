package application;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ResetPasswordController {

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Text errorText;

    private String email;

    public void setEmail(String email) {
        this.email = email;
    }

    @FXML
    private void handleResetPassword() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        errorText.setText(""); // Clear previous errors

        if (password == null || password.isEmpty() || confirmPassword == null || confirmPassword.isEmpty()) {
            errorText.setText("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorText.setText("Passwords do not match.");
            return;
        }

        // Update password in the database
        if (updatePasswordInDatabase(email, password)) {
            // Redirect to login page
            redirectToLoginPage();
        } else {
            errorText.setText("Failed to update password. Please try again.");
        }
    }

    private boolean updatePasswordInDatabase(String email, String password) {
        try (Connection connection = DatabaseUtil.getConnection()) {
            if (connection == null) {
                System.out.println("Database connection failed.");
                return false;
            }

            // SQL query to update the password
            String query = "UPDATE user_data SET password = ? WHERE email_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, password); // Apply hashing here if needed
            preparedStatement.setString(2, email);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Password updated successfully for email: " + email);
                return true;
            } else {
                System.out.println("No rows updated. Email may not exist: " + email);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void redirectToLoginPage() {
        try {
            Stage stage = (Stage) passwordField.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("login.fxml")), 600, 400);
            stage.setScene(scene);
            System.out.println("Password reset successful. Redirecting to login page.");
        } catch (Exception e) {
            e.printStackTrace();
            errorText.setText("Failed to redirect to login page.");
        }
    }
}
