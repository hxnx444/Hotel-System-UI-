package Hotel;
//----abstract class----
// This is our base template. We never actually make a generic "User".
// We only make Admins, Guests, or Receptionists, which all share these basic variables.
public abstract class User {
    protected int id;
    protected String name;

    public User() {}

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Making this 'abstract' forces all child classes (like Admin or Guest)
    // to write their own version of this method.
    public abstract void displayInfo();
}
/*User.java is an Abstract Class (public abstract class User).
It has an abstract method public abstract void displayInfo();
 that forces Guest.java, Admin.java, and Receptionist.java to provide their own implementation.*/