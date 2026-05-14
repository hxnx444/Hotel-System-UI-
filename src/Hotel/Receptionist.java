package Hotel;
//----inheritence----
// Extends User, which means it gets the ID and Name variables for free.
public class Receptionist extends User {

    private String shift;

    public Receptionist(int id, String name, String shift) {
        super(id, name);    // The 'super' keyword passes these values up to the parent User class
        this.shift = shift;
    }

    // Because User is an abstract class, we HAVE to write this method here.
    @Override
    public void displayInfo() {
        System.out.println("Receptionist: " + name + " | Shift: " + shift);
    }

    // This is an example of Association for the rubric.
    // The Receptionist uses the Guest object, but doesn't "own" it.
    public void checkInGuest(Guest c) {
        System.out.println("Receptionist " + name + " is checking in " + c.getName());
    }
    /*Association: In Receptionist.java, the method public void checkInGuest(Guest c) shows Association.
     The Receptionist interacts with the Guest object to check them in, but neither "owns" the other.*/
}