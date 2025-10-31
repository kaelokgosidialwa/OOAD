package ooad1;

import java.util.ArrayList;
import java.util.List;

public class Branch {
    private int branch_id;      // updated to match DB
    private String name;
    private String address;
    private String password;    // new field
    private List<Account> accounts;
    private List<Customer> customers;

    // Constructor without ID (for new branches)
    public Branch(String name, String address) {
        this.name = name;
        this.address = address;
        this.accounts = new ArrayList<>();
        this.customers = new ArrayList<>();
        this.branch_id = 0; // not yet assigned in DB
        this.password = generatePassword();
    }

    // Constructor with ID (for loading from DB)
    public Branch(int branch_id, String name, String address) {
        this.branch_id = branch_id;
        this.name = name;
        this.address = address;
        this.accounts = new ArrayList<>();
        this.customers = new ArrayList<>();
        this.password = generatePassword();
    }

    // Password generation logic
    private String generatePassword() {
        String namePart = name.length() >= 3 ? name.substring(0, 3) : name;
        String addrPart = address.length() >= 3 ? address.substring(0, 3) : address;
        return namePart + addrPart + (branch_id > 0 ? branch_id : "0");
    }

    // --- Getters and Setters ---
    public int getBranchId() { return branch_id; }
    public void setBranchId(int branch_id) { this.branch_id = branch_id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<Account> getAccounts() { return accounts; }
    public void setAccounts(List<Account> accounts) {
        this.accounts.clear();
        if (accounts != null) this.accounts.addAll(accounts);
    }

    public List<Customer> getCustomers() { return customers; }
    public void setCustomers(List<Customer> customers) {
        this.customers.clear();
        if (customers != null) this.customers.addAll(customers);
    }

 // --- Account Management ---
    public String openAccount(Customer customer, String accType, double initialDeposit, String companyAddress) {
        Account newAccount = createNewAccount(accType, this, customer, initialDeposit, companyAddress);
        if (newAccount == null) {
            return "Invalid account type or requirements not met.";
        }

        // Add to branch and customer
        accounts.add(newAccount);
        customer.addAccount(newAccount);
        if (!customers.contains(customer)) {
            customers.add(customer);
        }

        return accType + " account opened for " + customer.getFirstName();
    }

    // --- Internal factory method ---
    private Account createNewAccount(String accType, Branch branch, Customer customer, double initialDeposit, String companyAddress) {
        String customerId = customer.getIdNumber();
        String accNumber;
        switch (accType.toLowerCase()) {
            case "cheque":
                accNumber = "CHQ-" + (accounts.size() + 1);
                return new ChequeAccount(accNumber, branch, customerId, initialDeposit, companyAddress);

            case "savings":
                accNumber = "SAV-" + (accounts.size() + 1);
                return new SavingsAccount(accNumber, branch, customerId, initialDeposit);

            case "investment":
                if (initialDeposit < 500) return null; // enforce minimum
                accNumber = "INV-" + (accounts.size() + 1);
                return new InvestmentAccount(accNumber, branch, customerId, initialDeposit);

            default:
                return null; // invalid type
        }
    }



    public String closeAccount(Account account, Customer customer) {
        if (accounts.remove(account)) {
            customer.removeAccount(account);
            return "Account " + account.getAccNumber() + " closed.";
        }
        return "Account not found in this branch.";
    }

    public void addAccount(Account account) {
        if (account != null && !accounts.contains(account)) accounts.add(account);
    }

    public void addCustomer(Customer customer) {
        if (customer != null && !customers.contains(customer)) customers.add(customer);
    }

    @Override
    public String toString() {
        return name + " (" + address + ")";
    }

    public String displayBranchDetails() {
        return "Branch: " + name + " | Address: " + address +
               "\nTotal Customers: " + customers.size() +
               "\nTotal Accounts: " + accounts.size();
    }
}
