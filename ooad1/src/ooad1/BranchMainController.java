package ooad1;

import java.io.IOException;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
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

    // === Branch session ===
    private Branch sessionBranch;

    public void setSessionBranch(Branch branch) {
        this.sessionBranch = branch;
        BranchLabel.setText("Logged In as Branch: " + branch.getName());
        loadAccounts();
    }

    // -----------------------------
    // Account dropdown and details
    // -----------------------------
    private void loadAccounts() {
        if (sessionBranch == null || sessionBranch.getAccounts().isEmpty()) return;

        accountSelector.getItems().clear();
        accountSelector.getItems().addAll(sessionBranch.getAccounts());

        // Auto-select first account
        accountSelector.getSelectionModel().selectFirst();
        updateAccountDetails(accountSelector.getSelectionModel().getSelectedItem());

        // Update details when selection changes
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

    // -----------------------------
    // Menu actions
    // -----------------------------
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

    // -----------------------------
    // Branch actions
    // -----------------------------
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
    private void CreateAccount(ActionEvent event) {
        System.out.println("Navigating to account creation... (placeholder)");
    }

    @FXML
    private void seeBranchInfo(ActionEvent event) {
        System.out.println("Viewing branch info... (placeholder)");
    }

    // -----------------------------
    // Account operations (placeholders)
    // -----------------------------
    @FXML
    private void handleDeposit(ActionEvent event) {
        Account selected = accountSelector.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Deposit clicked for: " + selected.getAccNumber());
            // TODO: Implement deposit logic here
        }
    }

    @FXML
    private void handleWithdraw(ActionEvent event) {
        Account selected = accountSelector.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Withdraw clicked for: " + selected.getAccNumber());
            // TODO: Implement withdraw logic here
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
        if (selected != null) {
            System.out.println("Pay interest clicked for: " + selected.getAccNumber());
            // TODO: Implement interest payment logic here
        }
    }
}
