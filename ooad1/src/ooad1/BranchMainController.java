package ooad1;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class BranchMainController {

    @FXML private MenuBar menumain;
    @FXML private MenuItem prevmain;
    @FXML private MenuItem tologinmain;
    @FXML private Text BranchLabel;

    @FXML private ComboBox<Account> accountSelector;
    @FXML private Label accountNumberLabel;
    @FXML private Label accountTypeLabel;
    @FXML private Label accountBalanceLabel;
    @FXML private Label accountCustomerLabel;

    @FXML private Button SearchButton;
    @FXML private Button createAccount;
    @FXML private Button BranchInfoButton;
    @FXML private Button depositButton;
    @FXML private Button withdrawButton;
    @FXML private Button closeButton;
    @FXML private Button payInterestButton;

    private Branch sessionBranch;

    public void setSessionBranch(Branch branch) {
        this.sessionBranch = branch;
        BranchLabel.setText("Logged In as Branch: " + branch.getName());
        loadAccounts();
    }

    private void loadAccounts() {
        if (sessionBranch == null) return;

        accountSelector.getItems().clear();

        // Use a Set to guarantee unique accounts
        Set<String> seenAccountNumbers = new HashSet<>();
        for (Account acc : sessionBranch.getAccounts()) {
            if (!seenAccountNumbers.contains(acc.getAccNumber())) {
                accountSelector.getItems().add(acc);
                seenAccountNumbers.add(acc.getAccNumber());
            }
        }

        // Select first account if exists
        if (!accountSelector.getItems().isEmpty()) {
            accountSelector.getSelectionModel().selectFirst();
            updateAccountDetails(accountSelector.getSelectionModel().getSelectedItem());
        }

        // Update labels when selection changes
        accountSelector.setOnAction(e -> 
            updateAccountDetails(accountSelector.getSelectionModel().getSelectedItem())
        );
    }

    private void updateAccountDetails(Account account) {
        if (account == null) {
            accountNumberLabel.setText("-");
            accountTypeLabel.setText("-");
            accountBalanceLabel.setText("-");
            accountCustomerLabel.setText("-");
        } else {
            accountNumberLabel.setText(account.getAccNumber());
            accountTypeLabel.setText(account.getAccType());
            accountBalanceLabel.setText(String.format("%.2f", account.getBalance()));
            accountCustomerLabel.setText(account.getDbCustomerId());
        }
    }

    @FXML
    private void PrevScene(ActionEvent event) { System.out.println("Going to previous scene..."); }

    @FXML
    private void BackToLogin(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out");
        alert.setHeaderText("This will log you out of the current session.");
        alert.setContentText("Are you sure you want to log out?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/LoginScreen.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) menumain.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Login");
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void goToSearchDatabase(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/BranchSearch.fxml"));
            Parent root = loader.load();
            BranchSearchController controller = loader.getController();
            controller.setSessionBranch(sessionBranch);
            Stage stage = (Stage) SearchButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Branch Search");
        } catch (IOException e) { e.printStackTrace(); }
    }

    // -------------------- ACCOUNT ACTIONS --------------------
    @FXML
    private void createAccount(ActionEvent event) {
        if (sessionBranch == null) {
            showAlert("Error", "Branch not loaded.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Account");

        // --- Input Fields ---
        TextField customerIdField = new TextField();
        customerIdField.setPromptText("Customer ID (existing)");

        ChoiceBox<String> accTypeBox = new ChoiceBox<>();
        accTypeBox.getItems().addAll("savings", "investment", "cheque");

        TextField balanceField = new TextField();
        balanceField.setPromptText("Starting Balance");

        TextField companyAddressField = new TextField();
        companyAddressField.setPromptText("Company Address (for cheque)");

        TextField interestField = new TextField();
        interestField.setPromptText("Interest Rate (for savings/investment)");

        VBox vbox = new VBox(10,
            new Label("Customer ID:"), customerIdField,
            new Label("Account Type:"), accTypeBox,
            new Label("Starting Balance:"), balanceField,
            companyAddressField,
            interestField
        );
        vbox.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(vbox);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String accType = accTypeBox.getValue();
                if (accType == null) {
                    showAlert("Error", "Please select an account type.");
                    return;
                }

                double balance = Double.parseDouble(balanceField.getText());
                String companyAddr = companyAddressField.getText().isEmpty() ? null : companyAddressField.getText();
                Double interest = interestField.getText().isEmpty() ? null : Double.parseDouble(interestField.getText());
                String customerId = customerIdField.getText();

                // --- Generate random account number ---
                String accNumber = DatabaseCreate.generateAccountNumber();

                // --- Instantiate correct Account subclass in memory ---
                Account newAcc = null;
                switch (accType.toLowerCase()) {
                    case "savings":
                        newAcc = new SavingsAccount(accNumber, accType, balance, sessionBranch.getBranchId(), customerId, null, interest);
                        break;
                    case "investment":
                        if (balance < 500) {
                            showAlert("Error", "Minimum starting balance for investment is 500.");
                            return;
                        }
                        newAcc = new InvestmentAccount(accNumber, accType, balance, sessionBranch.getBranchId(), customerId, null, interest);
                        break;
                    case "cheque":
                        newAcc = new ChequeAccount(accNumber, accType, balance, sessionBranch.getBranchId(), customerId, companyAddr);
                        break;
                    default:
                        showAlert("Error", "Invalid account type.");
                        return;
                }

                // --- Save to database ---
                boolean success = DatabaseCreate.createAccount(
                    accNumber,
                    accType,
                    balance,
                    sessionBranch.getBranchId(),
                    customerId,
                    companyAddr,
                    interest
                );

                if (success) {
                    // --- Add account to sessionBranch ---
                    sessionBranch.addAccount(newAcc);

                    // --- Add to customer if loaded ---
                    Customer owner = sessionBranch.getCustomers().stream()
                            .filter(c -> c.getIdNumber().equals(customerId))
                            .findFirst()
                            .orElse(null);
                    if (owner != null) owner.addAccount(newAcc);

                    // --- Update GUI ---
                    if (!accountSelector.getItems().contains(newAcc)) {
                        accountSelector.getItems().add(newAcc);
                    }
                    accountSelector.getSelectionModel().select(newAcc);
                    updateAccountDetails(newAcc);

                    showAlert("Success", "Account created successfully: " + accNumber);
                } else {
                    showAlert("Error", "Failed to create account in database.");
                }

            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid number input: " + e.getMessage());
            } catch (Exception e) {
                showAlert("Error", "Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    @FXML
    private void handleDeposit(ActionEvent event) {
        Account selected = accountSelector.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Deposit Funds");
        dialog.setHeaderText("Deposit into account " + selected.getAccNumber());
        dialog.setContentText("Enter amount:");
        Optional<String> input = dialog.showAndWait();
        if (!input.isPresent()) return;

        double amount;
        try { amount = Double.parseDouble(input.get()); if (amount <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException e) { showAlert("Error", "Invalid amount."); return; }

        double oldBalance = selected.getBalance();
        selected.setBalance(oldBalance + amount);

        try { DatabaseUpdate.saveAccount(selected); updateAccountDetails(selected); }
        catch (SQLException e) { selected.setBalance(oldBalance); showAlert("Error", "Deposit failed."); e.printStackTrace(); }
    }

    @FXML
    private void handleWithdraw(ActionEvent event) {
        Account selected = accountSelector.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (selected.getAccType().equalsIgnoreCase("savings")) {
            showAlert("Info", "Cannot withdraw from Savings accounts.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Withdraw Funds");
        dialog.setHeaderText("Withdraw from account " + selected.getAccNumber());
        dialog.setContentText("Enter amount:");
        Optional<String> input = dialog.showAndWait();
        if (!input.isPresent()) return;

        double amount;
        try { amount = Double.parseDouble(input.get()); if (amount <= 0 || amount > selected.getBalance()) throw new NumberFormatException(); }
        catch (NumberFormatException e) { showAlert("Error", "Invalid amount."); return; }

        double oldBalance = selected.getBalance();
        selected.setBalance(oldBalance - amount);

        try { DatabaseUpdate.saveAccount(selected); updateAccountDetails(selected); }
        catch (SQLException e) { selected.setBalance(oldBalance); showAlert("Error", "Withdrawal failed."); e.printStackTrace(); }
    }

    @FXML
    private void handleCloseAccount(ActionEvent event) {
        Account selected = accountSelector.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        boolean success = DatabaseDelete.deleteAccount(selected.getAccNumber());
        if (success) {
            sessionBranch.getAccounts().remove(selected);
            accountSelector.getItems().remove(selected);
            updateAccountDetails(null);
            showAlert("Success", "Account closed successfully: " + selected.getAccNumber());
        } else showAlert("Error", "Failed to delete account.");
    }

    @FXML
    private void handlePayInterest(ActionEvent event) {
        Account selected = accountSelector.getSelectionModel().getSelectedItem();
        if (selected == null || !(selected instanceof Interest)) { showAlert("Info", "Interest not applicable."); return; }

        Interest interestAcc = (Interest) selected;
        double oldBalance = selected.getBalance();
        try {
            interestAcc.payInterest();
            DatabaseUpdate.saveAccount(selected);
            updateAccountDetails(selected);
            showAlert("Success", "Interest applied. New balance: $" + selected.getBalance());
        } catch (SQLException e) { selected.setBalance(oldBalance); showAlert("Error", "Failed to apply interest."); e.printStackTrace(); }
    }

    @FXML
    private void seeBranchInfo(ActionEvent event) {
        if (sessionBranch == null) return;
        int customerCount = sessionBranch.getCustomers().size();
        int accountCount = 0;
        double totalBalance = 0;

        for (Customer c : sessionBranch.getCustomers()) {
            for (Account a : c.getAccounts()) {
                accountCount++;
                totalBalance += a.getBalance();
            }
        }

        double avgBalance = accountCount > 0 ? totalBalance / accountCount : 0;
        String info = String.format("Branch ID: %d\nName: %s\nAddress: %s\nCustomers: %d\nAccounts: %d\nTotal Balance: %.2f\nAverage Balance: %.2f",
                sessionBranch.getBranchId(), sessionBranch.getName(), sessionBranch.getAddress(), customerCount, accountCount, totalBalance, avgBalance);
        showAlert("Branch Info", info);
    }

    private void showAlert(String title, String message) { showAlert(Alert.AlertType.INFORMATION, title, message); }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }
}
