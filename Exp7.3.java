// Model: Student.java
public class Student {
    private int studentId;
    private String name;
    private String department;
    private double marks;
    
    // Default constructor
    public Student() {
    }
    
    // Parameterized constructor
    public Student(int studentId, String name, String department, double marks) {
        this.studentId = studentId;
        this.name = name;
        this.department = department;
        this.marks = marks;
    }
    
    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }
    
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public double getMarks() {
        return marks;
    }
    
    public void setMarks(double marks) {
        this.marks = marks;
    }
    
    @Override
    public String toString() {
        return "Student [ID=" + studentId + ", Name=" + name + ", Department=" + department + ", Marks=" + marks + "]";
    }
}

// Database Connection: DBConnection.java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/school";
    private static final String USER = "root";
    private static final String PASSWORD = "password";
    
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        // Load the JDBC driver
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        // Establish and return the connection
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

// Controller: StudentController.java
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentController {
    private Connection connection;
    
    public StudentController() throws SQLException, ClassNotFoundException {
        this.connection = DBConnection.getConnection();
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        // Create the Student table if it doesn't exist
        try (Statement stmt = connection.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS Student (" +
                                   "StudentID INT PRIMARY KEY, " +
                                   "Name VARCHAR(100) NOT NULL, " +
                                   "Department VARCHAR(50) NOT NULL, " +
                                   "Marks DOUBLE NOT NULL)";
            stmt.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            System.err.println("Error creating Student table: " + e.getMessage());
        }
    }
    
    // Create a student record
    public boolean addStudent(Student student) {
        String sql = "INSERT INTO Student (StudentID, Name, Department, Marks) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, student.getStudentId());
            pstmt.setString(2, student.getName());
            pstmt.setString(3, student.getDepartment());
            pstmt.setDouble(4, student.getMarks());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding student: " + e.getMessage());
            return false;
        }
    }
    
    // Read all student records
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM Student";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Student student = new Student();
                student.setStudentId(rs.getInt("StudentID"));
                student.setName(rs.getString("Name"));
                student.setDepartment(rs.getString("Department"));
                student.setMarks(rs.getDouble("Marks"));
                
                students.add(student);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving students: " + e.getMessage());
        }
        
        return students;
    }
    
    // Read a specific student record
    public Student getStudentById(int studentId) {
        String sql = "SELECT * FROM Student WHERE StudentID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Student student = new Student();
                    student.setStudentId(rs.getInt("StudentID"));
                    student.setName(rs.getString("Name"));
                    student.setDepartment(rs.getString("Department"));
                    student.setMarks(rs.getDouble("Marks"));
                    
                    return student;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving student: " + e.getMessage());
        }
        
        return null;
    }
    
    // Update a student record
    public boolean updateStudent(Student student) {
        String sql = "UPDATE Student SET Name = ?, Department = ?, Marks = ? WHERE StudentID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getDepartment());
            pstmt.setDouble(3, student.getMarks());
            pstmt.setInt(4, student.getStudentId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
            return false;
        }
    }
    
    // Delete a student record
    public boolean deleteStudent(int studentId) {
        String sql = "DELETE FROM Student WHERE StudentID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
            return false;
        }
    }
    
    // Close the database connection
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}

// View: StudentView.java
import java.util.List;
import java.util.Scanner;

public class StudentView {
    private Scanner scanner;
    private StudentController controller;
    
    public StudentView() {
        this.scanner = new Scanner(System.in);
        try {
            this.controller = new StudentController();
        } catch (Exception e) {
            System.err.println("Error initializing application: " + e.getMessage());
            System.exit(1);
        }
    }
    
    public void start() {
        boolean exit = false;
        
        while (!exit) {
            displayMenu();
            int choice = getIntInput("Enter your choice: ");
            
            switch (choice) {
                case 1: addStudent(); break;
                case 2: viewAllStudents(); break;
                case 3: viewStudentById(); break;
                case 4: updateStudent(); break;
                case 5: deleteStudent(); break;
                case 6:
                    exit = true;
                    System.out.println("Exiting the application...");
                    controller.closeConnection();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        
        scanner.close();
    }
    
    private void displayMenu() {
        System.out.println("\n===== Student Management System =====");
        System.out.println("1. Add New Student");
        System.out.println("2. View All Students");
        System.out.println("3. View Student by ID");
        System.out.println("4. Update Student");
        System.out.println("5. Delete Student");
        System.out.println("6. Exit");
    }
    
    private void addStudent() {
        System.out.println("\n----- Add New Student -----");
        
        int studentId = getIntInput("Enter Student ID: ");
        System.out.print("Enter Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Department: ");
        String department = scanner.nextLine();
        double marks = getDoubleInput("Enter Marks: ");
        
        Student student = new Student(studentId, name, department, marks);
        
        if (controller.addStudent(student)) {
            System.out.println("Student added successfully!");
        } else {
            System.out.println("Failed to add student. Please try again.");
        }
    }
    
    private void viewAllStudents() {
        System.out.println("\n----- All Students -----");
        
        List<Student> students = controller.getAllStudents();
        
        if (students.isEmpty()) {
            System.out.println("No students found in the database.");
        } else {
            displayStudentHeader();
            for (Student student : students) {
                displayStudentRow(student);
            }
            displayStudentFooter();
        }
    }
    
    private void viewStudentById() {
        System.out.println("\n----- View Student by ID -----");
        
        int studentId = getIntInput("Enter Student ID: ");
        Student student = controller.getStudentById(studentId);
        
        if (student != null) {
            displayStudentHeader();
            displayStudentRow(student);
            displayStudentFooter();
        } else {
            System.out.println("Student with ID " + studentId + " not found.");
        }
    }
    
    private void updateStudent() {
        System.out.println("\n----- Update Student -----");
        
        int studentId = getIntInput("Enter Student ID: ");
        Student student = controller.getStudentById(studentId);
        
        if (student != null) {
            System.out.println("Current Student Details:");
            displayStudentHeader();
            displayStudentRow(student);
            displayStudentFooter();
            
            System.out.println("\nEnter new details (leave blank to keep current):");
            
            System.out.print("Enter new Name [" + student.getName() + "]: ");
            String name = scanner.nextLine();
            if (!name.isEmpty()) {
                student.setName(name);
            }
            
            System.out.print("Enter new Department [" + student.getDepartment() + "]: ");
            String department = scanner.nextLine();
            if (!department.isEmpty()) {
                student.setDepartment(department);
            }
            
            System.out.print("Enter new Marks [" + student.getMarks() + "]: ");
            String marksStr = scanner.nextLine();
            if (!marksStr.isEmpty()) {
                try {
                    double marks = Double.parseDouble(marksStr);
                    student.setMarks(marks);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid marks format. Keeping current marks.");
                }
            }
            
            if (controller.updateStudent(student)) {
                System.out.println("Student updated successfully!");
            } else {
                System.out.println("Failed to update student. Please try again.");
            }
        } else {
            System.out.println("Student with ID " + studentId + " not found.");
        }
    }
    
    private void deleteStudent() {
        System.out.println("\n----- Delete Student -----");
        
        int studentId = getIntInput("Enter Student ID: ");
        Student student = controller.getStudentById(studentId);
        
        if (student != null) {
            System.out.println("Student to be deleted:");
            displayStudentHeader();
            displayStudentRow(student);
            displayStudentFooter();
            
            System.out.print("Are you sure you want to delete this student? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            
            if (confirm.equals("y") || confirm.equals("yes")) {
                if (controller.deleteStudent(studentId)) {
                    System.out.println("Student deleted successfully!");
                } else {
                    System.out.println("Failed to delete student. Please try again.");
                }
            } else {
                System.out.println("Delete operation cancelled.");
            }
        } else {
            System.out.println("Student with ID " + studentId + " not found.");
        }
    }
    
    private void displayStudentHeader() {
        System.out.println("---------------------------------------------------------");
        System.out.printf("%-10s %-20s %-15s %-10s\n", "ID", "Name", "Department", "Marks");
        System.out.println("---------------------------------------------------------");
    }
    
    private void displayStudentRow(Student student) {
        System.out.printf("%-10d %-20s %-15s %-10.2f\n", 
                student.getStudentId(), 
                student.getName(), 
                student.getDepartment(), 
                student.getMarks());
    }
    
    private void displayStudentFooter() {
        System.out.println("---------------------------------------------------------");
    }
    
    private int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(scanner.nextLine());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }
    
    private double getDoubleInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }
}

// Main Application: StudentManagementApp.java
public class StudentManagementApp {
    public static void main(String[] args) {
        StudentView view = new StudentView();
        view.start();
    }
}
