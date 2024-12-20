package application;

public class Property {
    private int propertyId;
    private String streetAddress;
    private String area;
    private String city;
    private String state;
    private int pincode;
    private String accommodationType;
    private double rent;
    private int openSpots;
    private String spotType;
    private int beds;
    private int baths;
    private String moveInDate;
    private String moveOutDate;
    private String propertyInfo;
    private byte[] imageData;

    public Property(int propertyId, String streetAddress, String area, String city, String state, int pincode,
                    String accommodationType, double rent, int openSpots, String spotType, int beds, int baths,
                    String moveInDate, String moveOutDate, String propertyInfo, byte[] imageData) {
        this.propertyId = propertyId;
        this.streetAddress = streetAddress;
        this.area = area;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.accommodationType = accommodationType;
        this.rent = rent;
        this.openSpots = openSpots;
        this.spotType = spotType;
        this.beds = beds;
        this.baths = baths;
        this.moveInDate = moveInDate;
        this.moveOutDate = moveOutDate;
        this.propertyInfo = propertyInfo;
        this.imageData = imageData;
    }
    
    public Property(int propertyId, double rent, String accommodationType, String spotType, byte[] imageData) {
        this.rent = rent;
        this.accommodationType = accommodationType;
        this.spotType = spotType;
        this.imageData = imageData;
        this.propertyId = propertyId;
    }

    // Getters
    public int getPropertyId() { return propertyId; }
    public String getStreetAddress() { return streetAddress; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public double getRent() { return rent; }
    public int getBeds() { return beds; }
    public int getBaths() { return baths; }
    public String getMoveInDate() { return moveInDate; }
    public String getMoveOutDate() { return moveOutDate; }
    public String getPropertyInfo() { return propertyInfo; }
    public String getAccommodationType() { return accommodationType; }
    public String getSpotType() { return spotType; }
    public byte[] getImageData() { return imageData; }
    public int getPincode() { return pincode; }
    public int getOpenSpots() { return openSpots; }
    public String getArea() { return area; }
}
