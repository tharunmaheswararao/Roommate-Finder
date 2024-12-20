package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.util.Properties;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Random;

public class SignupController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    private String generatedOTP;

    @FXML
    private void handleSignup() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate mandatory fields
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(AlertType.ERROR, "Validation Error", "All fields are mandatory!");
            return;
        }

        // Validate phone number
        if (!phone.matches("\\d{10}")) {
            showAlert(AlertType.ERROR, "Validation Error", "Phone number must be exactly 10 digits.");
            return;
        }

        // Validate password match
        if (!password.equals(confirmPassword)) {
            showAlert(AlertType.ERROR, "Validation Error", "Passwords do not match!");
            return;
        }

        // Generate OTP
        generateAndSendOTP(email);
    }

    private void generateAndSendOTP(String email) {
        Random random = new Random();
        generatedOTP = String.format("%04d", random.nextInt(10000)); // 4-digit OTP
        
        System.out.println(generatedOTP);

        // Send OTP via Email
        sendEmail(email, "Your OTP Code", "Your OTP for registration is: " + generatedOTP);

        // Prompt for OTP
        showAlert(AlertType.INFORMATION, "OTP Sent", "An OTP has been sent to your email: " + email + ". Please verify to complete registration.");
        verifyOTP();
    }

    private void sendEmail(String recipient, String subject, String messageBody) {
        final String senderEmail = "roomatefindercsye6200@gmail.com"; // Replace with your email
        final String senderPassword = "Potato@06"; // Replace with your password

        // SMTP Server Properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
    }

    private void verifyOTP() {
        TextInputDialog otpDialog = new TextInputDialog();
        otpDialog.setTitle("OTP Verification");
        otpDialog.setHeaderText("Enter the OTP sent to your email");
        otpDialog.setContentText("OTP:");

        // Show dialog and capture input
        otpDialog.showAndWait().ifPresent(inputOTP -> {
            if (inputOTP.equals(generatedOTP)) {
                createAccount();
            } else {
                showAlert(AlertType.ERROR, "Verification Failed", "Invalid OTP. Please try again.");
            }
        });
    }

    private void createAccount() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();

        try (Connection connection = DatabaseUtil.getConnection()) {
            if (connection != null) {
                String query = "INSERT INTO user_data (first_name, last_name, email_id, phone_number, password) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, firstName);
                preparedStatement.setString(2, lastName);
                preparedStatement.setString(3, email);
                preparedStatement.setString(4, phone);
                preparedStatement.setString(5, password);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    showAlert(AlertType.INFORMATION, "Signup Successful", "Your account has been created successfully!");
                    handleSigninRedirect();
                } else {
                    showAlert(AlertType.ERROR, "Signup Failed", "An error occurred while creating your account.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "An error occurred while connecting to the database.");
        }
    }

    @FXML
    private void handleSigninRedirect() {
        switchToScene("login.fxml");
    }

    private void switchToScene(String fxmlFile) {
        try {
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource(fxmlFile)), WelcomeController.screenWidth, WelcomeController.screenHeight);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
