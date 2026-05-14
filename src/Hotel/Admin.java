package Hotel;
//----Inheritence----
// Same logic as Receptionist, just for managers.
public class Admin extends User {

    private String department;

    public Admin(int id, String name, String department) {
        super(id, name);
        this.department = department;
    }

    @Override
    public void displayInfo() {
        System.out.println("Administrator: " + name + " | Dept: " + department);
    }
}