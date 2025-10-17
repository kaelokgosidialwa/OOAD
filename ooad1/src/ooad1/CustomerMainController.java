package ooad1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

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
    private Label greetingLabel; // Add a label in FXML at the top for "Hello, Firstname"

    // ---------------------
    // Initialize scene
    // ---------------------
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

        // Update account details when selection changes
        accountSelector.setOnAction(e -> {
            Account selected = accountSelector.getSelectionModel().getSelectedItem();
            updateAccountDetails(selected);
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
        System.out.println("Returning to login...");
        SessionUser.clear(); // Clear session on logout
        // TODO: implement navigation to login scene
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
        System.out.println("Deposit clicked for account: " + selected.getAccNumber());
        // TODO: implement deposit logic
    }

    @FXML
    private void handleWithdraw(ActionEvent event) {
        Account selected = accountSelector.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        System.out.println("Withdraw clicked for account: " + selected.getAccNumber());
        // TODO: implement withdraw logic
    }

    @FXML
    private void handleViewTransactions(ActionEvent event) {
        Account selected = accountSelector.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        System.out.println("View transactions clicked for account: " + selected.getAccNumber());
        // TODO: implement transactions display logic
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        Account selected = accountSelector.getSelectionModel().getSelectedItem();
        updateAccountDetails(selected);
        System.out.println("Refreshed account details for: " + (selected != null ? selected.getAccNumber() : "none"));
    }
}
