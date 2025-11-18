package ooad1;

import java.util.ArrayList;
import java.util.List;
//todo make individual and company class
public class Customer {
    private String firstName;
    private String lastName;
    private String address;
    private String password;
    private String customer_id;
    private List<Account> accounts;

    public Customer(String customer_id, String firstName, String lastName, String address, String password) {
        this.customer_id = customer_id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.password = password;
        this.accounts = new ArrayList<>();
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getIdNumber() { return customer_id; }
    public void setIdNumber(String idNumber) { this.customer_id = idNumber; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public void removeAccount(Account account) {
        accounts.remove(account);
    }

    public List<Account> getAccounts() {
        return accounts;
    }
    
    public void setAccounts(List<Account> accounts) {
        this.accounts.clear();
        if (accounts != null) {
            this.accounts.addAll(accounts);
        }
    }

    public String getAccountsDetails() {
        if (accounts.isEmpty()) {
            return firstName + " " + lastName + " has no accounts.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Accounts for ").append(firstName).append(" ").append(lastName).append(":\n\n");
        for (Account acc : accounts) {
            sb.append(acc.toString()).append("\n");
        }
        return sb.toString();
    }
}

