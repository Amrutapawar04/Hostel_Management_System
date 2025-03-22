package JDBC_Example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

class RoomAlreadyBookedException extends Exception {
    public RoomAlreadyBookedException(String message) {
        super(message);
    }
}

class InvalidRoomException extends Exception {
    public InvalidRoomException(String message) {
        super(message);
    }
}

class PaymentFailedException extends Exception {
    public PaymentFailedException(String message) {
        super(message);
    }
}


public class HostelManagementSystem {
	 private static final String DB_URL = "jdbc:postgresql://localhost:5432/pdea";
	    private static final String DB_USER = "postgres";
	    private static final String DB_PASSWORD = "123";

	    public static void main(String[] args) {
	        Scanner scanner = new Scanner(System.in);
	        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
	            System.out.println("Connected to PostgreSQL Database!");
	            while (true) {
	                System.out.println("\nHostel Room Allocation System");
	                System.out.println("1. Register Student");
	                System.out.println("2. Allocate Room");
	                System.out.println("3. View Available Rooms");
	                System.out.println("4. Track Payments");
	                System.out.println("5. Exit");
	                System.out.print("Choose an option: ");
	                int choice = scanner.nextInt();
	                scanner.nextLine(); // Consume newline

	                switch (choice) {
	                    case 1:
	                        registerStudent(scanner, connection);
	                        break;
	                    case 2:
	                        allocateRoom(scanner, connection);
	                        break;
	                    case 3:
	                        viewAvailableRooms(connection);
	                        break;
	                    case 4:
	                        trackPayments(scanner, connection);
	                        break;
	                   
	                    case 5:
	                        System.out.println("Exiting... Goodbye!");
	                        return;
	                    default:
	                        System.out.println("Invalid choice! Try again.");
	                }
	            }
	        } catch (SQLException e) {
	            System.err.println("Database Connection Error: " + e.getMessage());
	        }
	    }

	    

		

		private static void registerStudent(Scanner scanner, Connection connection) {
			try {
	            System.out.print("Enter Student Name: ");
	            String name = scanner.nextLine();
	            System.out.print("Enter Age: ");
	            int age = scanner.nextInt();
	            scanner.nextLine(); // Consume newline
	            System.out.print("Enter Gender (Male/Female/Other): ");
	            String gender = scanner.nextLine();
	            System.out.print("Enter Contact Number: ");
	            String contact = scanner.nextLine();

	            String sql = "INSERT INTO students (name, age, gender, contact) VALUES (?, ?, ?, ?)";
	            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	                pstmt.setString(1, name);
	                pstmt.setInt(2, age);
	                pstmt.setString(3, gender);
	                pstmt.setString(4, contact);
	                int rowsInserted = pstmt.executeUpdate();
	                if (rowsInserted > 0) {
	                    System.out.println("Student Registered Successfully!");
	                }
	            }
	        } catch (SQLException e) {
	            System.err.println("Database Error: " + e.getMessage());
	        }
			
		}

		private static void allocateRoom(Scanner scanner, Connection connection) {
	        try {
	            System.out.print("Enter Student ID: ");
	            int studentId = scanner.nextInt();
	            System.out.print("Enter Room ID: ");
	            int roomId = scanner.nextInt();

	            String checkQuery = "SELECT status FROM rooms WHERE room_id = ?";
	            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
	                checkStmt.setInt(1, roomId);
	                ResultSet rs = checkStmt.executeQuery();
	                if (!rs.next()) {
	                    throw new InvalidRoomException("Room ID does not exist!");
	                }
	                if (rs.getString("status").equals("Occupied")) {
	                    throw new RoomAlreadyBookedException("Room is already occupied!");
	                }
	            }

	            String sql = "UPDATE rooms SET status = 'Occupied', allocated_to = ? WHERE room_id = ?";
	            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	                pstmt.setInt(1, studentId);
	                pstmt.setInt(2, roomId);
	                int updatedRows = pstmt.executeUpdate();
	                if (updatedRows > 0) {
	                    System.out.println("Room allocated successfully!");
	                } else {
	                    System.out.println("Room allocation failed.");
	                }
	            }
	        } catch (SQLException e) {
	            System.err.println("Database Error: " + e.getMessage());
	        } catch (RoomAlreadyBookedException | InvalidRoomException e) {
	            System.err.println("Error: " + e.getMessage());
	        }
	    }
		
		private static void viewAvailableRooms(Connection connection) {
			String sql = "SELECT room_id, capacity FROM rooms WHERE status = 'Available'";
	        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	            ResultSet rs = pstmt.executeQuery();
	            System.out.println("Available Rooms:");
	            boolean found = false;
	            while (rs.next()) {
	                int roomId = rs.getInt("room_id");
	                int capacity = rs.getInt("capacity");
	                System.out.println("Room ID: " + roomId + ", Capacity: " + capacity);
	                found = true;
	            }
	            if (!found) {
	                System.out.println("No rooms available.");
	            }
	        } catch (SQLException e) {
	            System.err.println("Database Error: " + e.getMessage());
	        }
			
			
		}

	    private static void trackPayments(Scanner scanner, Connection connection) {
	        try {
	            System.out.print("Enter Student ID: ");
	            int studentId = scanner.nextInt();
	            System.out.print("Enter Payment Amount: ");
	            double amount = scanner.nextDouble();

	            if (amount <= 0) {
	                throw new PaymentFailedException("Payment amount must be positive.");
	            }

	            String sql = "INSERT INTO payments (student_id, amount, status) VALUES (?, ?, 'Paid')";
	            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
	                pstmt.setInt(1, studentId);
	                pstmt.setDouble(2, amount);
	                int rowsInserted = pstmt.executeUpdate();
	                if (rowsInserted > 0) {
	                    System.out.println("Payment recorded successfully!");
	                }
	            }
	        } catch (SQLException e) {
	            System.err.println("Database Error: " + e.getMessage());
	        } catch (PaymentFailedException e) {
	            System.err.println("Error: " + e.getMessage());
	        }
	    }


}
