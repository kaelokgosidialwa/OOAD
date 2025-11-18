package ooad1;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseRead {

    private static final String DB_URL = "jdbc:sqlite:resources/bank.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // -------------------------------
    // Branch methods
    // -------------------------------

    public static List<Branch> queryBranch(String searchColumn, String searchValue) {
        List<Branch> branches = new ArrayList<>();
        if (!searchColumn.equals("branch_id") && !searchColumn.equals("name") && !searchColumn.equals("address")) {
            throw new IllegalArgumentException("Invalid search column: " + searchColumn);
        }

        String sql = "SELECT * FROM Branch WHERE " + searchColumn + " LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + searchValue + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Branch branch = new Branch(rs.getInt("branch_id"), rs.getString("name"), rs.getString("address"));
                branch.setPassword(rs.getString("password"));
                branches.add(branch);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return branches;
    }

    public static Branch checkCredentialsBranch(String branchName, String password) {
        String sql = "SELECT * FROM Branch WHERE name = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, branchName);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Branch branch = new Branch(rs.getInt("branch_id"), rs.getString("name"), rs.getString("address"));
                branch.setPassword(password);

                // Load all customers and accounts
                loadBranch(branch);
                return branch;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // -------------------------------
    // Customer methods
    // -------------------------------

    public static List<Customer> queryCustomer(String column, String value) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM Customer WHERE " + column + " LIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + value + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Customer c = new Customer(
                        rs.getString("customer_id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("address"),
                        rs.getString("password")
                );
                queryAccountByCustomer(c.getIdNumber(), c);
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
                Customer c = new Customer(
                        rs.getString("customer_id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("address"),
                        rs.getString("password")
                );

                // Load accounts for this customer
                queryAccountByCustomer(c.getIdNumber(), c);

                return c;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // -------------------------------
    // Account methods
    // -------------------------------

    public static List<Account> queryAccountByCustomer(String customerId, Customer customer) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM Account WHERE customer_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Account acc = createAccountFromResultSet(rs);
                if (acc != null) {
                    accounts.add(acc);
                    if (customer != null) customer.addAccount(acc);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    public static List<Account> queryAccountByBranch(int branchId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM Account WHERE branch_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, branchId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Account acc = createAccountFromResultSet(rs);
                if (acc != null) accounts.add(acc);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    private static Account createAccountFromResultSet(ResultSet rs) throws SQLException {
        String accNumber = rs.getString("accNumber");
        String accType = rs.getString("accType");
        double balance = rs.getDouble("balance");
        int branchId = rs.getInt("branch_id");
        String customerId = rs.getString("customer_id");
        String companyAddress = rs.getString("companyAddress");
        double interest = rs.getDouble("interest");

        switch (accType.toLowerCase()) {
            case "savings":
                return new SavingsAccount(accNumber, accType, balance, branchId, customerId, companyAddress, interest);
            case "investment":
                return new InvestmentAccount(accNumber, accType, balance, branchId, customerId, companyAddress, interest);
            case "cheque":
                return new ChequeAccount(accNumber, accType, balance, branchId, customerId, companyAddress);
            default:
                return null;
        }
    }

    // -------------------------------
    // Helper: Branch & Customer relations
    // -------------------------------

    public static int getBranchIdByCustomer(String customerId) {
        String sql = "SELECT branch_id FROM BranchCustomer WHERE customer_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("branch_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void loadBranch(Branch branch) {
        if (branch == null) return;

        try {
            // Load customers
            String sql = "SELECT customer_id FROM BranchCustomer WHERE branch_id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, branch.getBranchId());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String customerId = rs.getString("customer_id");
                    List<Customer> custList = queryCustomer("customer_id", customerId);
                    if (!custList.isEmpty()) {
                        Customer c = custList.get(0);
                        if (!branch.getCustomers().contains(c)) branch.addCustomer(c);
                    }
                }
            }

            // Load branch accounts
            List<Account> branchAccounts = queryAccountByBranch(branch.getBranchId());
            for (Account acc : branchAccounts) {
                branch.addAccount(acc);
                Customer owner = branch.getCustomers().stream()
                        .filter(c -> c.getIdNumber().equals(acc.getDbCustomerId()))
                        .findFirst()
                        .orElse(null);
                if (owner != null && !owner.getAccounts().contains(acc)) owner.addAccount(acc);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
