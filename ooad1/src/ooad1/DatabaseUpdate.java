package ooad1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseUpdate {

	public static void saveCustomer(Customer sessionUser) {
	    if (sessionUser == null) return;

	    String updateCustomerSQL = "UPDATE Customer SET firstName = ?, lastName = ?, address = ?, password = ? WHERE customer_id = ?";
	    String updateAccountSQL = "UPDATE Account SET balance = ?, accType = ?, branch_id = ?, companyAddress = ?, interest = ? WHERE accNumber = ?";

	    Connection conn = null;
	    PreparedStatement psCustomer = null;
	    PreparedStatement psAccount = null;

	    try {
	        conn = DatabaseRead.getConnection();
	        conn.setAutoCommit(false); // start transaction

	        psCustomer = conn.prepareStatement(updateCustomerSQL);
	        psAccount = conn.prepareStatement(updateAccountSQL);

	        // Update customer info
	        psCustomer.setString(1, sessionUser.getFirstName());
	        psCustomer.setString(2, sessionUser.getLastName());
	        psCustomer.setString(3, sessionUser.getAddress());
	        psCustomer.setString(4, sessionUser.getPassword());
	        psCustomer.setString(5, sessionUser.getIdNumber());
	        psCustomer.executeUpdate();

	        // Update all accounts
	        for (Account acc : sessionUser.getAccounts()) {
	            psAccount.setDouble(1, acc.getBalance());
	            psAccount.setString(2, acc.getAccType());
	            psAccount.setInt(3, acc.getDbBranchId());
	            psAccount.setString(4, acc.getCompanyAddress());

	            double interestValue = 0.0;
	            if (acc instanceof Interest) {
	                interestValue = ((Interest) acc).getInterest();
	            }
	            psAccount.setDouble(5, interestValue);
	            psAccount.setString(6, acc.getAccNumber());
	            psAccount.executeUpdate();
	        }

	        conn.commit(); // commit all changes
	        System.out.println("Customer and accounts saved successfully for: " + sessionUser.getIdNumber());

	    } catch (SQLException e) {
	        e.printStackTrace();
	        try {
	            if (conn != null) {
	                System.out.println("Rolling back transaction due to error...");
	                conn.rollback(); // undo all changes in this transaction
	            }
	        } catch (SQLException rollbackEx) {
	            rollbackEx.printStackTrace();
	        }
	    } finally {
	        try {
	            if (conn != null) {
	                conn.setAutoCommit(true); // reset autocommit
	                conn.close(); // close connection
	            }
	            if (psCustomer != null) psCustomer.close();
	            if (psAccount != null) psAccount.close();
	        } catch (SQLException ex) {
	            ex.printStackTrace();
	        }
	      }
	    }
	    
	    public static void saveAccount(Account account) throws SQLException {
	        if (account == null) return;

	        String sql = "UPDATE Account SET balance = ?, accType = ?, branch_id = ?, companyAddress = ?, interest = ? " +
	                     "WHERE accNumber = ?";

	        try (Connection conn = DatabaseRead.getConnection();
	        	    PreparedStatement ps = conn.prepareStatement(sql)) {

	            ps.setDouble(1, account.getBalance());
	            ps.setString(2, account.getAccType());
	            ps.setInt(3, account.getDbBranchId());
	            ps.setString(4, account.getCompanyAddress() != null ? account.getCompanyAddress() : "");

	            // Only apply interest if account implements Interest
	            double interestValue = 0.0;
	            if (account instanceof Interest) {
	                interestValue = ((Interest) account).getInterest();
	            }
	            ps.setDouble(5, interestValue);

	            ps.setString(6, account.getAccNumber());

	            ps.executeUpdate();
	        }
    }
}
