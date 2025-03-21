// ProductManager.java
import java.sql.*;
import java.util.Scanner;

public class ProductManager {
    // JDBC URL, username, and password of MySQL server
    private static final String URL = "jdbc:mysql://localhost:3306/inventory";
    private static final String USER = "root";
    private static final String PASSWORD = "password";
    
    private static Connection connection = null;
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish connection
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            connection.setAutoCommit(false); // Enable transaction management
            
            System.out.println("Connected to the database successfully");
            
            // Main menu loop
            boolean exit = false;
            while (!exit) {
                System.out.println("\n===== Product Management System =====");
                System.out.println("1. Create new product");
                System.out.println("2. View all products");
                System.out.println("3. View product by ID");
                System.out.println("4. Update product");
                System.out.println("5. Delete product");
                System.out.println("6. Exit");
                System.out.print("Enter your choice: ");
                
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline
                
                switch (choice) {
                    case 1: createProduct(); break;
                    case 2: viewAllProducts(); break;
                    case 3: viewProductById(); break;
                    case 4: updateProduct(); break;
                    case 5: deleteProduct(); break;
                    case 6: 
                        exit = true;
                        System.out.println("Exiting the application");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
            
            // Close connection
            if (connection != null) {
                connection.close();
                System.out.println("Database connection closed");
            }
            
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
    
    // Create a new product
    private static void createProduct() {
        try {
            System.out.println("\n----- Create New Product -----");
            
            System.out.print("Enter Product ID: ");
            int productId = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            System.out.print("Enter Product Name: ");
            String productName = scanner.nextLine();
            
            System.out.print("Enter Price: ");
            double price = scanner.nextDouble();
            
            System.out.print("Enter Quantity: ");
            int quantity = scanner.nextInt();
            
            String sql = "INSERT INTO Product (ProductID, ProductName, Price, Quantity) VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, productId);
                pstmt.setString(2, productName);
                pstmt.setDouble(3, price);
                pstmt.setInt(4, quantity);
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    connection.commit();
                    System.out.println("Product created successfully!");
                } else {
                    connection.rollback();
                    System.out.println("Failed to create product.");
                }
            }
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println("Error creating product: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            scanner.nextLine(); // Clear input buffer
        }
    }
    
    // View all products
    private static void viewAllProducts() {
        try {
            System.out.println("\n----- All Products -----");
            
            String sql = "SELECT * FROM Product";
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                displayProductResults(rs);
            }
            
        } catch (SQLException e) {
            System.out.println("Error viewing products: " + e.getMessage());
        }
    }
    
    // View product by ID
    private static void viewProductById() {
        try {
            System.out.println("\n----- View Product by ID -----");
            
            System.out.print("Enter Product ID: ");
            int productId = scanner.nextInt();
            
            String sql = "SELECT * FROM Product WHERE ProductID = ?";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, productId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    displayProductResults(rs);
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Error viewing product: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            scanner.nextLine(); // Clear input buffer
        }
    }
    
    // Update product
    private static void updateProduct() {
        try {
            System.out.println("\n----- Update Product -----");
            
            System.out.print("Enter Product ID to update: ");
            int productId = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            // First, check if the product exists
            String checkSql = "SELECT * FROM Product WHERE ProductID = ?";
            
            try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                checkStmt.setInt(1, productId);
                
                ResultSet rs = checkStmt.executeQuery();
                
                if (!rs.next()) {
                    System.out.println("Product with ID " + productId + " not found.");
                    return;
                }
            }
            
            System.out.print("Enter new Product Name (or press Enter to keep current): ");
            String productName = scanner.nextLine();
            
            System.out.print("Enter new Price (or -1 to keep current): ");
            double price = scanner.nextDouble();
            
            System.out.print("Enter new Quantity (or -1 to keep current): ");
            int quantity = scanner.nextInt();
            
            // Build the SQL query based on what needs to be updated
            StringBuilder sqlBuilder = new StringBuilder("UPDATE Product SET ");
            boolean needsComma = false;
            
            if (!productName.isEmpty()) {
                sqlBuilder.append("ProductName = ?");
                needsComma = true;
            }
            
            if (price >= 0) {
                if (needsComma) {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append("Price = ?");
                needsComma = true;
            }
            
            if (quantity >= 0) {
                if (needsComma) {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append("Quantity = ?");
            }
            
            sqlBuilder.append(" WHERE ProductID = ?");
            
            String sql = sqlBuilder.toString();
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                int paramIndex = 1;
                
                if (!productName.isEmpty()) {
                    pstmt.setString(paramIndex++, productName);
                }
                
                if (price >= 0) {
                    pstmt.setDouble(paramIndex++, price);
                }
                
                if (quantity >= 0) {
                    pstmt.setInt(paramIndex++, quantity);
                }
                
                pstmt.setInt(paramIndex, productId);
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    connection.commit();
                    System.out.println("Product updated successfully!");
                } else {
                    connection.rollback();
                    System.out.println("Failed to update product.");
                }
            }
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println("Error updating product: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            scanner.nextLine(); // Clear input buffer
        }
    }
    
    // Delete product
    private static void deleteProduct() {
        try {
            System.out.println("\n----- Delete Product -----");
            
            System.out.print("Enter Product ID to delete: ");
            int productId = scanner.nextInt();
            
            String sql = "DELETE FROM Product WHERE ProductID = ?";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, productId);
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    connection.commit();
                    System.out.println("Product deleted successfully!");
                } else {
                    connection.rollback();
                    System.out.println("Product with ID " + productId + " not found.");
                }
            }
            
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println("Error deleting product: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            scanner.nextLine(); // Clear input buffer
        }
    }
    
    // Utility method to display product results
    private static void displayProductResults(ResultSet rs) throws SQLException {
        boolean found = false;
        
        System.out.println("-------------------------------------------------------------------------------");
        System.out.printf("%-10s %-25s %-15s %-15s\n", "ProductID", "ProductName", "Price", "Quantity");
        System.out.println("-------------------------------------------------------------------------------");
        
        while (rs.next()) {
            found = true;
            int productId = rs.getInt("ProductID");
            String productName = rs.getString("ProductName");
            double price = rs.getDouble("Price");
            int quantity = rs.getInt("Quantity");
            
            System.out.printf("%-10d %-25s $%-14.2f %-15d\n", productId, productName, price, quantity);
        }
        
        System.out.println("-------------------------------------------------------------------------------");
        
        if (!found) {
            System.out.println("No products found.");
        }
    }
}
