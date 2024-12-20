package application;

import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javafx.scene.layout.BorderPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainListingPageController extends BaseController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private TextField searchBar;

    @FXML
    private VBox filtersSidebar;

    @FXML
    private Button toggleFiltersButton;

    @FXML
    private ImageView filterIcon;

    private boolean filtersVisible = false;

    @FXML
    private DatePicker moveInDateFilter;

    @FXML
    private DatePicker moveOutDateFilter;

    @FXML
    private ComboBox<String> locationFilter;

    @FXML
    private ComboBox<String> roomTypeFilter;

    @FXML
    private TextField minBudgetFilter;

    @FXML
    private TextField maxBudgetFilter;

    @FXML
    private ComboBox<String> typeFilter;

    @FXML
    private GridPane listingsGrid;

    @FXML
    private Button homeButton;

    @FXML
    private Button NewPostButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button logOutButton;
    
    @FXML
    private Button clearFilters;
    
    @Override
    public void setUserId(int userId) {
        super.setUserId(userId);
//        initializeUserSpecificData();
        loadListings();
    }
    
    @FXML
    public void initialize() {
        System.out.println("Controller initialized!");

        // Populate Location Filter
        locationFilter.setItems(FXCollections.observableArrayList(
            "Back Bay",
            "Huntington Avenue",
            "Newbury Street",
            "Roxbury",
            "Dorchester",
            "Allston",
            "Brookline",
            "Mission Main",
            "Jamaica Plain",
            "Fenway-Kenmore"
        ));

        // Populate other filters
        roomTypeFilter.setItems(FXCollections.observableArrayList("Shared", "Private"));
        typeFilter.setItems(FXCollections.observableArrayList("Temporary", "Permanent"));

        // Initialize sidebar as hidden
        filtersSidebar.setVisible(false);
        filtersSidebar.setManaged(false);

        // Set default button highlighting
        highlightButton(homeButton); // Highlight "Home" as default
        

        // Add click listeners for top bar buttons
        homeButton.setOnAction(event -> {
        	highlightButton(homeButton);
//        	navigateTo("MainListingPage.fxml", super.userId);
        });
        NewPostButton.setOnAction(event -> {
            highlightButton(NewPostButton);
            navigateTo("RoommatePost.fxml", super.userId);
        });
        profileButton.setOnAction(event -> {
            highlightButton(profileButton);
            navigateTo("UserProfile.fxml", super.userId);
        });
        logOutButton.setOnAction(event -> {
            highlightButton(logOutButton);
            logOut();
        });

        // Load the filter icon
        filterIcon.setImage(new Image(getClass().getResourceAsStream("/images/filter-icon.png")));

        // Set toggle functionality
        toggleFiltersButton.setOnAction(event -> toggleFilters());

        // Validate numeric input for budget fields
        addNumericValidation(minBudgetFilter);
        addNumericValidation(maxBudgetFilter);
        
        clearFilters.setOnAction(event -> {
        	locationFilter.setValue(null);
            typeFilter.setValue(null);
            roomTypeFilter.setValue(null);

            // Reset TextFields
            minBudgetFilter.clear();
            maxBudgetFilter.clear();

            // Reset DatePickers
            moveInDateFilter.setValue(null);
            moveOutDateFilter.setValue(null);

            System.out.println("Filters cleared.");
        });

    }

    private void toggleFilters() {
        if (filtersVisible) {
            // Hide filters
            rootPane.setRight(null);
            filtersSidebar.setVisible(false);
            filtersSidebar.setManaged(false);
        } else {
            // Show filters
            rootPane.setRight(filtersSidebar);
            filtersSidebar.setVisible(true);
            filtersSidebar.setManaged(true);
        }
        filtersVisible = !filtersVisible;
    }

    private void highlightButton(Button selectedButton) {
        // Reset styles for all buttons
        homeButton.getStyleClass().remove("active-tab");
        NewPostButton.getStyleClass().remove("active-tab");
        profileButton.getStyleClass().remove("active-tab");
        logOutButton.getStyleClass().remove("active-tab");

        // Add style to the selected button
        selectedButton.getStyleClass().add("active-tab");
    }

    private void navigateTo(String fxmlFile, int userId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Pass user_id to the appropriate controller
            if (fxmlFile.equals("UserProfile.fxml")) {
                UserProfileController userProfileController = loader.getController();
                userProfileController.setUserId(userId);
                System.out.println("User ID passed to UserProfileController: " + userId);
            } else if (fxmlFile.equals("RoommatePost.fxml")) {
                RoommatePostController roommatePostController = loader.getController();
                roommatePostController.setUserId(userId);
                System.out.println("User ID passed to RoommatePostController: " + userId);
            }

            // Get the current stage and set the new scene
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root, WelcomeController.screenWidth, WelcomeController.screenHeight);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println("Failed to navigate to: " + fxmlFile);
            e.printStackTrace();
        }
    }

    private void addNumericValidation(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                textField.setText(newValue.replaceAll("[^\\d]", "")); // Allow only digits
            }
        });
    }
    
    private List<Property> getProperties() {
        List<Property> properties = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/rfdb", "root", "root");
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT property_id, property_image, rent, accommodation_type, spot_type FROM properties")) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int propertyId = rs.getInt("property_id");
                byte[] imageData = rs.getBytes("property_image");
                double rent = rs.getDouble("rent");
                String accommodationType = rs.getString("accommodation_type");
                String spotType = rs.getString("spot_type");

                // Create and add a Property object
                properties.add(new Property(propertyId, rent, accommodationType, spotType, imageData));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties;
    }


    private void loadListings() {
        // Get all properties
        List<Property> properties = getProperties();

        // Clear existing grid items
        listingsGrid.getChildren().clear();

        int row = 0, col = 0;

        // Iterate through properties and add to the grid
        for (Property property : properties) {
            Button propertyCard = createPropertyCard(property);
            listingsGrid.add(propertyCard, col, row);

            col++;
            if (col == 3) { // Adjust for a 3-column layout
                col = 0;
                row++;
            }
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
            imageContainer.setAlignment(javafx.geometry.Pos.CENTER);
            cardContent.getChildren().add(imageContainer);
        }

        // Property Details
        VBox details = new VBox();
        details.setSpacing(5);
        details.getChildren().add(new Label("Rent: $" + property.getRent()));
        details.getChildren().add(new Label("Accommodation: " + property.getAccommodationType()));
        details.getChildren().add(new Label("Spot Type: " + property.getSpotType()));

        // Add the details section to the same VBox
        cardContent.getChildren().add(details);

        // Set the card's graphic
        card.setGraphic(cardContent);

        // Add action to navigate to GetDetails page
        card.setOnAction(event -> navigateToGetDetails(property.getPropertyId()));

        return card;
    }
    
    private void navigateToGetDetails(int propertyId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GetDetails.fxml"));
            Parent root = loader.load();
            
            GetUserPropertyController getUserPropertyController = loader.getController();
            getUserPropertyController.setUserId(userId);
            System.out.println("User ID passed to UserProfileController: " + userId);

            // Pass property ID to the GetUserPropertyController
            GetUserPropertyController controller = loader.getController();
            controller.fromMain = 1;
            controller.setPropertyId(propertyId);

            // Get the current stage
            Stage stage = (Stage) listingsGrid.getScene().getWindow();

            // Set the new scene
            Scene scene = new Scene(root, WelcomeController.screenWidth, WelcomeController.screenHeight);
            stage.setScene(scene);

            // Maximize the stage
            stage.setMaximized(true); // Ensure the window is maximized
            stage.show();

            System.out.println("Navigated to GetDetails.fxml and stage maximized.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error navigating to GetDetails.fxml: " + e.getMessage());
        }
    }


    @FXML
    private void applyFilters() {
        System.out.println("applyFilters method called.");

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/rfdb", "root", "root")) {
            StringBuilder query = new StringBuilder("SELECT * FROM properties WHERE 1=1");
            List<Object> parameters = new ArrayList<>();

            if (minBudgetFilter.getText() != null && !minBudgetFilter.getText().isEmpty()) {
                query.append(" AND rent >= ?");
                parameters.add(Double.parseDouble(minBudgetFilter.getText()));
            }
            if (maxBudgetFilter.getText() != null && !maxBudgetFilter.getText().isEmpty()) {
                query.append(" AND rent <= ?");
                parameters.add(Double.parseDouble(maxBudgetFilter.getText()));
            }
            if (locationFilter.getValue() != null && !locationFilter.getValue().isEmpty()) {
                query.append(" AND area = ?");
                parameters.add(locationFilter.getValue());
            }
            if (roomTypeFilter.getValue() != null && !roomTypeFilter.getValue().isEmpty()) {
                query.append(" AND spot_type = ?");
                parameters.add(roomTypeFilter.getValue());
            }
            if (typeFilter.getValue() != null && !typeFilter.getValue().isEmpty()) {
                query.append(" AND accommodation_type = ?");
                parameters.add(typeFilter.getValue());
            }
            if (moveInDateFilter.getValue() != null) {
                query.append(" AND move_in_dt >= ?");
                parameters.add(java.sql.Date.valueOf(moveInDateFilter.getValue()));
            }
            if (moveOutDateFilter.getValue() != null) {
                query.append(" AND move_out_dt <= ?");
                parameters.add(java.sql.Date.valueOf(moveOutDateFilter.getValue()));
            }

            PreparedStatement pstmt = conn.prepareStatement(query.toString());
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            listingsGrid.getChildren().clear();
            int row = 0, col = 0;

            while (rs.next()) {
                Property property = new Property(
                    rs.getInt("property_id"),
                    rs.getDouble("rent"),
                    rs.getString("accommodation_type"),
                    rs.getString("spot_type"),
                    rs.getBytes("property_image")
                );

                Button propertyCard = createPropertyCard(property);
                listingsGrid.add(propertyCard, col, row);

                col++;
                if (col == 3) {
                    col = 0;
                    row++;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showErrorPopup("Database Error", "Could not retrieve properties. Please try again.");
        } catch (NumberFormatException e) {
            showErrorPopup("Input Error", "Please ensure budget fields contain valid numbers.");
        } catch (Exception e) {
            showErrorPopup("Error", "An unexpected error occurred. Please try again.");
        }
    }


    private void logOut() {
        System.out.println("Logging out...");
        navigateTo("login.fxml", super.userId);
    }

    private void showErrorPopup(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}