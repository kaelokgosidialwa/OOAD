package ooad1;

import java.sql.*;
import java.util.List;

public class DatabaseManipulation {

    // Save a single customer and all their accounts
    public static boolean saveCustomer(Customer customer) {
        boolean success = saveCustomerOnly(customer);

        if (success) {
            List<Account> accounts = customer.getAccounts();
            for (Account acc : accounts) {
                success &= saveAccount(acc);
            }
        }

        return success;
    }

    // Save only customer (without accounts)
    public static boolean saveCustomerOnly(Customer customer) {
        String sql = "INSERT OR REPLACE INTO Customer (id, firstname, lastname, address, password) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getIdNumber());
            stmt.setString(2, customer.getFirstName());
            stmt.setString(3, customer.getLastName());
            stmt.setString(4, customer.getAddress());
            stmt.setString(5, customer.getPassword());

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Save a single account
    public static boolean saveAccount(Account account) {
        String sql = "INSERT OR REPLACE INTO Account (accNumber, accType, balance, branch_id, customer_id, companyAddress, interest) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, account.getAccNumber());
            stmt.setString(2, account.getAccType());
            stmt.setDouble(3, account.getBalance());
            stmt.setInt(4, account.getDbBranchId());
            stmt.setString(5, account.getDbCustomerId());

            if (account instanceof ChequeAccount) {
                stmt.setString(6, ((ChequeAccount) account).getCompanyAddress());
                stmt.setDouble(7, 0);
            } else if (account instanceof SavingsAccount) {
                stmt.setString(6, ((SavingsAccount) account).getCompanyAddress());
                stmt.setDouble(7, ((SavingsAccount) account).getInterest());
            } else if (account instanceof InvestmentAccount) {
                stmt.setString(6, ((InvestmentAccount) account).getCompanyAddress());
                stmt.setDouble(7, ((InvestmentAccount) account).getInterest());
            } else {
                stmt.setString(6, null);
                stmt.setDouble(7, 0);
            }

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Save a branch
    public static boolean saveBranch(Branch branch) {
        String sql = "INSERT OR REPLACE INTO Branch (branch_id, name, address, password) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, branch.getBranchId());
            stmt.setString(2, branch.getName());
            stmt.setString(3, branch.getAddress());
            stmt.setString(4, branch.getPassword());

            stmt.executeUpdate();

            // Optionally save customers and accounts for this branch
            for (Customer c : branch.getCustomers()) {
                saveCustomer(c);
            }

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Save a list of branches
    public static boolean saveBranches(List<Branch> branches) {
        boolean success = true;
        for (Branch b : branches) {
            success &= saveBranch(b);
        }
        return success;
    }
}
