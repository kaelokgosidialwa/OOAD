package ooad1;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String DB_URL = "jdbc:sqlite:resources/bank.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Load branches by a specific column and value.
     * @param searchColumn The column to filter by: "branch_id", "name", or "address".
     * @param searchValue The value to match.
     * @return List of matching Branch objects.
     */
    public static List<Branch> queryBranch(String searchColumn, String searchValue) {
        List<Branch> branches = new ArrayList<>();

        // Validate the column name to prevent SQL injection
        if (!searchColumn.equals("branch_id") && 
            !searchColumn.equals("name") &&
            !searchColumn.equals("address")) {
            throw new IllegalArgumentException("Invalid search column: " + searchColumn);
        }

        String sql = "SELECT * FROM Branch WHERE " + searchColumn + " LIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + searchValue + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Branch branch = new Branch(
                        rs.getInt("branch_id"),
                        rs.getString("name"),
                        rs.getString("address")
                );
                branch.setPassword(rs.getString("password")); // if branch has a password
                branches.add(branch);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return branches;
    }
    
    /**
     * Check credentials for branch login
     * @param branchName The branch name entered
     * @param password The branch password entered
     * @return Fully loaded Branch object if credentials are correct, null otherwise
     */
    public static Branch checkCredentialsBranch(String branchName, String password) {
        String sql = "SELECT * FROM Branch WHERE name = ? AND password = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, branchName);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int branchId = rs.getInt("branch_id");
                String name = rs.getString("name");
                String address = rs.getString("address");

                // Create branch object
                Branch branch = new Branch(branchId, name, address);
                branch.setPassword(password); // already validated

                // Load customers for this branch
                Database.loadBranch(branch);

                // Load accounts for all branch customers
                for (Customer c : branch.getCustomers()) {
                    Database.queryAccount("customer_id", c.getIdNumber(), c);
                }

                return branch;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // credentials not found
    }
    
    public static List<Customer> queryCustomer(String column, String value) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM Customer WHERE " + column + " LIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + value + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Customer c = new Customer(
                        rs.getString("id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("address"),
                        rs.getString("password")
                );

                customers.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }
    
    public static Customer checkCredentialsCustomer(String firstName, String lastName, String password) {
        String sql = "SELECT * FROM Customer WHERE firstname = ? AND lastname = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Customer(
                    rs.getString("id"),
                    rs.getString("firstname"),
                    rs.getString("lastname"),
                    rs.getString("address"),
                    rs.getString("password")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // login failed
    }
    
    public static List<Account> queryAccount(String column, String value, Customer customer) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM Account WHERE " + column + " LIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + value + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String accNumber = rs.getString("accNumber");
                String accType = rs.getString("accType");
                double balance = rs.getDouble("balance");
                int branchId = rs.getInt("branch_id");
                String customerId = rs.getString("customer_id");
                String companyAddress = rs.getString("companyAddress");
                double interest = rs.getDouble("interest");

                Account account = null;
                switch (accType.toLowerCase()) {
                    case "savings":
                        account = new SavingsAccount(accNumber, accType, balance,
                                branchId, customerId, companyAddress, interest);
                        break;
                    case "investment":
                        account = new InvestmentAccount(accNumber, accType, balance,
                                branchId, customerId, companyAddress, interest);
                        break;
                    case "cheque":
                        account = new ChequeAccount(accNumber, accType, balance,
                                branchId, customerId, companyAddress);
                        break;
                }

                if (account != null) {
                    accounts.add(account);
                    if (customer != null) {
                        customer.addAccount(account);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return accounts;
    }
    
    public static void loadBranch(Branch branch) {
        if (branch == null) return;

        // 1️⃣ Load all customers linked to this branch via the BranchCustomer table
        String sql = "SELECT customer_id FROM BranchCustomer WHERE branch_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, branch.getBranchId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String customerId = rs.getString("customer_id");

                // 2️⃣ Use queryCustomer to load the full customer object
                List<Customer> customerList = queryCustomer("customer_id", customerId);
                if (customerList.isEmpty()) continue;

                Customer customer = customerList.get(0);

                // 3️⃣ Load accounts for this customer, but only keep accounts belonging to this branch
                List<Account> accounts = queryAccount("customer_id", customerId, customer);
                accounts.removeIf(acc -> acc.getDbBranchId() != branch.getBranchId());

                // 4️⃣ Add these accounts to the branch's master account list
                for (Account acc : accounts) {
                    if (!branch.getAccounts().contains(acc)) {
                        branch.addAccount(acc); // branch and customer now reference the same Account objects
                    }
                }

                // 5️⃣ Add the customer to the branch
                if (!branch.getCustomers().contains(customer)) {
                    branch.addCustomer(customer);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
