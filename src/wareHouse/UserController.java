package wareHouse;

import Models.UserModel;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import DB.DBConnection;

public class UserController {

    private String currentUser, currentRole;
    private Locale currentLocale = new Locale("");

    private ResourceBundle bundle;

    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtPassword;
    @FXML
    private ComboBox<String> comboRole;
    @FXML
    private TextField txtSearch;
    @FXML
    private Label lbPass;
    @FXML
    private Label lbRole;
    @FXML
    private Label lbname;
    @FXML
    private Button add;
    @FXML
    private Button delete;
    @FXML
    private Button refresh;
    @FXML
    private Button update;
    @FXML
    private Label user_mangment;

    @FXML
    private TableView<UserModel> tableUsers;
    @FXML
    private TableColumn<UserModel, Integer> colId;
    @FXML
    private TableColumn<UserModel, String> colUsername;
    @FXML
    private TableColumn<UserModel, String> colPassword;
    @FXML
    private TableColumn<UserModel, String> colRole;

    private int selectedUserId = -1;

    ObservableList<UserModel> usersList = FXCollections.observableArrayList();

    private int currentUserId;

    public void initData(String user, String role, Locale currentLocale, int user_id) {
        this.currentUser = user;
        this.currentRole = role;
        this.currentLocale = currentLocale;
        this.currentUserId = user_id;
        loadLanguage();
    }

    private void loadLanguage() {
        bundle = ResourceBundle.getBundle("lang.lang", currentLocale);

        txtSearch.setPromptText(bundle.getString("user.search"));
        txtUsername.setPromptText(bundle.getString("user.username"));
        txtPassword.setPromptText(bundle.getString("user.userPassword"));
        comboRole.setPromptText(bundle.getString("user.comRole"));
        user_mangment.setText(bundle.getString("user.titel"));
        colId.setText(bundle.getString("user.id"));
        colUsername.setText(bundle.getString("user.name"));
        colRole.setText(bundle.getString("user.role"));
        colPassword.setText(bundle.getString("user.password"));
        add.setText(bundle.getString("user.add"));
        update.setText(bundle.getString("user.update"));
        delete.setText(bundle.getString("user.delete"));
        refresh.setText(bundle.getString("user.refresh"));

    }

    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

//       comboRole.setItems(FXCollections.observableArrayList("مسؤول النظام", "مدير المخزون", "مدير المشتريات", "مدخل بيانات", "مشاهدِ"));
        loadRoles();
        loadUsers();

        tableUsers.setOnMouseClicked(e -> {
            UserModel u = tableUsers.getSelectionModel().getSelectedItem();
            if (u != null) {
                selectedUserId = u.getUserId();
                txtUsername.setText(u.getUsername());
                txtPassword.setText(u.getPassword());
                comboRole.setValue(u.getRole());
            }
        });

        txtUsername.setTextFormatter(arabicEnglish);
    }

    TextFormatter<String> arabicEnglish = new TextFormatter<>(change -> {
        if (change.getControlNewText().matches("[a-zA-Zأ-ي\\s]*")) {
            return change;
        }
        return null;
    });

    // ================== LOAD ROLES ==================
    private void loadRoles() {
        comboRole.getItems().clear();

        String sql = "SELECT role_name FROM Roles ORDER BY role_id";

        try (Connection conn = DBConnection.getConnection(); ResultSet rs = conn.createStatement().executeQuery(sql)) {

            while (rs.next()) {
                comboRole.getItems().add(rs.getString(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // تحميل المستخدمين
    private void loadUsers() {
        usersList.clear();

        String sql = """
            SELECT u.user_id, u.username, u.password, r.role_name
            FROM Users u
            JOIN Roles r ON u.role_id = r.role_id
            ORDER BY u.user_id
        """;

        try (Connection conn = DBConnection.getConnection(); ResultSet rs = conn.createStatement().executeQuery(sql)) {

            while (rs.next()) {
                usersList.add(new UserModel(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role_name")
                ));
            }

            tableUsers.setItems(usersList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- ADD USER ----------------
    @FXML
    private void addUser() {

        // ---------------- VALIDATION ----------------
        if (!validate()) {
            return;
        }

        String sql = """
            INSERT INTO Users (user_id, username, password, role_id)
            VALUES (seq_user_id.NEXTVAL, ?, ?, 
            (SELECT role_id FROM Roles WHERE role_name = ?))
        """;

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, txtUsername.getText());
            ps.setString(2, txtPassword.getText());
            ps.setString(3, comboRole.getValue());
            ps.executeUpdate();

            showAlert("Success",
                    currentLocale.getLanguage().equals("en")
                    ? "User added successfully!" : "تمت إضافة المستخدم بنجاح");
            refreshUser();

        } catch (Exception e) {
            showAlert("faild",
                    currentLocale.getLanguage().equals("en")
                    ? "Error adding User" : "حدث خطأ أثناء إضافة المستخدم");

        }
        clearFields();
    }

    // ---------------- EDIT USER ----------------
    @FXML
    private void editUser() {

        if (selectedUserId == -1) {
            showAlert("faild",
                    currentLocale.getLanguage().equals("en")
                    ? "Choose an User to edit!" : "اختر مستخدم لتعديله!");
            return;
        }

        // ---------------- VALIDATION ----------------
        if (!validate()) {
            return;
        }

        String sql = """
            UPDATE Users
            SET username = ?, password = ?,
                role_id = (SELECT role_id FROM Roles WHERE role_name = ?)
            WHERE user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, txtUsername.getText());
            ps.setString(2, txtPassword.getText());
            ps.setString(3, comboRole.getValue());
            ps.setInt(4, selectedUserId);
            ps.executeUpdate();

            showAlert("Success",
                    currentLocale.getLanguage().equals("en")
                    ? "The data updated successfully" : "تم تعديل البيانات بنجاح!");
            refreshUser();

        } catch (Exception e) {
            showAlert("faild",
                    currentLocale.getLanguage().equals("en")
                    ? "An error occurred while modifying the User." : "حدث خطأ أثناء تعديل المستخدم");

        }
        clearFields();
    }

// ---------------- DELETE USER ----------------
    @FXML
    private void deleteUser() {

        if (selectedUserId == -1) {
            showAlert("faild",
                    currentLocale.getLanguage().equals("en")
                    ? "Please select a User to delete" : "يرجى اختيار مستخدم للحذف");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, currentLocale.getLanguage().equals("en")
                ? "Delete user with ID " + selectedUserId + " ?" : "حذف المستخدم رقم " + selectedUserId + " ?", ButtonType.YES, ButtonType.NO);

        if (confirm.showAndWait().get() == ButtonType.NO) {
            return;
        }

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps
                = conn.prepareStatement("DELETE FROM Users WHERE user_id = ?")) {

            ps.setInt(1, selectedUserId);
            ps.executeUpdate();

            showAlert("Success",
                    currentLocale.getLanguage().equals("en")
                    ? "Deleted successfully" : "تم الحذف بنجاح");

            refreshUser();

        } catch (Exception e) {
            showAlert("faild",
                    currentLocale.getLanguage().equals("en")
                    ? "An error occurred while Deleted the User." : "حدث خطأ أثناء حذف المستخدم");

        }

    }

    // ---------------- BACK ----------------
    @FXML
    private void goBack(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Home.fxml"));
            Parent root = loader.load();

            HomeController HomeController = loader.getController();
            HomeController.initData(currentUser, currentRole, currentLocale, currentUserId);

            FadeTransition ft = new FadeTransition(Duration.millis(300), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            Scene homeScene = new Scene(root);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(homeScene);
            window.setTitle("Warehouse");
            window.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================== SEARCH ==================
    @FXML
    private void searchUser() {

        String key = txtSearch.getText().trim().toLowerCase();

        if (key.isEmpty()) {
            loadUsers();
            return;
        }

        usersList.clear();

        String sql = """
            SELECT u.user_id, u.username, u.password, r.role_name
            FROM Users u
            JOIN Roles r ON u.role_id = r.role_id
            WHERE LOWER(u.username) LIKE ?
               OR LOWER(r.role_name) LIKE ?
        """;

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + key + "%");
            ps.setString(2, "%" + key + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                usersList.add(new UserModel(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role_name")
                ));
            }

            tableUsers.setItems(usersList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================
    // CLEAR FIELDS
    // =============================
    void clearFields() {
        txtUsername.clear();
        txtPassword.clear();
        comboRole.getSelectionModel().clearSelection();
        selectedUserId = -1;

        lbname.setText("");
        lbPass.setText("");
        lbRole.setText("");
    }

    // ================== REFRESH ==================
    @FXML
    private void refreshUser() {
        clearFields();
        loadUsers();
    }

    private void showAlert(String title, String msg) {
        Alert.AlertType type = title.equals("Success")
                ? Alert.AlertType.INFORMATION
                : Alert.AlertType.WARNING;

        Alert a = new Alert(type, msg, ButtonType.OK);
        a.setTitle(title);
        a.show();
    }

    private boolean validate() {
        boolean valid = true;

        if (txtUsername.getText().trim().isEmpty() || comboRole.getSelectionModel().isEmpty() || txtPassword.getText().trim().isEmpty()) {

            if (txtUsername.getText().trim().isEmpty()) {
                lbname.setText(currentLocale.getLanguage().equals("en")
                        ? "Enter the name" : "ادخل الأسم");
            } else if (!txtUsername.getText().matches("[a-zA-Zأ-ي\\s]*")) {
                lbname.setText(currentLocale.getLanguage().equals("en")
                        ? "You must enter letters" : "يجب عليك إدخال الحروف");
            } else if (txtUsername.getText().length() < 3) {
                lbname.setText(currentLocale.getLanguage().equals("en")
                        ? "It must be more than three letters" : "يجب أن يكون أكثر من  ثلاث احرف");
            } else {
                lbname.setText("");
            }

            if (txtPassword.getText().trim().isEmpty()) {
                lbPass.setText(currentLocale.getLanguage().equals("en")
                        ? "Enter the Password" : "ادخل كلمة السر");
            } else if (txtPassword.getText().length() < 3) {
                lbPass.setText(currentLocale.getLanguage().equals("en")
                        ? "It must be more than three letters" : "يجب أن يكون أكثر من  ثلاث احرف");

            } else {
                lbPass.setText("");
            }

            if (comboRole.getSelectionModel().isEmpty()) {
                lbRole.setText(currentLocale.getLanguage().equals("en")
                        ? "Choose the Role" : "اختر الصلاحية");
            } else {
                lbRole.setText("");
            }
            return false;
        }

        return valid;
    }

    @FXML
    private void Username() {
        if (!txtUsername.getText().trim().isEmpty()) {
            lbname.setText("");
        }
    }

    @FXML
    private void Password() {
        if (!txtPassword.getText().trim().isEmpty()) {
            lbPass.setText("");
        }
    }

    @FXML
    private void comboRole() {
        lbRole.setText("");
    }

}
