package ooad1;


import java.io.IOException;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.*;

public class LoginScreenController {

    
    @FXML private MenuBar menumain;
    @FXML private Menu backmain;
    @FXML private MenuItem prevmain;
    @FXML private MenuItem tologinmain;

    
    @FXML private Button logBrrch;
    @FXML private Button Logcus;
    @FXML private Button signup;
    
    public Customer sessionUser;

    
    @FXML
    private void PrevScene(ActionEvent event) {
        System.out.println("Going to previous scene...");
    }

    @FXML
    private void BackToLogin(ActionEvent event) {
        System.out.println("Returning to login...");
    }

    @FXML
    private void LoginBranch(ActionEvent event) {
        // Create a custom dialog for branch login
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Branch Login");
        dialog.setHeaderText("Enter Branch Name and Password");

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Grid for branch name and password
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField branchField = new TextField();
        branchField.setPromptText("Branch Name");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        grid.add(new Label("Branch Name:"), 0, 0);
        grid.add(branchField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Convert result to a pair when login is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(branchField.getText().trim(), passwordField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(branchPassword -> {
            String branchName = branchPassword.getKey();
            String password = branchPassword.getValue();

            // === Check credentials in DB ===
            Branch branch = Database.checkCredentialsBranch(branchName, password);

            if (branch != null) {
                // Load all customers and accounts for this branch
                Database.loadBranch(branch);

                // Store branch in session
                SessionBranch.setBranch(branch);

                // Switch to BranchMain.fxml
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/BranchMain.fxml"));
                    Parent root = loader.load();

                    BranchMainController controller = loader.getController();
                    controller.setSessionBranch(branch); // pass session to controller

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root, 800, 800));
                    stage.setTitle("Branch Main");
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Failed to load BranchMain scene.");
                }

            } else {
                showAlert("Invalid branch name or password");
            }
        });
    }


    @FXML
    private void LoginCustomer(ActionEvent event) {
        // Create login dialog
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Customer Login");
        dialog.setHeaderText("Enter Username and Password");

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Firstname Lastname");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(usernameField.getText(), passwordField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(userPassword -> {
            String username = userPassword.getKey().trim();
            String password = userPassword.getValue();

            String[] parts = username.split("\\s+");
            if (parts.length < 2) {
                showAlert("Username must be in the format 'Firstname Lastname'");
                return;
            }
            String firstName = parts[0];
            String lastName = parts[1];

            // Check credentials via your new method
            Customer customer = Database.checkCredentialsCustomer(firstName, lastName, password);

            if (customer != null) {
                // Load all accounts for this customer
                Database.queryAccount("customer_id", customer.getIdNumber(), customer);

                // Store the customer in SessionUser
                SessionUser.setCustomer(customer);

                // Switch to CustomerMain.fxml
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/scenes/CustomerMain.fxml"));
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root, 800, 800));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert("Failed to load CustomerMain scene.");
                }
            } else {
                showAlert("Invalid username or password");
            }
        });
    }

    // Utility method for showing alerts
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void SignUp(ActionEvent event) {
        System.out.println("Sign Up clicked.");
    }
}
