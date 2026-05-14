package Hotel;
//----Interface----
// Implementing Comparable here so we can easily sort a list of rooms by price later
public class Room implements Comparable<Room> {
    private int roomNumber;
    private String type;
    private double price;
    private boolean available;

    public Room(int roomNumber, String type, double price) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.price = price;
        this.available = true; // New rooms are always empty by default
    }

    public int getRoomNumber() { return roomNumber; }
    public String getType() { return type; }
    public double getPrice() { return price; }

    // JavaFX tables are picky. They look for methods specifically named
    // "is[VariableName]" or "get[VariableName]" to fill in the columns.
    public boolean isAvailable() { return available; }
    public boolean getAvailable() { return available; }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void displayRoom() {
        String status = available ? "Available" : "Booked";
        System.out.println("Room " + roomNumber + " [" + type + "] - $" + price + " - " + status);
    }

    // This is the actual logic for the Comparable interface.
    // It tells Java how to compare two rooms (cheapest to most expensive).
    @Override
    /*
    (Override): In Room.java (overriding the compareTo method for sorting) and in Guest.java
    (overriding the abstract displayInfo method from the User class).
     */
    public int compareTo(Room other) {
        return Double.compare(this.price, other.price);
    }
}
/*Room.java implements an Interface: public class Room implements Comparable<Room>.
 It overrides the compareTo() method to sort rooms by price.*/