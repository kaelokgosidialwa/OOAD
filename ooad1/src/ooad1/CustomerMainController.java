package ooad1;

import java.io.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;
import javafx.fxml.*;
import javafx.stage.*;
import java.util.*;
import java.util.Optional;

public class CustomerMainController {

    @FXML
    private ComboBox<Account> accountSelector;

    @FXML
    private Label balanceLabel;

    @FXML
    private Label typeLabel;

    @FXML
    private Label numberLabel;

    @FXML
    private TextField amountField;

    @FXML
    private Label greetingLabel;
    
    @FXML
    private MenuBar menumain;

    @FXML
    private Menu backmain;

    @FXML
    private MenuItem prevmain;

    @FXML
    private MenuItem tologinmain;
    
    @FXML
    private Button withdrawButton;
    
    @FXML
    private Button depositButton;
    
    private List<String> sessionTransactions = new ArrayList<>();

    @FXML
    private void initialize() {
        Customer sessionUser = SessionUser.getCustomer();
        if (sessionUser == null) return;

        // Set greeting
        if (greetingLabel != null) {
            greetingLabel.setText("Hello, " + sessionUser.getFirstName() + "!");
        }

        // Populate account dropdown
        accountSelector.getItems().clear();
        accountSelector.getItems().addAll(sessionUser.getAccounts());

        // Auto-select first account if available
        if (!sessionUser.getAccounts().isEmpty()) {
            accountSelector.getSelectionModel().selectFirst();
            updateAccountDetails(accountSelector.getSelectionModel().getSelectedItem());
        }

        // Update account details and withdraw button when selection changes
        accountSelector.setOnAction(e -> {
            Account selected = accountSelector.getSelectionModel().getSelectedItem();
            updateAccountDetails(selected);

            if (selected != null && selected.getAccType().equalsIgnoreCase("savings")) {
                withdrawButton.setDisable(true);
                withdrawButton.setText("Cannot Withdraw");
            } else {
                withdrawButton.setDisable(false);
                withdrawButton.setText("Withdraw");
            }
        });
    }

    // ---------------------
    // Update labels with selected account info
    // ---------------------
    private void updateAccountDetails(Account account) {
        if (account == null) {
            balanceLabel.setText("Balance: $0.00");
            typeLabel.setText("Account Type:");
            numberLabel.setText("Account Number:");
        } else {
            balanceLabel.setText("Balance: $" + account.getBalance());
            typeLabel.setText("Account Type: " + account.getAccType());
            numberLabel.setText("Account Number: " + account.getAccNumber());
        }
    }

    // ---------------------
    // Menu actions
    // ---------------------
    @FXML
    private void PrevScene(ActionEvent event) {
        System.out.println("Going to previous scene...");
        // TODO: implement navigation logic
    }

    @FXML
    private void BackToLogin(ActionEvent event) {
        Customer sessionUser = SessionUser.getCustomer();
        boolean saveFailed = false;

        // Attempt to save the session user
        if (sessionUser != null) {
            try {
                DatabaseUpdate.saveCustomer(sessionUser);
            } catch (Exception e) {
                saveFailed = true;
                e.printStackTrace();
            }
        }

        // Build the confirmation alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log Out");

        if (saveFailed) {
            alert.setHeaderText("Saving Failed!");
            alert.setContentText("Changes to your accounts could not be saved.\n" +
                                 "Are you sure you want to log out? This session will not be reflected on your account!");
        } else {
            alert.setHeaderText("This will log you out of the current session.");
            alert.setContentText("Are you sure you want to log out?");
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {

            // Clear session
            SessionUser.clear();

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
    private void handleAbout(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Banking System v0.1");
        alert.setContentText("This system allows customers to manage their bank accounts — view, deposit, and withdraw funds.\n\nDeveloped by OOAD1 Team.");
        alert.showAndWait();
    }

    // ---------------------
    // Account actions (dummy logic)
    // ---------------------
    @FXML
    private void handleDeposit(ActionEvent event) {
        Account selected = accountSelector.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Deposit Error", "Please enter an amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Deposit Error", "Invalid number entered.");
            return;
        }

        if (amount <= 0) {
            showAlert(Alert.AlertType.WARNING, "Deposit Error", "Amount must be greater than zero.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deposit");
        confirm.setHeaderText("Deposit $" + amount + " into account " + selected.getAccNumber() + "?");
        confirm.setContentText("Are you sure you want to proceed?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            double oldBalance = selected.getBalance(); // backup in case save fails
            selected.setBalance(oldBalance + amount);   // update in memory

            updateAccountDetails(selected);
            amountField.clear();

            // Try to save
            try {
                DatabaseUpdate.saveCustomer(SessionUser.getCustomer());
                sessionTransactions.add("Deposited $" + amount + " into account " + selected.getAccNumber());
                showAlert(Alert.AlertType.INFORMATION, "Deposit Successful",
                        "Successfully deposited $" + amount + " into account " + selected.getAccNumber() + ".");
            } catch (Exception ex) {
                // Rollback in-memory balance
                selected.setBalance(oldBalance);
                updateAccountDetails(selected);
                showAlert(Alert.AlertType.ERROR, "Deposit Failed",
                        "Deposit failed. Your money has not been deposited.");
                ex.printStackTrace();
            }
        }
    }

    @FXML
    private void handleWithdraw(ActionEvent event) {
        Account selected = accountSelector.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (selected.getAccType().equalsIgnoreCase("savings")) {
            showAlert(Alert.AlertType.INFORMATION, "Withdrawal Not Allowed",
                    "Withdrawals are not allowed from Savings accounts.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Amount", "Please enter a valid positive number.");
            return;
        }

        if (amount > selected.getBalance()) {
            showAlert(Alert.AlertType.ERROR, "Insufficient Funds",
                    "You cannot withdraw more than the current balance.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Withdrawal");
        confirm.setHeaderText(null);
        confirm.setContentText("Withdraw $" + amount + " from account " + selected.getAccNumber() + "?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            double oldBalance = selected.getBalance(); // backup
            selected.setBalance(oldBalance - amount);

            updateAccountDetails(selected);

            try {
                DatabaseUpdate.saveCustomer(SessionUser.getCustomer());
                sessionTransactions.add("Withdrew $" + amount + " from account " + selected.getAccNumber());
                showAlert(Alert.AlertType.INFORMATION, "Withdrawal Successful",
                        "Withdrawal of $" + amount + " completed.\nNew balance: " + selected.getBalance());
            } catch (Exception ex) {
                // Rollback in-memory balance
                selected.setBalance(oldBalance);
                updateAccountDetails(selected);
                showAlert(Alert.AlertType.ERROR, "Withdrawal Failed",
                        "Withdrawal failed. Your money has been returned.");
                ex.printStackTrace();
            }
        }
    }


    @FXML
    private void handleViewTransactions(ActionEvent event) {
        Account selected = accountSelector.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("Transactions this session for account ").append(selected.getAccNumber()).append(":\n\n");

        boolean hasTransactions = false;
        for (String t : sessionTransactions) {
            if (t.contains(selected.getAccNumber())) { // only show for this account
                sb.append(t).append("\n");
                hasTransactions = true;
            }
        }

        if (!hasTransactions) {
            sb.append("No transactions have been made for this account in this session.");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Session Transactions");
        alert.setHeaderText(null);
        alert.setContentText(sb.toString());
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); // makes long text scrollable
        alert.showAndWait();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        Account selected = accountSelector.getSelectionModel().getSelectedItem();
        updateAccountDetails(selected);
        System.out.println("Refreshed account details for: " + (selected != null ? selected.getAccNumber() : "none"));
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
