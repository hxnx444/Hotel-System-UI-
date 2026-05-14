package Hotel;

public class Reservation {
    private Guest guest;
    private Room room;
    private int roomNumber;
    private int nights;
    private double totalPrice;

    // We use this constructor when the receptionist is making a brand-new booking in the app
    public Reservation(Guest guest, Room room, int nights) {
        this.guest = guest;
        this.room = room;
        /*Aggregation: A Reservation "has-a" Guest and "has-a" Room.
        If you look at the top of Reservation.java, you will see it stores these as variables: private Guest guest; and private Room room;.
        If a reservation is deleted, the Room and Guest still exist independently.*/
        this.roomNumber = room.getRoomNumber();
        this.nights = nights;
        this.totalPrice = calculateTotal();
    }

    // We use this constructor when we are just downloading old bookings from the MySQL database
    public Reservation(Guest guest, int roomNumber, int nights, double totalPrice) {
        this.guest = guest;
        this.room = null; // We don't need the full room object if we are just loading history
        this.roomNumber = roomNumber;
        this.nights = nights;
        this.totalPrice = totalPrice;
    }

    private double calculateTotal() {
        if (room == null) return totalPrice; // Safe fallback so the app doesn't crash if room is empty
        return room.getPrice() * nights;
    }

    public double getTotalPrice() { return totalPrice; }
    public int getNights() { return nights; }
    public int getRoomNumber() { return roomNumber; }
    public Room getRoom() { return room; }
    public Guest getGuest() { return guest; }

    public String getGuestName() {
        return (guest != null) ? guest.getName() : "Unknown";
    }

    public String getGuestPhone() {
        return (guest != null) ? guest.getPhone() : "";
    }

    public void showSummary() {
        System.out.println("\n--- RESERVATION SLIP ---");
        System.out.println("Guest: " + getGuestName());
        System.out.println("Room: " + roomNumber + (room != null ? " (" + room.getType() + ")" : ""));
        System.out.println("Nights: " + nights);
        System.out.println("Total Due: $" + totalPrice);
        System.out.println("------------------------");
    }
}