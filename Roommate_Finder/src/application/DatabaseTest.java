package application;
import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseTest {
    public static void main(String[] args) throws ClassNotFoundException {
        // Replace with your database credentials
    	Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/rfdb"; // Update rfdb if your database name is different
        String username = "root"; // Your MySQL username
        String password = "root"; // Your MySQL password

        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            System.out.println("Database connected successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
