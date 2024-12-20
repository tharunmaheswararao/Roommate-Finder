package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserProfileController extends BaseController {

    @FXML
    private Label firstNameLabel;

    @FXML
    private Label lastNameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label phoneLabel;

    @FXML
    private TilePane propertyCardsContainer;
    
    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        loadUserProfile(userId);
        loadUserPosts(userId);
    }
    
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainListingPage.fxml"));
            Parent root = loader.load();

            // Pass the user_id back to MainListingPageController
            MainListingPageController mainListingPageController = loader.getController();
            mainListingPageController.setUserId(super.userId);

            // Get current stage
            Stage stage = (Stage) firstNameLabel.getScene().getWindow();
            Scene scene = new Scene(root, WelcomeController.screenWidth, WelcomeController.screenHeight);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error navigating back to MainListingPage.fxml: " + e.getMessage());
        }
    } 
    
    @FXML
    public void initialize() {
    	System.out.println("UserProfileController initialized!");
//        System.out.println("User ID received in initialize: " + super.userId);
    }
    
    private void loadUserProfile(int userId) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/rfdb", "root", "root");
             PreparedStatement pstmt = conn.prepareStatement("SELECT first_name, last_name, email_id, phone_number FROM user_data WHERE user_id = ?")) {
//        	System.out.println(userId);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                firstNameLabel.setText(rs.getString("first_name"));
                System.out.println(rs.getString("first_name"));
                lastNameLabel.setText(rs.getString("last_name"));
                emailLabel.setText(rs.getString("email_id"));
                phoneLabel.setText(rs.getString("phone_number"));
            } else {
                // Notify if no user is found
                firstNameLabel.setText("User not found");
                lastNameLabel.setText("");
                emailLabel.setText("");
                phoneLabel.setText("");
            }

        } catch (Exception e) {
            e.printStackTrace();
            firstNameLabel.setText("Error loading profile");
        }
    }

    private void loadUserPosts(int userId) {
        List<Property> properties = getUserProperties(userId);

        propertyCardsContainer.getChildren().clear();

        for (Property property : properties) {
            propertyCardsContainer.getChildren().add(createPropertyCard(property));
        }
    }

    private Button createPropertyCard(Property property) {
        Button card = new Button();
        card.setStyle("-fx-background-color: #ffffff; -fx-padding: 10; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefWidth(250); // Adjust width as needed

        // Card Layout
        VBox cardContent = new VBox();
        cardContent.setSpacing(10);
        cardContent.setStyle("-fx-padding: 10;"); // Apply consistent padding to the content

        // Centering Property Image
        if (property.getImageData() != null) {
            ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(property.getImageData())));
            imageView.setFitWidth(350);
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(true);

            // Wrap the ImageView in an HBox to center it
            HBox imageContainer = new HBox(imageView);
            imageContainer.setAlignment(javafx.geometry.Pos.CENTER); // Center the image
            cardContent.getChildren().add(imageContainer);
        }

        // Property Details
        VBox details = new VBox();
        details.setSpacing(5);

        // Add Labels for property details
        details.getChildren().add(new Label("Rent: $" + property.getRent()));
        details.getChildren().add(new Label("Accommodation: " + property.getAccommodationType()));
        details.getChildren().add(new Label("Spot Type: " + property.getSpotType()));
        
     // Edit and Delete Buttons
        HBox buttons = new HBox(10);
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");

        editButton.setOnAction(e -> handleEditProperty(property));
        deleteButton.setOnAction(e -> handleDeleteProperty(property.getPropertyId()));

        buttons.getChildren().addAll(editButton, deleteButton);

        // Add the details section to the same VBox
        cardContent.getChildren().add(details);
        cardContent.getChildren().add(buttons);

        // Set the card's graphic
        card.setGraphic(cardContent);

        // Add action to navigate to GetDetails page
        card.setOnAction(event -> navigateToGetDetails(property.getPropertyId(), super.userId));

        return card;
    }
    
    private void handleEditProperty(Property property) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EditProperty.fxml"));
            Parent root = loader.load();

            EditPropertyController controller = loader.getController();
//            controller.setPropertyDetails(property);
            controller.setUserId(super.userId);
            controller.setPropertyId(property.getPropertyId());

            Stage stage = new Stage();
            Scene scene = new Scene(root, WelcomeController.screenWidth, WelcomeController.screenHeight);
            stage.setTitle("Edit Property");
            stage.setScene(scene);
            stage.showAndWait();

            loadUserPosts(super.userId); // Refresh after edit
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteProperty(int propertyId) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/rfdb", "root", "root");
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM properties WHERE property_id = ?")) {

            pstmt.setInt(1, propertyId);
            pstmt.executeUpdate();

            loadUserPosts(super.userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToGetDetails(int propertyId, int userId) {
        try {
            // Load the GetDetails.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GetDetails.fxml"));
            Parent root = loader.load();

            // Pass property ID to the GetUserPropertyController
            GetUserPropertyController controller = loader.getController();
            controller.setPropertyId(propertyId);
            controller.setUserId(userId);

            // Get the current stage
            Stage stage = (Stage) propertyCardsContainer.getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(root, WelcomeController.screenWidth, WelcomeController.screenHeight);
            stage.setScene(scene);

            // Maximize the stage
            stage.setMaximized(true); // Ensure the window is maximized
            stage.show();

            // Debugging: Check if maximized is being applied
            System.out.println("Navigated to GetDetails.fxml and stage maximized.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error navigating to GetDetails.fxml: " + e.getMessage());
        }
    }

    private List<Property> getUserProperties(int userId) {
        List<Property> properties = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/rfdb", "root", "root");
             PreparedStatement pstmt = conn.prepareStatement("SELECT property_id, property_image, rent, accommodation_type, spot_type FROM properties WHERE created_by = ?")) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int propertyId = rs.getInt("property_id");
                byte[] imageData = rs.getBytes("property_image");
                double rent = rs.getDouble("rent");
                String accommodationType = rs.getString("accommodation_type");
                String spotType = rs.getString("spot_type");

                properties.add(new Property(propertyId, rent, accommodationType, spotType, imageData));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties;
    }

    @FXML
    private void handleUpdatePassword() {
        try {
            // Load UpdatePassword.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UpdatePassword.fxml"));
            Parent root = loader.load();
            
            UpdatePasswordController controller = loader.getController();
            controller.setUserId(userId);

            // Get current stage
            Stage stage = new Stage();
            stage.setTitle("Update Password");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading UpdatePassword.fxml: " + e.getMessage());
        }
    }
}
