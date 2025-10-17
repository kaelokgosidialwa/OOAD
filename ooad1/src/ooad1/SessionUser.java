package ooad1;

public class SessionUser {
    private static Customer customer;

    // Get the current logged-in customer
    public static Customer getCustomer() {
        return customer;
    }

    // Set the logged-in customer (called after login)
    public static void setCustomer(Customer c) {
        customer = c;
    }

    // Optional: clear session on logout
    public static void clear() {
        customer = null;
    }
}
