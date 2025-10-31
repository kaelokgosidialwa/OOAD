package ooad1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseCreate {
	
	public static boolean createAccount(
            String accNumber,
            String accType,
            double balance,
            int branchId,
            String customerId,
            String companyAddress,  // optional for cheque
            Double interest   // optional for savings/investment
    ) {
        // --- Validation rules ---
        if (accType == null || accNumber == null || customerId == null) {
            System.out.println("Error: Missing required fields.");
            return false;
        }

        if (accType.equalsIgnoreCase("investment") && balance < 500) {
            System.out.println("Error: Minimum balance for investment accounts is 500.");
            return false;
        }

        // --- SQL template ---
        String sql = "INSERT INTO account " +
                "(accNumber, accType, balance, branch_id, customer_id, company_address, interestRate) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseRead.getConnection();
        	    PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accNumber);
            ps.setString(2, accType);
            ps.setDouble(3, balance);
            ps.setInt(4, branchId);
            ps.setString(5, customerId);

            // optional fields
            if (accType.equalsIgnoreCase("cheque")) {
                ps.setString(6, companyAddress);
            } else {
                ps.setNull(6, java.sql.Types.VARCHAR);
            }

            if (accType.equalsIgnoreCase("savings") || accType.equalsIgnoreCase("investment")) {
                ps.setObject(7, interest);
            } else {
                ps.setNull(7, java.sql.Types.DOUBLE);
            }

            int rows = ps.executeUpdate();
            System.out.println("Account created successfully: " + accNumber);
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Failed to create account: " + e.getMessage());
            return false;
        }
	  }  
        public static boolean createCustomer(String customerId, String firstName, String lastName, String address, String password) {
            String sql = "INSERT INTO Customer (customer_id, first_name, last_name, address, password) VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseRead.getConnection();
            	    PreparedStatement ps = conn.prepareStatement(sql)) {

                // Set parameters
                ps.setString(1, customerId);
                ps.setString(2, firstName);
                ps.setString(3, lastName);
                ps.setString(4, address);
                ps.setString(5, password);

                int rows = ps.executeUpdate();

                if (rows > 0) {
                    System.out.println("✅ Customer " + customerId + " successfully created.");
                    return true;
                } else {
                    System.out.println("⚠️ Failed to create customer " + customerId + ".");
                    return false;
                }

            } catch (SQLException e) {
                System.err.println("❌ Database error while creating customer: " + e.getMessage());
                return false;
            }
        } 
            public static boolean createBranch(String name, String address, String password) {
                String sql = "INSERT INTO Branch (name, address, password) VALUES (?, ?, ?)";

                try (Connection conn = DatabaseRead.getConnection();
                	   PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setString(1, name);
                    ps.setString(2, address);
                    ps.setString(3, password);

                    int rows = ps.executeUpdate();

                    if (rows > 0) {
                        System.out.println("✅ Branch created successfully: " + name);
                        return true;
                    } else {
                        System.out.println("⚠️ No rows inserted for branch: " + name);
                        return false;
                    }

                } catch (SQLException e) {
                    System.err.println("❌ Error creating branch " + name + ": " + e.getMessage());
                    return false;
                }             
            		}
                
                public static String generateCustomerId() {
                    return "CUS" + String.format("%03d", (int) (Math.random() * 999 + 1));
                }

                public static int generateBranchId() {
                    return (int) (Math.random() * 9000 + 1000); // e.g. 1000–9999
                }
                
                public static String generateAccountNumber() {
                    int num = (int) (Math.random() * 900000 + 100000); // 6-digit ID
                    return "ACC" + num;
                }

}
