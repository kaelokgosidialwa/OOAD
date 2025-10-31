package ooad1;

public class ChequeAccount extends Account implements Withdrawal {

    private String companyAddress;
    private int branchId;        
    private String customerId;   

    // Constructor for loading from database
    public ChequeAccount(String accNumber, String accType, double balance,
                         int branchId, String customerId, String companyAddress) {
        super(accNumber, null, accType, balance);
        this.branchId = branchId;
        this.customerId = customerId;
        this.companyAddress = companyAddress;
    }

    // Constructor for creating a new Cheque account
    public ChequeAccount(String accNumber, Branch branch, String customerId, double initialDeposit, String companyAddress) {
        super(accNumber, branch, "Cheque", initialDeposit);
        this.branchId = branch.getBranchId();
        this.customerId = customerId;
        this.companyAddress = companyAddress;
    }

    // Getters and setters
    public String getCompanyAddress() { return companyAddress; }
    public void setCompanyAddress(String companyAddress) { this.companyAddress = companyAddress; }

    public int getBranchId() { return branchId; }
    public String getCustomerId() { return customerId; }

    @Override
    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
        }
    }

    @Override
    public String toString() {
        return "Account Type: Cheque" +
               "\nAccount Number: " + accNumber +
               "\nBalance: " + balance +
               "\nBranch ID: " + branchId +
               "\nCustomer ID: " + customerId +
               "\nCompany Address: " + (companyAddress != null ? companyAddress : "N/A");
    }
}
