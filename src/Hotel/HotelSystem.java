package Hotel;

import java.sql.SQLException;
import java.util.ArrayList;

// This class is the "brain" of our app. The UI talks to this, and this talks to the Database.
public class HotelSystem {

    // We keep a copy of the database data in memory (ArrayLists) so the app runs super fast
    // and doesn't have to query MySQL every time we click a button.
    /*
    In HotelSystem.java, you use private ArrayList<Room> rooms;
     and private ArrayList<Reservation> reservations; to store all the data in memory.
     */
    private ArrayList<Room> rooms;
    //composition
    private ArrayList<Reservation> reservations;

    public HotelSystem() {
        DatabaseHelper.createTables();
        // Load everything up right when the system starts
        rooms = DatabaseHelper.loadAllRooms();
        reservations = DatabaseHelper.loadAllReservations();
    }

    public void addRoom(Room room) throws Exception {
        if (room == null) throw new Exception("Room cannot be null.");
        try {
            DatabaseHelper.addRoomToDatabase(room); // Save to DB
            rooms.add(room); // Add to local memory
        } catch (SQLException e) {
            throw new Exception("Room ID already exists or DB error.");
        }
    }
    /*HotelSystem.java has two searchRoom methods: searchRoom(int roomNumber) and searchRoom(String type). This is method overloading.*/
    // Standard linear search to find a room by its number
    public Room searchRoom(int roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber) {
                return room;
            }
        }
        // Throw our custom exception if we finish the loop and find nothing
        throw new RoomNotFoundException(roomNumber);
    }

    // Overloaded search method (searches by type instead of ID). Good for the rubric!
    public ArrayList<Room> searchRoom(String type) {
        ArrayList<Room> result = new ArrayList<>();
        if (type == null || type.trim().isEmpty()) return result;

        for (Room room : rooms) {
            if (room.getType().equalsIgnoreCase(type.trim())) {
                result.add(room);
            }
        }
        return result;
    }

    // A classic Bubble Sort. We use this to sort rooms from cheapest to most expensive.
    public void sortRoomsByPrice() {
        for (int i = 0; i < rooms.size() - 1; i++) {
            for (int j = 0; j < rooms.size() - i - 1; j++) {
                // Uses the compareTo() method we wrote inside the Room.java file
                if (rooms.get(j).compareTo(rooms.get(j + 1)) > 0) {
                    Room temp = rooms.get(j);
                    rooms.set(j, rooms.get(j + 1));
                    rooms.set(j + 1, temp);
                }
            }
        }
    }

    public void addReservation(Reservation reservation) throws Exception {
        if (reservation == null) throw new Exception("Reservation cannot be null.");

        Room room = searchRoom(reservation.getRoomNumber());
        if (!room.isAvailable()) throw new Exception("Room is already booked.");

        try {
            DatabaseHelper.addReservationToDatabase(reservation);

            // Sync the status so the room shows as 'Booked' in the UI and DB
            room.setAvailable(false);
            DatabaseHelper.updateRoomAvailability(room.getRoomNumber(), false);

            reservations.add(reservation);
        } catch (SQLException e) {
            throw new Exception("Database error: " + e.getMessage());
        }
    }

    public void cancelReservation(int roomNumber) throws Exception {
        Room room = searchRoom(roomNumber);
        if (room.isAvailable()) throw new Exception("No reservation exists for this room.");

        // .removeIf is a quick way to delete from the ArrayList without writing a whole loop
        reservations.removeIf(r -> r.getRoomNumber() == roomNumber);
        DatabaseHelper.deleteReservation(roomNumber);

        room.setAvailable(true);
        DatabaseHelper.updateRoomAvailability(roomNumber, true);
    }

    public void checkIn(int roomNumber) throws Exception {
        Room room = searchRoom(roomNumber);
        if (!room.isAvailable()) throw new Exception("Room already occupied.");

        room.setAvailable(false);
        DatabaseHelper.updateRoomAvailability(roomNumber, false);
    }

    public void checkOut(int roomNumber) throws Exception {
        Room room = searchRoom(roomNumber);
        room.setAvailable(true);
        DatabaseHelper.updateRoomAvailability(roomNumber, true);
    }

    // We return a NEW array list here so the UI can't accidentally mess up our main data list
    public ArrayList<Room> getAllRooms() { return new ArrayList<>(rooms); }
    public ArrayList<Reservation> getAllReservations() { return new ArrayList<>(reservations); }
}