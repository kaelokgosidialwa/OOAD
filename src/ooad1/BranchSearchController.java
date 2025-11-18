package ooad1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import java.io.IOException;


public class BranchSearchController {

    @FXML
    private MenuBar menumain;

    @FXML
    private MenuItem prevmain;

    @FXML
    private MenuItem tologinmain;

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> branchListView;

    @FXML
    private ComboBox<String> searchTypeComboBox;

    private final ObservableList<String> searchResults = FXCollections.observableArrayList();

    // --- Session branch object ---
    private Branch sessionBranch;

    // --- Set the session branch from previous scene ---
    public void setSessionBranch(Branch branch) {
        this.sessionBranch = branch;
        if (branch != null) {
            System.out.println("Session branch set: " + branch.getName());
        }
    }

    @FXML
    public void initialize() {
        // Initialize search type ComboBox
        searchTypeComboBox.setItems(FXCollections.observableArrayList(
                "Account by Name",
                "Account by CustomerID",
                "Customer by Name",
                "Customer by ID"
        ));
        searchTypeComboBox.getSelectionModel().selectFirst();

        // Bind ListView to observable list
        branchListView.setItems(searchResults);

        // Listen for text changes and trigger search
        searchField.textProperty().addListener((obs, oldText, newText) -> performSearch(newText.trim()));
    }

    private void performSearch(String queryText) {
        searchResults.clear();
        if (queryText.isEmpty() || sessionBranch == null) return;

        String searchType = searchTypeComboBox.getSelectionModel().getSelectedItem();

        switch (searchType) {
            case "Account by Name" -> searchAccounts("accNumber", queryText);
            case "Account by CustomerID" -> searchAccounts("customerID", queryText);
            case "Customer by Name" -> searchCustomers("firstName", queryText);
            case "Customer by ID" -> searchCustomers("id", queryText);
        }
    }

    /** Search accounts using session branch's accounts only */
    private void searchAccounts(String fieldName, String value) {
        for (Account a : sessionBranch.getAccounts()) {
            boolean matches = switch (fieldName) {
                case "accNumber" -> a.getAccNumber().toLowerCase().contains(value.toLowerCase());
                case "customerID" -> a.getDbCustomerId().toLowerCase().contains(value.toLowerCase());
                default -> false;
            };
            if (matches) {
                searchResults.add("Account: " + a.getAccNumber()
                        + " | CustomerID: " + a.getDbCustomerId()
                        + " | Balance: " + a.getBalance());
            }
        }
    }

    /** Search customers using session branch's customers only */
    private void searchCustomers(String fieldName, String value) {
        for (Customer c : sessionBranch.getCustomers()) {
            boolean matches = switch (fieldName) {
                case "firstName" -> c.getFirstName().toLowerCase().contains(value.toLowerCase());
                case "id" -> c.getIdNumber().toLowerCase().contains(value.toLowerCase());
                default -> false;
            };
            if (matches) {
                StringBuilder accountInfo = new StringBuilder();
                for (Account a : c.getAccounts()) {
                    if (a.getDbBranchId() == sessionBranch.getBranchId()) {
                        accountInfo.append(a.getAccNumber())
                                .append(" (")
                                .append(a.getBalance())
                                .append("), ");
                    }
                }
                String accountsStr = accountInfo.length() > 0
                        ? accountInfo.substring(0, accountInfo.length() - 2)
                        : "No accounts in this branch";

                searchResults.add("Customer: " + c.getFirstName() + " " + c.getLastName()
                        + " | ID: " + c.getIdNumber()
                        + " | Accounts: " + accountsStr);
            }
        }
    }

    // === Menu navigation === //

    @FXML
    private void PrevScene(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BranchMain.fxml"));
            Parent root = loader.load();

            // Pass the session branch back
            BranchMainController controller = loader.getController();
            controller.setSessionBranch(sessionBranch);

            Stage stage = (Stage) menumain.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Branch Main");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void BackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BookstoreLogIn.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) menumain.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Log In");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
