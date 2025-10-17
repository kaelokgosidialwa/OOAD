package ooad1;

public class SessionBranch {

    // The currently logged-in branch
    private static Branch branch;

    // Private constructor to prevent instantiation
    private SessionBranch() {}

    // Set the current branch
    public static void setBranch(Branch b) {
        branch = b;
    }

    // Get the current branch
    public static Branch getBranch() {
        return branch;
    }

    // Optional: clear session (e.g., on logout)
    public static void clear() {
        branch = null;
    }
}