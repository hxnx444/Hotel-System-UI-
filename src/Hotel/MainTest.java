package Hotel;

// Import Scanner to allow user input from the keyboard
import java.util.Scanner;

public class MainTest {

    // HELPER METHOD: A simple method to make the console output look consistent and neat.

    private static void printHeader(String title) {
        System.out.println("      " + title);
    }

    public static void main(String[] args) throws Exception {
        // Initialize Scanner for reading user input
        Scanner scanner = new Scanner(System.in);

        // Create the main system object (holds the list of rooms and reservations)
        HotelSystem system = new HotelSystem();

        printHeader("SYSTEM INITIALIZATION");

        // Create an Admin object to simulate a logged-in manager
        Admin admin = new Admin(202513, "Admin Hana", "Management");
        System.out.print("System initialized by: ");
        admin.displayInfo(); // Display admin details

        // Adding rooms to the system manually so the hotel isn't empty when we start
        system.addRoom(new Room(101, "Single", 50.0));
        system.addRoom(new Room(102, "Double", 100.0));
        system.addRoom(new Room(103, "Suite", 300.0));
        system.addRoom(new Room(104, "Single", 55.0));

        System.out.println("Rooms:");

        // Create a Receptionist object who will handle the check-in later
        Receptionist receptionist = new Receptionist(2, "Hana", "Morning");
        System.out.println("Receptionist on duty: " + receptionist.getName());

        //SORTING DEMONSTRATION
        printHeader(" ROOMS (Sorted by Price)");
        // Calls the sorting algorithm (Bubble Sort) to organize rooms from cheapest to expensive
        system.sortRoomsByPrice();


        //SEARCH 1: BY ID (Integer)
        printHeader("SEARCH ROOM BY ID");
        System.out.print(" Enter Room ID: ");

        // Input Validation: Check if the user actually typed a number
        if (scanner.hasNextInt()) {
            int id = scanner.nextInt();
            scanner.nextLine(); // "Eat" the leftover newline character so it doesn't skip the next input

            // Call the search method (Linear Search)
            Room found = system.searchRoom(id);

            System.out.println("Result: ");
            // Check if search returned a Room object or null
            if (found != null) found.displayRoom();
            else System.out.println("Room not found.");
        } else {
            // Handle cases where user types text instead of a number
            scanner.nextLine();
            System.out.println("Invalid input.");
        }


        //SEARCH 2: BY TYPE (String)
        printHeader(" SEARCH ROOM BY TYPE");
        System.out.print(" Enter Type (Single/Double/Suite): ");
        String type = scanner.nextLine(); // FIXED: Changed 'int' to 'String' // Read the string input // Read the string input

        // Call the void search method which prints results directly inside the method
        system.searchRoom(Integer.parseInt(type));


        // --- BOOKING PROCESS ---
        printHeader("BOOKING & PAYMENT");

        // 1. Gather Guest Information
        System.out.print("Enter Guest Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Phone Number: ");
        String phone = scanner.nextLine();

        // Create the Guest object
        Guest cust = new Guest(99, name, phone);

        // 2. Select a Room
        System.out.print("Enter Room Number to Book: ");
        if (scanner.hasNextInt()) {
            int rNum = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            // Find the specific room object in the list
            Room roomToBook = system.searchRoom(rNum);

            // 3. Validation: Does the room exist AND is it available?
            if (roomToBook != null && roomToBook.isAvailable()) {
                System.out.print("How many nights? ");

                int nights = scanner.nextInt();
                scanner.nextLine();

                // Create the Reservation object (links Guest + Room + Duration)
                Reservation res = new Reservation(cust, roomToBook, nights);
                res.showSummary(); // Print the bill details

                // Add the reservation to the system (marks room as booked)
                system.addReservation(res);

                // --- PAYMENT PROCESS ---
                System.out.println("\n--- Payment Section ---");
                System.out.println("Total Due: $" + res.getTotalPrice());
                System.out.print("Enter Cash Amount: ");

                double cash = scanner.nextDouble();
                scanner.nextLine(); // Consume newline

                // Create Payment object with total due and payment type "Cash"
                Payment payment = new Payment(res.getTotalPrice(), "Cash");

                // Check if the cash provided is enough to cover the total price
                if (payment.processPayment(cash)) {
                    System.out.println("Payment Accepted!");
                    // Calculate and show change
                    System.out.println("Change: $" + payment.calculateChange(cash));

                    // Receptionist formally checks the guest in
                    receptionist.checkInGuest(cust);
                } else {
                    System.out.println("Transaction Cancelled. Insufficient funds.");
                }

            } else {
                // If room was null or isAvailable() was false
                System.out.println("Room not found or already booked.");
            }
        } else {
            scanner.nextLine();
            System.out.println("Invalid number entered.");
        }

        printHeader("RESERVATION COMPLETED");
        System.out.println("System shutting down...");
        scanner.close();
    }
}