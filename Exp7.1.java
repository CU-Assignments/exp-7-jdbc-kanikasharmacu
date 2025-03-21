// DatabaseConnection.java
import java.sql.*;

public class DatabaseConnection {
    public static void main(String[] args) {
        // JDBC URL, username, and password of MySQL server
        String url = "jdbc:mysql://localhost:3306/company";
        String user = "root";
        String password = "password";
        
        // SQL query to execute
        String query = "SELECT EmpID, Name, Salary FROM Employee";
        
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish a connection
            Connection con = DriverManager.getConnection(url, user, password);
            System.out.println("Database connected successfully");
            
            // Create a statement
            Statement stmt = con.createStatement();
            
            // Execute the query and get the result set
            ResultSet rs = stmt.executeQuery(query);
            
            // Process the result set
            System.out.println("Employee Data:");
            System.out.println("EmpID\tName\t\tSalary");
            System.out.println("-------------------------------");
            
            while (rs.next()) {
                int empId = rs.getInt("EmpID");
                String name = rs.getString("Name");
                double salary = rs.getDouble("Salary");
                
                System.out.println(empId + "\t" + name + "\t\t" + salary);
            }
            
            // Close resources
            rs.close();
            stmt.close();
            con.close();
            System.out.println("Database connection closed");
            
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
