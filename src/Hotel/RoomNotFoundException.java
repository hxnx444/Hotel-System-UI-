package Hotel;

// Custom exception. This is better than just returning 'null' when a search fails
// because it lets us catch it specifically and show a nice error message to the user.
public class RoomNotFoundException extends RuntimeException {
    private int roomNumber;

    public RoomNotFoundException(int roomNumber) {
        super("Room " + roomNumber + " was not found in the system.");
        this.roomNumber = roomNumber;
    }

    public int getRoomNumber() {
        return roomNumber;
    }
}