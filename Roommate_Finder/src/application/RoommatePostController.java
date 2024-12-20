package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RoommatePostController extends BaseController {
	
	@FXML
	private Button backButton;

    @FXML
    private TextField streetAddressField;

    @FXML
    private TextField areaField;

    @FXML
    private TextField cityField;

    @FXML
    private TextField stateField;

    @FXML
    private TextField pincodeField;

    @FXML
    private ChoiceBox<String> typeChoiceBox;

    @FXML
    private TextField costField;

    @FXML
    private Label costTypeLabel;

    @FXML
    private Spinner<Integer> openSpotsSpinner;

    @FXML
    private ChoiceBox<String> privateSharedChoiceBox;

    @FXML
    private Spinner<Integer> bedSpinner;

    @FXML
    private Spinner<Integer> bathSpinner;

    @FXML
    private DatePicker moveInDatePicker;

    @FXML
    private DatePicker moveOutDatePicker;

    @FXML
    private TextArea utilitiesTextArea;

    @FXML
    private Label imagePathLabel;
    
    @FXML
    private VBox detailsContainer;
    
    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
    }
    
    @FXML
    private void handleBack() {
        try {
            // Load the UserProfile.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainListingPage.fxml"));
            Parent root = loader.load();
            
            MainListingPageController mainListingPageController = loader.getController();
            mainListingPageController.setUserId(super.userId);

            // Get the current stage
            Stage stage = (Stage) detailsContainer.getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(root, WelcomeController.screenWidth, WelcomeController.screenHeight);
            stage.setScene(scene);

            // Ensure full screen
            stage.setMaximized(true);
            stage.setTitle("User Profile");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error navigating back to UserProfile.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void initialize() {
        // Initialize choice boxes
        typeChoiceBox.getItems().addAll("Temporary", "Permanent");
        typeChoiceBox.setValue("Temporary"); // Set default value
        typeChoiceBox.setOnAction(event -> updateCostTypeLabel());

        privateSharedChoiceBox.getItems().addAll("Private", "Shared");
        privateSharedChoiceBox.setValue("Private");

        // Initialize spinners
        openSpotsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
        bedSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1));
        bathSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1));

        // Set default cost type label
        updateCostTypeLabel();
    }

    private void updateCostTypeLabel() {
        if ("Temporary".equals(typeChoiceBox.getValue())) {
            costTypeLabel.setText("(Per Day)");
        } else if ("Permanent".equals(typeChoiceBox.getValue())) {
            costTypeLabel.setText("(Per Month)");
        }
    }

    @FXML
    private void handleUploadImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            imagePathLabel.setText(file.getAbsolutePath());
        } else {
            imagePathLabel.setText("No file selected");
        }
    }

    @FXML
    private void handleSubmitPost(ActionEvent event) throws ClassNotFoundException, SQLException {
        // Gather data from the form
        String streetAddress = streetAddressField.getText();
        String area = areaField.getText();
        String city = cityField.getText();
        String state = stateField.getText();
        String pincode = pincodeField.getText();
        String type = typeChoiceBox.getValue();
        String cost = costField.getText();
        int openSpots = openSpotsSpinner.getValue();
        int beds = bedSpinner.getValue();
        int baths = bathSpinner.getValue();
        String privateOrShared = privateSharedChoiceBox.getValue();
        String moveInDate = moveInDatePicker.getValue() != null ? moveInDatePicker.getValue().toString() : null;
        String moveOutDate = moveOutDatePicker.getValue() != null ? moveOutDatePicker.getValue().toString() : null;
        String utilities = utilitiesTextArea.getText();
        String imagePath = imagePathLabel.getText();

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
        	Class.forName("com.mysql.cj.jdbc.Driver");
            // Step 1: Establish connection to the database
            String url = "jdbc:mysql://localhost:3306/rfdb";
            String username = "root"; // Replace with your MySQL username
            String password = "root"; // Replace with your MySQL password
            conn = DriverManager.getConnection(url, username, password);

            // Step 2: Create SQL query
            String sql = "INSERT INTO properties (st_addr, area, city, state, pincode, property_image, accommodation_type, rent, open_spots, spot_type, no_beds, no_baths, move_in_dt, move_out_dt, property_info, created_by) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql);

            // Step 3: Set parameters for the query
            pstmt.setString(1, streetAddress);
            pstmt.setString(2, area);
            pstmt.setString(3, city);
            pstmt.setString(4, state);
            pstmt.setString(5, pincode);

            // Set image file as binary stream
            if (!"No file selected".equals(imagePath)) {
                FileInputStream fis = new FileInputStream(imagePath);
                pstmt.setBinaryStream(6, fis, fis.available());
            } else {
                pstmt.setNull(6, java.sql.Types.BLOB); // If no image is selected
            }

            pstmt.setString(7, type);
            pstmt.setBigDecimal(8, cost.isEmpty() ? null : new java.math.BigDecimal(cost));
            pstmt.setInt(9, openSpots);
            pstmt.setString(10, privateOrShared);
            pstmt.setInt(11, beds);
            pstmt.setInt(12, baths);
            pstmt.setString(13, moveInDate);
            pstmt.setString(14, moveOutDate);
            pstmt.setString(15, utilities);
            pstmt.setInt(16, super.userId); // Replace with the user_id if logged-in user data is available

            // Step 4: Execute the query
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Roommate post saved successfully!");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Posted successfully!");
                alert.showAndWait();

                // Navigate to UserProfile.fxml
                navigateTo("UserProfile.fxml");
            } else {
                System.out.println("Failed to save the post.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Step 5: Close resources
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void navigateTo(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Pass user_id to UserProfileController
            UserProfileController userProfileController = loader.getController();
            userProfileController.setUserId(super.userId);

            // Get the current stage
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, WelcomeController.screenWidth, WelcomeController.screenHeight);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error redirecting to UserProfile.fxml: " + e.getMessage());
        }
    }
}
