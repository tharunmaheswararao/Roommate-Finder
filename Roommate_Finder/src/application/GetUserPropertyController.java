package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GetUserPropertyController extends BaseController {

    @FXML
    private VBox detailsContainer;

    @FXML
    private Label detailsPlaceholder;

    private int propertyId;
    
    protected int fromMain;
    
    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
        loadPropertyDetails();
    }
    
    public void setPropertyId(int propertyId) {
        this.propertyId = propertyId;
        loadPropertyDetails(); // Trigger data loading when propertyId is set
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
    public void initialize() {
        
        if (propertyId != 0) {
            loadPropertyDetails();
        } else {
            detailsContainer.getChildren().add(new Label("No property selected."));
        }
    }

    private VBox createPropertyCard(Property property) {
        VBox card = new VBox();
        card.setSpacing(10); // Space between elements
        card.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 10; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

        // Image Section
        if (property.getImageData() != null) {
            ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(property.getImageData())));
            imageView.setFitWidth(500); // Set the desired width for the image
            imageView.setFitHeight(300); // Set the desired height for the image
            imageView.setPreserveRatio(true); // Preserve the image's aspect ratio
            card.getChildren().add(imageView); // Add the image to the card
        } else {
            Label noImageLabel = new Label("No Image Available");
            noImageLabel.setStyle("-fx-font-style: italic; -fx-text-fill: gray;");
            card.getChildren().add(noImageLabel); // Add a placeholder for missing images
        }

        // Details Section
        VBox details = new VBox();
        details.setSpacing(5);

        details.getChildren().add(new Label("Property ID: " + property.getPropertyId()));
        details.getChildren().add(new Label("Address: " + property.getStreetAddress()));
        details.getChildren().add(new Label("City: " + property.getCity()));
        details.getChildren().add(new Label("State: " + property.getState()));
        details.getChildren().add(new Label("Pincode: " + property.getPincode()));
        details.getChildren().add(new Label("Accommodation Type: " + property.getAccommodationType()));
        details.getChildren().add(new Label("Rent: $" + property.getRent()));
        details.getChildren().add(new Label("Open Spots: " + property.getOpenSpots()));
        details.getChildren().add(new Label("Spot Type: " + property.getSpotType()));
        details.getChildren().add(new Label("Beds: " + property.getBeds()));
        details.getChildren().add(new Label("Baths: " + property.getBaths()));
        details.getChildren().add(new Label("Move-In Date: " + property.getMoveInDate()));
        details.getChildren().add(new Label("Move-Out Date: " + property.getMoveOutDate()));
        details.getChildren().add(new Label("Additional Info: " + property.getPropertyInfo()));

        card.getChildren().add(details); // Add the details section below the image

        return card;
    }

    public List<Property> getPropertiesByUserId(int userId, int propertyId) {
        List<Property> propertyList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            String url = "jdbc:mysql://localhost:3306/rfdb";
            String username = "root";
            String password = "root";

            conn = DriverManager.getConnection(url, username, password);

            String sql = "SELECT property_id, st_addr, area, city, state, pincode, accommodation_type, rent, " +
                    "open_spots, spot_type, no_beds, no_baths, move_in_dt, move_out_dt, property_info, " +
                    "property_image " +
                    "FROM properties WHERE created_by = ? AND property_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, propertyId);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                int fetchedPropertyId = rs.getInt("property_id");
                String streetAddress = rs.getString("st_addr");
                String area = rs.getString("area");
                String city = rs.getString("city");
                String state = rs.getString("state");
                int pincode = rs.getInt("pincode");
                String accommodationType = rs.getString("accommodation_type");
                double rent = rs.getDouble("rent");
                int openSpots = rs.getInt("open_spots");
                String spotType = rs.getString("spot_type");
                int beds = rs.getInt("no_beds");
                int baths = rs.getInt("no_baths");
                String moveInDate = rs.getString("move_in_dt");
                String moveOutDate = rs.getString("move_out_dt");
                String propertyInfo = rs.getString("property_info");

                // Retrieve image as byte array
                byte[] imageData = rs.getBytes("property_image");

                propertyList.add(new Property(fetchedPropertyId, streetAddress, area, city, state, pincode,
                        accommodationType, rent, openSpots, spotType, beds, baths, moveInDate, moveOutDate,
                        propertyInfo, imageData));
            }
        } catch (Exception e) {
            System.err.println("Error while fetching properties: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                System.err.println("Error while closing resources: " + e.getMessage());
            }
        }

        return propertyList;
    }
    
    public List<Property> getPropertiesByUserId(int propertyId) {
        List<Property> propertyList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            String url = "jdbc:mysql://localhost:3306/rfdb";
            String username = "root";
            String password = "root";

            conn = DriverManager.getConnection(url, username, password);

            String sql = "SELECT property_id, st_addr, area, city, state, pincode, accommodation_type, rent, " +
                    "open_spots, spot_type, no_beds, no_baths, move_in_dt, move_out_dt, property_info, " +
                    "property_image " +
                    "FROM properties WHERE property_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, propertyId);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                int fetchedPropertyId = rs.getInt("property_id");
                String streetAddress = rs.getString("st_addr");
                String area = rs.getString("area");
                String city = rs.getString("city");
                String state = rs.getString("state");
                int pincode = rs.getInt("pincode");
                String accommodationType = rs.getString("accommodation_type");
                double rent = rs.getDouble("rent");
                int openSpots = rs.getInt("open_spots");
                String spotType = rs.getString("spot_type");
                int beds = rs.getInt("no_beds");
                int baths = rs.getInt("no_baths");
                String moveInDate = rs.getString("move_in_dt");
                String moveOutDate = rs.getString("move_out_dt");
                String propertyInfo = rs.getString("property_info");

                // Retrieve image as byte array
                byte[] imageData = rs.getBytes("property_image");

                propertyList.add(new Property(fetchedPropertyId, streetAddress, area, city, state, pincode,
                        accommodationType, rent, openSpots, spotType, beds, baths, moveInDate, moveOutDate,
                        propertyInfo, imageData));
            }
        } catch (Exception e) {
            System.err.println("Error while fetching properties: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                System.err.println("Error while closing resources: " + e.getMessage());
            }
        }

        return propertyList;
    }
    
    private void loadPropertyDetails() {
        detailsContainer.getChildren().clear(); // Clear existing details
        if(fromMain == 1) {
        	fromMain = 0;
        	System.out.println("Came from Main Page");
        	List<Property> properties = getPropertiesByUserId(propertyId);

            if (properties.isEmpty()) {
                detailsContainer.getChildren().add(new Label("No details found for the selected property."));
            } else {
                for (Property property : properties) {
                    detailsContainer.getChildren().add(createPropertyCard(property));
                }
            }
        } else {
        	System.out.println("Came from Profile Page");
        	System.out.println(super.userId);
            System.out.println(propertyId);
        	List<Property> properties = getPropertiesByUserId(userId, propertyId);

            if (properties.isEmpty()) {
                detailsContainer.getChildren().add(new Label("No details found for the selected property."));
            } else {
                for (Property property : properties) {
                    detailsContainer.getChildren().add(createPropertyCard(property));
                }
            }
        }
    }
}
