package Hotel;

import java.sql.*;
import java.util.ArrayList;

// We put all our MySQL code in this one file so it doesn't clutter up the UI code.
public class DatabaseHelper {
    // Make sure your XAMPP/WAMP is running and you have a database named 'hoteldb'
    private static final String URL = "jdbc:mysql://localhost:3306/hoteldb";
    private static final String USER = "root";
    private static final String PASS = "";

    // This block runs automatically once when the app starts to load the MySQL driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL Driver missing: " + e.getMessage());
        }
    }

    // Grabs a fresh connection to the DB
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // Builds the tables for us if it's the first time running the app on a new computer
    public static void createTables() {
        String createRoomTable =
                "CREATE TABLE IF NOT EXISTS Rooms (" +
                        "roomNumber INT PRIMARY KEY, type VARCHAR(50) NOT NULL, " +
                        "price DOUBLE NOT NULL, isAvailable BOOLEAN NOT NULL)";

        String createReservationTable =
                "CREATE TABLE IF NOT EXISTS Reservations (" +
                        "id INT PRIMARY KEY AUTO_INCREMENT, guestName VARCHAR(100) NOT NULL, " +
                        "guestPhone VARCHAR(20), roomNumber INT NOT NULL, " +
                        "nights INT NOT NULL, totalPrice DOUBLE NOT NULL, " +
                        "FOREIGN KEY(roomNumber) REFERENCES Rooms(roomNumber))";

        // The try-with-resources block (the parentheses after 'try') is cool because
        // it automatically closes the database connection when it's done. No memory leaks!
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(createRoomTable);
            stmt.execute(createReservationTable);
        } catch (SQLException e) {
            System.out.println("Table creation failed: " + e.getMessage());
        }
    }

    public static void addRoomToDatabase(Room r) throws SQLException {
        // We use PreparedStatement and '?' instead of pasting variables directly into the string.
        // It prevents SQL injection hacking and handles formatting automatically.
        String sql = "INSERT INTO Rooms(roomNumber, type, price, isAvailable) VALUES(?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, r.getRoomNumber());
            pstmt.setString(2, r.getType());
            pstmt.setDouble(3, r.getPrice());
            pstmt.setBoolean(4, r.isAvailable());
            pstmt.executeUpdate();
        }
    }

    public static void updateRoomAvailability(int roomNumber, boolean isAvailable) {
        String sql = "UPDATE Rooms SET isAvailable = ? WHERE roomNumber = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, isAvailable);
            pstmt.setInt(2, roomNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to update availability: " + e.getMessage());
        }
    }

    public static ArrayList<Room> loadAllRooms() {
        ArrayList<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM Rooms";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            // Loop through every row in the database and convert it back into a Java object
            while (rs.next()) {
                Room room = new Room(rs.getInt("roomNumber"), rs.getString("type"), rs.getDouble("price"));
                room.setAvailable(rs.getBoolean("isAvailable"));
                rooms.add(room);
            }
        } catch (SQLException e) {
            System.out.println("Error loading rooms: " + e.getMessage());
        }
        return rooms;
    }

    public static void addReservationToDatabase(Reservation res) throws SQLException {
        String sql = "INSERT INTO Reservations(guestName, guestPhone, roomNumber, nights, totalPrice) VALUES(?,?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, res.getGuestName());
            pstmt.setString(2, res.getGuestPhone());
            pstmt.setInt(3, res.getRoomNumber());
            pstmt.setInt(4, res.getNights());
            pstmt.setDouble(5, res.getTotalPrice());
            pstmt.executeUpdate();

            // Lock the room so nobody else can book it
            updateRoomAvailability(res.getRoomNumber(), false);
        }
    }

    public static ArrayList<Reservation> loadAllReservations() {
        ArrayList<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM Reservations";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Guest guest = new Guest(rs.getString("guestName"), rs.getString("guestPhone"));
                Reservation res = new Reservation(guest, rs.getInt("roomNumber"), rs.getInt("nights"), rs.getDouble("totalPrice"));
                reservations.add(res);
            }
        } catch (SQLException e) {
            System.out.println("Error loading reservations: " + e.getMessage());
        }
        return reservations;
    }

    public static void deleteReservation(int roomNumber) {
        String sql = "DELETE FROM Reservations WHERE roomNumber = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomNumber);
            if (pstmt.executeUpdate() > 0) {
                // Free the room back up once the reservation is deleted
                updateRoomAvailability(roomNumber, true);
            }
        } catch (SQLException e) {
            System.out.println("Error deleting reservation: " + e.getMessage());
        }
    }
}