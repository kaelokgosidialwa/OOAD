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
import javafx.util.Pair;

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

    // ---------------------- Branch Login ----------------------
    @FXML
    private void LoginBranch(ActionEvent event) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Branch Login");
        dialog.setHeaderText("Enter Branch Name and Password");

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField branchField = new TextField();
        branchField.setPromptText("Branch Name");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        grid.add(new Label("Branch Name:"), 0, 0);
        grid.add(branchField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

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
                    controller.setSessionBranch(branch);

                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root, 800, 800));
                    stage.setTitle("Branch Main");
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to load BranchMain scene.");
                }

            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid branch name or password.");
            }
        });
    }

    // ---------------------- Customer Login ----------------------
    @FXML
    private void LoginCustomer(ActionEvent event) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Customer Login");
        dialog.setHeaderText("Enter Username and Password");

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

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
                return new Pair<>(usernameField.getText().trim(), passwordField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(userPassword -> {
            String username = userPassword.getKey();
            String password = userPassword.getValue();

            String[] parts = username.split("\\s+");
            if (parts.length < 2) {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Username must be in the format 'Firstname Lastname'.");
                return;
            }
            String firstName = parts[0];
            String lastName = parts[1];

            Customer customer = DatabaseRead.checkCredentialsCustomer(firstName, lastName, password);

            if (customer != null) {
                // Accounts are already loaded via checkCredentialsCustomer + queryCustomer
                SessionUser.setCustomer(customer);

                // Switch to CustomerMain.fxml
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/scenes/CustomerMain.fxml"));
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(new Scene(root, 800, 800));
                    stage.setTitle("Customer Main");
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to load CustomerMain scene.");
                }

            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }
        });
    }

    // ---------------------- Sign Up ----------------------
    @FXML
    private void SignUp(ActionEvent event) {
        ChoiceDialog<String> typeDialog = new ChoiceDialog<>("Customer", "Customer", "Branch");
        typeDialog.setTitle("Sign Up");
        typeDialog.setHeaderText("Choose the type of account to create");
        typeDialog.setContentText("Select type:");

        Optional<String> typeResult = typeDialog.showAndWait();
        if (typeResult.isEmpty()) return;

        String choice = typeResult.get();
        boolean success = false;

        if (choice.equals("Customer")) {
            Optional<String> firstName = promptText("Customer Sign Up", "Enter first name");
            Optional<String> lastName  = promptText("Customer Sign Up", "Enter last name");
            Optional<String> address   = promptText("Customer Sign Up", "Enter address");
            Optional<String> password  = promptText("Customer Sign Up", "Enter password");

            if (firstName.isEmpty() || lastName.isEmpty() || address.isEmpty() || password.isEmpty()) return;

            String newId = DatabaseCreate.generateCustomerId();
            success = DatabaseCreate.createCustomer(newId, firstName.get(), lastName.get(), address.get(), password.get());

            if (success) showAlert(Alert.AlertType.INFORMATION, "Success", "Customer created!\nID: " + newId);
            else showAlert(Alert.AlertType.ERROR, "Error", "Failed to create customer.");

        } else if (choice.equals("Branch")) {
            Optional<String> branchName    = promptText("Branch Sign Up", "Enter branch name");
            Optional<String> branchAddress = promptText("Branch Sign Up", "Enter branch address");
            Optional<String> branchPassword= promptText("Branch Sign Up", "Enter password");

            if (branchName.isEmpty() || branchAddress.isEmpty() || branchPassword.isEmpty()) return;

            success = DatabaseCreate.createBranch(branchName.get(), branchAddress.get(), branchPassword.get());
            int newBranchId = DatabaseCreate.generateBranchId();

            if (success) showAlert(Alert.AlertType.INFORMATION, "Success", "Branch created!\nID: " + newBranchId);
            else showAlert(Alert.AlertType.ERROR, "Error", "Failed to create branch.");
        }
    }

    // ---------------------- Helper Methods ----------------------
    private Optional<String> promptText(String title, String header) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        return dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
