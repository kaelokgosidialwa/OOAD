package ooad1;

public class InvestmentAccount extends Account implements Interest, Withdrawal {

    private final double interest = 0.05; // Fixed 5%
    private int branchId;            
    private String customerId;       
    private String companyAddress;

    //
    public InvestmentAccount(String accNumber, String accType, double balance, int branchId,
                             String customerId, String companyAddress, double interest) {
        super(accNumber, null, accType, balance);
        this.branchId = branchId;
        this.customerId = customerId;
        this.companyAddress = companyAddress;
    }

    //
    public InvestmentAccount(String accNumber, Branch branch, String customerId, double initialDeposit) {
        super(accNumber, branch, "Investment", validateDeposit(initialDeposit));
        this.branchId = branch.getBranchId();
        this.customerId = customerId;
        this.companyAddress = branch.getAddress();
    }

    private static double validateDeposit(double initialDeposit) {
        if (initialDeposit < 500) {
            throw new IllegalArgumentException("Investment account requires a minimum deposit of 500.");
        }
        return initialDeposit;
    }

    // Getters
    public double getInterestRate() { return interest; }
    public int getBranchId() { return branchId; }
    public String getCustomerId() { return customerId; }
    public String getCompanyAddress() { return companyAddress; }

    // Implement Interest interface
    @Override
    public void payInterest() {
        balance += balance * interest;
    }

    @Override
    public void setInterest(double rate) {}
    public double getInterest() {return interest;}

    @Override
    public String calculateInterest() {
        double interestAmount = balance * interest;
        balance += interestAmount;
        return "Interest of " + (interest * 100) + "% applied. New balance: " + balance;
    }

    // Implement Withdrawal interface
    @Override
    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
        }
    }

    @Override
    public String toString() {
        return "Account Type: Investment" +
                "\nAccount Number: " + accNumber +
                "\nBalance: " + balance +
                "\nBranch ID: " + branchId +
                "\nCustomer ID: " + customerId +
                "\nCompany Address: " + (companyAddress != null ? companyAddress : "N/A") +
                "\nInterest Rate: " + (interest * 100) + "%";
    }
}
