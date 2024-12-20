package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class EditPropertyController {

    private int userId;
    private int propertyId;
    private byte[] propertyImage;

    @FXML private Button backButton, uploadImageButton, saveButton;

    // Address Fields
    @FXML private TextField streetAddressField, areaField, cityField, stateField, pincodeField;

    // Property Details
    @FXML private TextField rentField;
    @FXML private Spinner<Integer> openSpotsSpinner, bedSpinner, bathSpinner;
    @FXML private ChoiceBox<String> accommodationTypeChoiceBox, spotTypeChoiceBox;

    // Dates
    @FXML private DatePicker moveInDatePicker, moveOutDatePicker;

    // Property Information
    @FXML private TextArea propertyInfoTextArea;

    // Image Path Label
    @FXML private Label imagePathLabel;

    public void setPropertyId(int propertyId) {
        this.propertyId = propertyId;
        fetchPropertyDetailsFromDatabase(); // Fetch details from the database
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @FXML
    private void initialize() {
        System.out.println("EditPropertyController initialized!");

        // Initialize choice boxes and spinners
        accommodationTypeChoiceBox.getItems().addAll("Temporary", "Permanent");
        spotTypeChoiceBox.getItems().addAll("Shared", "Private");

        openSpotsSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
        bedSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
        bathSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));

        uploadImageButton.setOnAction(e -> handleUploadImage());
        backButton.setOnAction(e -> handleBack());
    }

    private void fetchPropertyDetailsFromDatabase() {
    	System.out.println(this.propertyId);
        // Fetch property details from the database using the propertyId
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/rfdb", "root", "root");
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM properties WHERE property_id = ?")) {

            pstmt.setInt(1, propertyId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
            	System.out.println(rs.getString("st_addr"));
                streetAddressField.setText(rs.getString("st_addr"));
                areaField.setText(rs.getString("area"));
                cityField.setText(rs.getString("city"));
                stateField.setText(rs.getString("state"));
                pincodeField.setText(String.valueOf(rs.getInt("pincode")));

                rentField.setText(String.valueOf(rs.getDouble("rent")));
                openSpotsSpinner.getValueFactory().setValue(rs.getInt("open_spots"));
                bedSpinner.getValueFactory().setValue(rs.getInt("no_beds"));
                bathSpinner.getValueFactory().setValue(rs.getInt("no_baths"));

                accommodationTypeChoiceBox.setValue(rs.getString("accommodation_type"));
                spotTypeChoiceBox.setValue(rs.getString("spot_type"));

                // Handle dates
                String moveInDate = rs.getString("move_in_dt");
                if (moveInDate != null && !moveInDate.isEmpty()) {
                    moveInDatePicker.setValue(LocalDate.parse(moveInDate));
                }

                String moveOutDate = rs.getString("move_out_dt");
                if (moveOutDate != null && !moveOutDate.isEmpty()) {
                    moveOutDatePicker.setValue(LocalDate.parse(moveOutDate));
                }

                propertyInfoTextArea.setText(rs.getString("property_info"));

                propertyImage = rs.getBytes("property_image");
                if (propertyImage != null) {
                    imagePathLabel.setText("Image already uploaded");
                }
            } else {
                showAlert("Error", "Property not found in the database.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while fetching property details.");
        }
    }

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());

        if (file != null) {
            imagePathLabel.setText(file.getName());
            try {
                propertyImage = Files.readAllBytes(file.toPath());
                System.out.println("Image uploaded successfully: " + file.getName());
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error reading image file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSave() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/rfdb", "root", "root");
             PreparedStatement pstmt = conn.prepareStatement(
                     "UPDATE properties SET st_addr = ?, area = ?, city = ?, state = ?, pincode = ?, rent = ?, " +
                             "open_spots = ?, spot_type = ?, no_beds = ?, no_baths = ?, move_in_dt = ?, move_out_dt = ?, " +
                             "property_info = ?, property_image = ?, accommodation_type = ? WHERE property_id = ?")) {

            // Set values from form fields
            pstmt.setString(1, streetAddressField.getText());
            pstmt.setString(2, areaField.getText());
            pstmt.setString(3, cityField.getText());
            pstmt.setString(4, stateField.getText());
            pstmt.setInt(5, Integer.parseInt(pincodeField.getText()));
            pstmt.setDouble(6, Double.parseDouble(rentField.getText()));
            pstmt.setInt(7, openSpotsSpinner.getValue());
            pstmt.setString(8, spotTypeChoiceBox.getValue());
            pstmt.setInt(9, bedSpinner.getValue());
            pstmt.setInt(10, bathSpinner.getValue());

            // Dates
            pstmt.setObject(11, moveInDatePicker.getValue());
            pstmt.setObject(12, moveOutDatePicker.getValue());

            pstmt.setString(13, propertyInfoTextArea.getText());

            // Handle image upload
            pstmt.setBytes(14, propertyImage);

            pstmt.setString(15, accommodationTypeChoiceBox.getValue());

            // Property ID (WHERE clause)
            pstmt.setInt(16, propertyId);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Property updated successfully!");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Updated successfully!");
                alert.showAndWait();
                handleBack();
            } else {
                System.err.println("No rows updated. Check property ID.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while updating the property.");
        }
    }

    @FXML
    private void handleBack() {
        ((Stage) backButton.getScene().getWindow()).close(); // Close the form and return
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
