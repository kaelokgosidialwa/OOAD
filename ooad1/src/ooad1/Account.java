package ooad1;

public abstract class Account {
    protected String accNumber;
    protected double balance;
    protected Branch branch;
    protected String accType;
    protected int dbBranchId;
    protected String dbCustomerId;
    protected String companyAddress;

    //
    public Account(String accNumber, Branch branch, String accType, double initialDeposit) {
        this.accNumber = accNumber;
        this.branch = branch;
        this.accType = accType;
        this.balance = initialDeposit;
    }
    
    public Account(String accNumber, String accType, double balance, int branchId, String customerId, String companyAddress, double interest) {
        this.accNumber = accNumber;
        this.accType = accType;
        this.balance = balance;
        this.branch = null;
        this.dbBranchId = branchId;
        this.dbCustomerId = customerId;
        this.companyAddress = null;
    }

    //
 // Getters
    public String getAccNumber() { return accNumber; }
    public double getBalance() { return balance; }
    public Branch getBranch() { return branch; }
    public String getAccType() { return accType; }
    public int getDbBranchId() { return dbBranchId; }
    public String getDbCustomerId() { return dbCustomerId; }
    public String getCompanyAddress() { return companyAddress; }

    // Setters
    public void setBalance(double balance) { this.balance = balance; }
    public void setBranch(Branch branch) { this.branch = branch; }
    public void setAccType(String accType) { this.accType = accType; }
    public void setDbBranchId(int dbBranchId) { this.dbBranchId = dbBranchId; }
    public void setDbCustomerId(String dbCustomerId) { this.dbCustomerId = dbCustomerId; }
    public void setCompanyAddress(String companyAddress) { this.companyAddress = companyAddress; }

    //
    public String deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            return amount + " deposited. New balance: " + balance;
        } else {
            return "Deposit amount must be positive.";
        }
    }

    //
    @Override
    public String toString() {
        String branchName = (branch != null) ? branch.getName() : "";
        return "Account Number: " + accNumber +
               "\nAccount Type: " + accType +
               "\nBalance: " + balance +
               "\nBranch: " + branchName;
    }
}