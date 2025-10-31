package ooad1;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.*;

public class BranchMainController {

    // --- FXML components ---
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

    //
    private Branch sessionBranch;

    public void setSessionBranch(Branch branch) {
        this.sessionBranch = branch;
        BranchLabel.setText("Logged In as Branch: " + branch.getName());
        loadAccounts();
    }

    private void loadAccounts() {
        if (sessionBranch == null) return;

        accountSelector.getItems().clear();
        for (Account acc : sessionBranch.getAccounts()) {
            if (!accountSelector.getItems().contains(acc)) {
                accountSelector.getItems().add(acc);
            }
        }

        accountSelector.getSelectionModel().selectFirst();
        updateAccountDetails(accountSelector.getSelectionModel().getSelectedItem());

        accountSelector.setOnAction(e ->
                updateAccountDetails(accountSelector.getSelectionModel().getSelectedItem()));
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
    private void PrevScene(ActionEvent event) {
        System.out.println("Going to previous scene... (placeholder)");
    }

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
                stage.show();

                System.out.println("Logged out and returned to login screen.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Logout cancelled.");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
                double balance = Double.parseDouble(balanceField.getText());
                String companyAddr = companyAddressField.getText().isEmpty() ? null : companyAddressField.getText();
                Double interest = interestField.getText().isEmpty() ? null : Double.parseDouble(interestField.getText());
                String customerId = customerIdField.getText();

                // --- Generate random account number ---
                String accNumber = DatabaseCreate.generateAccountNumber();

                // --- Instantiate the correct Account subclass ---
                Account newAcc = null;
                switch (accType.toLowerCase()) {
                    case "savings":
                        newAcc = new SavingsAccount(accNumber, sessionBranch, accType, balance);
                        if (interest != null) ((Interest) newAcc).setInterest(interest);
                        break;
                    case "investment":
                        if (balance < 500) {
                            showAlert("Error", "Minimum starting balance for investment is 500.");
                            return;
                        }
                        newAcc = new InvestmentAccount(accNumber, sessionBranch, accType, balance);
                        if (interest != null) ((Interest) newAcc).setInterest(interest);
                        break;
                    case "cheque":
                        // Use the constructor for new Cheque accounts
                        newAcc = new ChequeAccount(accNumber, sessionBranch, customerId, balance, companyAddr);
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
                    // --- Add to sessionBranch and customer ---
                    sessionBranch.addAccount(newAcc);
                    Customer owner = sessionBranch.getCustomers().stream()
                            .filter(c -> c.getIdNumber().equals(customerId))
                            .findFirst()
                            .orElse(null);

                    if (owner != null) owner.addAccount(newAcc);

                    // --- Refresh GUI ---
                    if (accountSelector != null) {
                        accountSelector.getItems().add(newAcc);
                        accountSelector.getSelectionModel().select(newAcc);
                        updateAccountDetails(newAcc);
                    }

                    showAlert("Success", "Account created successfully: " + accNumber);
                } else {
                    showAlert("Error", "Failed to create account in database.");
                }

            } catch (Exception e) {
                showAlert("Error", "Invalid input: " + e.getMessage());
            }
        }
    }



    @FXML
    private void handleWithdraw(ActionEvent event) {
        Account selected = accountSelector.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Check withdrawal rules (e.g., savings cannot withdraw)
        if (selected.getAccType().equalsIgnoreCase("savings")) {
            showAlert(Alert.AlertType.INFORMATION, "Withdrawal Not Allowed", 
                      "Withdrawals are not allowed from Savings accounts.");
            return;
        }

        // Prompt for amount
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Withdraw Funds");
        dialog.setHeaderText("Withdraw from account " + selected.getAccNumber());
        dialog.setContentText("Enter amount:");

        Optional<String> input = dialog.showAndWait();
        if (!input.isPresent()) return;

        double amount;
        try {
            amount = Double.parseDouble(input.get());
            if (amount <= 0 || amount > selected.getBalance()) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Enter a valid amount within balance.");
            return;
        }

        double oldBalance = selected.getBalance();
        selected.setBalance(oldBalance - amount); // Update in memory

        try {
            DatabaseUpdate.saveAccount(selected); // Persist to DB
            showAlert(Alert.AlertType.INFORMATION, "Withdrawal Successful", 
                      "Withdrew $" + amount + ". New balance: $" + selected.getBalance());
            updateAccountDetails(selected); // Update GUI
        } catch (SQLException e) {
            selected.setBalance(oldBalance); // rollback in memory
            showAlert(Alert.AlertType.ERROR, "Withdrawal Failed", "Failed to save withdrawal. No changes made.");
            e.printStackTrace();
        }
    }


    @FXML
    private void handleCloseAccount(ActionEvent event) {
        Account selected = accountSelector.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Close account clicked for: " + selected.getAccNumber());
            // TODO: Implement account closure here
        }
    }

    @FXML
    private void handlePayInterest(ActionEvent event) {
        Account selected = accountSelector.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!(selected instanceof Interest)) {
            showAlert(Alert.AlertType.INFORMATION, "Interest Not Applicable",
                      "Selected account does not earn interest.");
            return;
        }

        Interest interestAcc = (Interest) selected;

        double oldBalance = selected.getBalance();

        try {
            // Apply interest — ensure this method actually updates the account's balance
            interestAcc.payInterest(); // <-- should modify selected.balance internally

            // Save updated account to DB
            DatabaseUpdate.saveAccount(selected);

            // Show message with updated balance
            showAlert(Alert.AlertType.INFORMATION, "Interest Applied",
                      "Interest applied successfully.\nNew balance: $" + selected.getBalance());

            // Refresh GUI labels
            updateAccountDetails(selected);

        } catch (SQLException e) {
            // Rollback balance in memory if saving failed
            selected.setBalance(oldBalance);
            showAlert(Alert.AlertType.ERROR, "Interest Failed",
                      "Failed to save interest. No changes made.");
            e.printStackTrace();
        }
    }


    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
