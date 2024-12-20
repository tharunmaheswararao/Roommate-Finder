package application;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.util.Random;

public class ForgetPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    private Text errorText;

    private String generatedOTP;

    @FXML
    private void handleResetPassword() {
        String email = emailField.getText();

        if (email == null || email.isEmpty()) {
            errorText.setText("Please enter an email.");
            return;
        }

        // Simulate checking email in database
        if (!isEmailInDatabase(email)) {
            errorText.setText("Email not found in the system.");
            return;
        }

        // Generate and send OTP
        generatedOTP = generateOTP();
        sendOTPEmail(email, generatedOTP);

        // Redirect to OTP Verification Page
        redirectToOTPVerificationPage(email, generatedOTP);
    }

    private boolean isEmailInDatabase(String email) {
        // Simulate a database check (replace with actual logic)
        return true;
    }

    private String generateOTP() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private void sendOTPEmail(String email, String otp) {
        // Simulate sending an email (replace with actual email-sending logic)
        System.out.println("Sending OTP " + otp + " to email: " + email);
    }

    private void redirectToOTPVerificationPage(String email, String otp) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("otp_verification.fxml"));
            Scene scene = new Scene(loader.load(), 600, 400);

            OTPVerificationController controller = loader.getController();
            controller.setEmailAndOTP(email, otp);

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            errorText.setText("Failed to load OTP verification page.");
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource("login.fxml")), WelcomeController.screenWidth, WelcomeController.screenHeight);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            errorText.setText("Failed to load login page.");
        }
    }
}
