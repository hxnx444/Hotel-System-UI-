package Hotel;
//----Inheritence----
public class Guest extends User {
    private String phone;
/*OVERLOADING
Guest.java has two constructors (one with an ID from the database,
one without an ID for walk-ins). This is constructor overloading.
 */
    // We use this when loading a known guest from the database
    public Guest(int id, String name, String phone) {
        super(id, name);
        this.phone = phone;
    }

    // We use this for walk-ins when we don't have a database ID for them yet
    public Guest(String name, String phone) {
        super(0, name); // Just pass 0 as a dummy ID
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public void displayInfo() {
        System.out.println("Guest: " + name + " | Phone: " + phone);
    }
}