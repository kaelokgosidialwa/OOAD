package ooad1;

public class SavingsAccount extends Account implements Interest {

    private double interest = 0.03;  // 3%
    private int branchId;           
    private String customerId;      
    private String companyAddress;

    public SavingsAccount(String accNumber, String accType, double balance, int branchId,
                          String customerId, String companyAddress, double interest) {
        super(accNumber, accType, balance, branchId, customerId, companyAddress, interest);
        this.branchId = branchId;
        this.customerId = customerId;
        this.companyAddress = companyAddress;
        this.interest = interest;
    }

    public SavingsAccount(String accNumber, Branch branch, String customerId, double initialDeposit) {
        super(accNumber, branch, "Savings", initialDeposit);
        this.branchId = branch.getBranchId();
        this.customerId = customerId;
        this.companyAddress = branch.getAddress();
    }

    @Override
    public void setInterest(double rate) {this.interest = rate;}
    public double getInterest() {return interest;}

    @Override
    public void payInterest() {
        double interestAmount = balance * interest;
        balance += interestAmount;
    }

    @Override
    public String calculateInterest() {
        double interestAmount = balance * interest;
        return "Interest of " + (interest * 100) + "% = " + interestAmount;
    }

    @Override
    public String toString() {
        return "Account Type: Savings" +
               "\nAccount Number: " + accNumber +
               "\nBalance: " + balance +
               "\nBranch ID: " + branchId +
               "\nCustomer ID: " + customerId +
               "\nCompany Address: " + (companyAddress != null ? companyAddress : "N/A") +
               "\nInterest Rate: " + (interest * 100) + "%";
    }
}
