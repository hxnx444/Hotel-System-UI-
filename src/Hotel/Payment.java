package Hotel;

// Just a small helper class to do the checkout math.
public class Payment {

    private double totalAmount;
    private String method;

    public Payment(double totalAmount, String method) {
        this.totalAmount = totalAmount;
        this.method = method;
    }

    // Returns true if they gave us enough money, false if they are short.
    public boolean processPayment(double cashGiven) {
        if (cashGiven >= totalAmount) {
            System.out.println("Processing " + method + " payment...");
            return true;
        } else {
            System.out.println("Insufficient funds!");
            return false;
        }
    }

    public double calculateChange(double cashGiven) {
        return cashGiven - totalAmount;
    }
}