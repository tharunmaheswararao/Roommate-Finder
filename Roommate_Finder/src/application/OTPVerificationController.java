package application;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

public class OTPVerificationController {

    @FXML
    private TextField otpField;

    @FXML
    private Text errorText;

    private String email;
    private String generatedOTP;

    public void setEmailAndOTP(String email, String otp) {
        this.email = email;
        this.generatedOTP = otp;
    }

    @FXML
    private void handleVerifyOTP() {
        String enteredOTP = otpField.getText();

        if (enteredOTP == null || enteredOTP.isEmpty()) {
            errorText.setText("Please enter the OTP.");
            return;
        }

        if (enteredOTP.equals(generatedOTP)) {
            redirectToResetPasswordPage(email);
        } else {
            errorText.setText("Invalid OTP. Please try again.");
        }
    }

    private void redirectToResetPasswordPage(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("reset_password.fxml"));
            Scene scene = new Scene(loader.load(), 600, 400);

            ResetPasswordController controller = loader.getController();
            controller.setEmail(email);

            Stage stage = (Stage) otpField.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            errorText.setText("Failed to load reset password page.");
        }
    }
}
