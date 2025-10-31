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
            Branch branch = DatabaseRead.checkCredentialsBranch(branchName, password);

            if (branch != null) {
                // Load all customers and accounts for this branch
                DatabaseRead.loadBranch(branch);

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
            Customer customer = DatabaseRead.checkCredentialsCustomer(firstName, lastName, password);

            if (customer != null) {
                // Load all accounts for this customer
                DatabaseRead.queryAccount("customer_id", customer.getIdNumber(), customer);

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

        // Step 1: Ask user whether to sign up as Customer or Branch
        ChoiceDialog<String> typeDialog = new ChoiceDialog<>("Customer", "Customer", "Branch");
        typeDialog.setTitle("Sign Up");
        typeDialog.setHeaderText("Choose the type of account to create");
        typeDialog.setContentText("Select type:");

        Optional<String> typeResult = typeDialog.showAndWait();
        if (typeResult.isEmpty()) {
            System.out.println("Sign-up cancelled.");
            return;
        }

        String choice = typeResult.get();
        boolean success = false;

        // Step 2: Based on selection, show appropriate input dialogs
        if (choice.equals("Customer")) {
            TextInputDialog firstNameDialog = new TextInputDialog();
            firstNameDialog.setTitle("Customer Sign Up");
            firstNameDialog.setHeaderText("Enter first name");
            Optional<String> firstName = firstNameDialog.showAndWait();
            if (firstName.isEmpty()) return;

            TextInputDialog lastNameDialog = new TextInputDialog();
            lastNameDialog.setTitle("Customer Sign Up");
            lastNameDialog.setHeaderText("Enter last name");
            Optional<String> lastName = lastNameDialog.showAndWait();
            if (lastName.isEmpty()) return;

            TextInputDialog addressDialog = new TextInputDialog();
            addressDialog.setTitle("Customer Sign Up");
            addressDialog.setHeaderText("Enter address");
            Optional<String> address = addressDialog.showAndWait();
            if (address.isEmpty()) return;

            TextInputDialog passwordDialog = new TextInputDialog();
            passwordDialog.setTitle("Customer Sign Up");
            passwordDialog.setHeaderText("Enter password");
            Optional<String> password = passwordDialog.showAndWait();
            if (password.isEmpty()) return;

            // Auto-generate customer ID
            String newId = DatabaseCreate.generateCustomerId();

            // Save to database
            success = DatabaseCreate.createCustomer(newId, firstName.get(), lastName.get(), address.get(), password.get());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Customer created!\nYour ID: " + newId);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create customer.");
            }

        } else if (choice.equals("Branch")) {
            TextInputDialog nameDialog = new TextInputDialog();
            nameDialog.setTitle("Branch Sign Up");
            nameDialog.setHeaderText("Enter branch name");
            Optional<String> branchName = nameDialog.showAndWait();
            if (branchName.isEmpty()) return;

            TextInputDialog addressDialog = new TextInputDialog();
            addressDialog.setTitle("Branch Sign Up");
            addressDialog.setHeaderText("Enter branch address");
            Optional<String> branchAddress = addressDialog.showAndWait();
            if (branchAddress.isEmpty()) return;

            TextInputDialog passwordDialog = new TextInputDialog();
            passwordDialog.setTitle("Branch Sign Up");
            passwordDialog.setHeaderText("Enter password");
            Optional<String> branchPassword = passwordDialog.showAndWait();
            if (branchPassword.isEmpty()) return;

            // Auto-generate branch ID
            int newBranchId = DatabaseCreate.generateBranchId();

            success = DatabaseCreate.createBranch(branchName.get(), branchAddress.get(), branchPassword.get());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Branch created!\nBranch ID: " + newBranchId);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create branch.");
            }
        }
    }

    /**
     * Reusable alert helper
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
