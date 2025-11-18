package ooad1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseDelete {
	public static boolean deleteAccount(String accountNumber) {
        String sql = "DELETE FROM Account WHERE accNumber = ?";

        try (Connection conn = DatabaseRead.getConnection();
        	    PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            int rowsDeleted = ps.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("Account " + accountNumber + " deleted successfully.");
                return true;
            } else {
                System.out.println("No account found with account number: " + accountNumber);
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting account: " + e.getMessage());
            return false;
        }
	}
}