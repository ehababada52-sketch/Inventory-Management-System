package wareHouse;

import Models.SupplierModel;
import java.sql.*;
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

public class SupplierController {

    @FXML
    private TableView<SupplierModel> tableSupplier;
    @FXML
    private TableColumn<SupplierModel, Integer> col_id;
    @FXML
    private TableColumn<SupplierModel, String> col_name;
    @FXML
    private TableColumn<SupplierModel, String> col_phone;
    @FXML
    private TableColumn<SupplierModel, String> col_email;
    @FXML
    private TableColumn<SupplierModel, String> col_status;

    @FXML
    private TextField SupplierName;
    @FXML
    private TextField SupplierPhone;
    @FXML
    private TextField Supplieremail;
    @FXML
    private Button add;
    @FXML
    private Button delete;
    @FXML
    private ComboBox<String> statusCombo1;
    @FXML
    private TextField txtSearch;

    @FXML
    private Label lbEmail;
    @FXML
    private Label lbPhone;
    @FXML
    private Label lbStatus;

    @FXML
    private Label lbname;
    @FXML
    private Button edit;
    @FXML
    private Button refresh;
    @FXML
    private Label sm;

    private ObservableList<SupplierModel> listM = FXCollections.observableArrayList();

    private String currentUser, currentRole;

    private Locale currentLocale = new Locale("");
    private ResourceBundle bundle;
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

        SupplierName.setPromptText(bundle.getString("Supplier.Name"));
        SupplierPhone.setPromptText(bundle.getString("Supplier.Phone"));
        Supplieremail.setPromptText(bundle.getString("Supplier.email"));
        statusCombo1.setPromptText(bundle.getString("Supplier.status.Combo1"));
        add.setText(bundle.getString("Supplier.add"));
        delete.setText(bundle.getString("Supplier.delete"));
        edit.setText(bundle.getString("Supplier.edit"));
        refresh.setText(bundle.getString("Supplier.refresh"));
        txtSearch.setPromptText(bundle.getString("Supplier.txtSearch"));
        col_id.setText(bundle.getString("Supplier.col_id"));
        col_name.setText(bundle.getString("Supplier.col_name"));
        col_phone.setText(bundle.getString("Supplier.col_phone"));
        col_email.setText(bundle.getString("Supplier.col_email"));
        col_status.setText(bundle.getString("Supplier.col_status"));
        sm.setText(bundle.getString("Supplier.sm"));
    }

    @FXML
    public void initialize() {

        // columns
        col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        col_phone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        col_email.setCellValueFactory(new PropertyValueFactory<>("email"));
        col_status.setCellValueFactory(new PropertyValueFactory<>("status"));

        // combo
        statusCombo1.setItems(FXCollections.observableArrayList("Available", "Unavailable"));
        statusCombo1.setPromptText("اختر الحالة");

        loadSuppliers();
        tableSupplier.setEditable(false);

        SupplierName.setTextFormatter(arabicEnglish);
        SupplierPhone.setTextFormatter(phoneFormatter);

    }

    TextFormatter<String> arabicEnglish = new TextFormatter<>(change -> {
        if (change.getControlNewText().matches("[a-zA-Zأ-ي\\s]*")) {
            return change;
        }
        return null;
    });
    TextFormatter<String> phoneFormatter = new TextFormatter<>(change -> {
        String newText = change.getControlNewText();
        if (newText.matches("\\d*") && newText.length() <= 11) {
            return change;
        }
        return null;
    });

    // ================= LOAD SUPPLIERS =================
    private void loadSuppliers() {
        listM.clear();
        String sql = "SELECT * FROM Suppliers ORDER BY supplier_id";

        try (Connection conn = DBConnection.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                listM.add(new SupplierModel(
                        rs.getInt("supplier_id"),
                        rs.getString("supplier_name"),
                        rs.getString("supplier_phone"),
                        rs.getString("supplier_email"),
                        rs.getString("supplier_status")
                ));
            }
            tableSupplier.setItems(listM);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= ADD SUPPLIER =================
    @FXML
    void addSupplier(ActionEvent event) {

        // ---------------- VALIDATION ----------------
        if (!validate()) {
            return;
        }

        String sql = """
                INSERT INTO Suppliers
                (supplier_id, supplier_name, supplier_phone, supplier_email, supplier_status)
                VALUES (seq_supplier_id.NEXTVAL, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, SupplierName.getText());
            ps.setString(2, SupplierPhone.getText());
            ps.setString(3, Supplieremail.getText());
            ps.setString(4, statusCombo1.getValue());

            ps.executeUpdate();

            showAlert("Success",
                    currentLocale.getLanguage().equals("en")
                    ? "Supplier added successfully!" : "تمت إضافة المورد بنجاح");
            clearFields();
            loadSuppliers();

        } catch (Exception e) {
            showAlert("faild",
                    currentLocale.getLanguage().equals("en")
                    ? "An error occurred while added the Supplier." : "حدث خطأ أثناء إضافة المورد");

        }
    }

    // ================= LOAD SELECTED =================\
    @FXML
    private void loadSelected() {
        SupplierModel selected = tableSupplier.getSelectionModel().getSelectedItem();
        if (selected != null) {
            SupplierName.setText(selected.getName());
            SupplierPhone.setText(selected.getPhone());
            Supplieremail.setText(selected.getEmail());
            statusCombo1.setValue(selected.getStatus());
        }
    }
// ----------updateSupplier----------

    @FXML
    private void updateSupplier() {
        SupplierModel selected = tableSupplier.getSelectionModel().getSelectedItem();
        if (selected == null) {

            showAlert("faild",
                    currentLocale.getLanguage().equals("en")
                    ? "Choose an Supplier to edit!" : "اختر مورد لتعديله!");
            return;
        }

        // ---------------- VALIDATION ----------------
        if (!validate()) {
            return;
        }

        String sql = """
        UPDATE Suppliers
        SET supplier_name = ?,
            supplier_phone = ?,
            supplier_email = ?,
            supplier_status = ?
        WHERE supplier_id = ?
    """;

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, SupplierName.getText());
            ps.setString(2, SupplierPhone.getText());
            ps.setString(3, Supplieremail.getText());
            ps.setString(4, statusCombo1.getValue());
            ps.setInt(5, selected.getId());

            ps.executeUpdate();

            showAlert("Success",
                    currentLocale.getLanguage().equals("en")
                    ? "The data updated successfully" : "تم تعديل البيانات بنجاح!");
            loadSuppliers();
            clearFields();

        } catch (Exception e) {
            showAlert("faild",
                    currentLocale.getLanguage().equals("en")
                    ? "An error occurred while updated the Supplier." : "حدث خطأ أثناء تعديل المورد");

        }
    }
//-----------deleteSupplier-------------

    @FXML
    private void deleteSupplier() {

        SupplierModel selected = tableSupplier.getSelectionModel().getSelectedItem();
        if (selected == null) {

            showAlert("faild",
                    currentLocale.getLanguage().equals("en")
                    ? "Please select a Supplier to delete" : "يرجى اختيار مورد للحذف");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, currentLocale.getLanguage().equals("en")
                ? "Delete Supplier ID " + selected.getId() + " ?" : "هل تريد حذف المورد رقم " + selected.getId() + " ?", ButtonType.YES, ButtonType.NO);

        if (confirm.showAndWait().get() == ButtonType.NO) {
            return;
        }

        String sql = "DELETE FROM Suppliers WHERE supplier_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, selected.getId());
            ps.executeUpdate();

            showAlert("Success",
                    currentLocale.getLanguage().equals("en")
                    ? "Deleted successfully" : "تم الحذف بنجاح");
            loadSuppliers();
            clearFields();

        } catch (Exception e) {
            showAlert("faild",
                    currentLocale.getLanguage().equals("en")
                    ? "An error occurred while Deleted the Supplier." : "حدث خطأ أثناء حذف المورد");

        }

    }

    // ================= BACK =================
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
    //----------searchSupplier------------

    @FXML
    private void searchSupplier() {
        String key = txtSearch.getText().trim().toLowerCase();

        if (key.isEmpty()) {
            Refresh();
            return;
        }
        // امسح الجدول الأول
        listM.clear();

        String sql = """
        SELECT * FROM Suppliers
        WHERE LOWER(supplier_name) LIKE ?
           OR supplier_phone LIKE ?
           OR LOWER(supplier_status) LIKE ?
    """;

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + key + "%");
            ps.setString(2, "%" + key + "%");
            ps.setString(3, "%" + key + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                listM.add(new SupplierModel(
                        rs.getInt("supplier_id"),
                        rs.getString("supplier_name"),
                        rs.getString("supplier_phone"),
                        rs.getString("supplier_email"),
                        rs.getString("supplier_status")
                ));
            }

            tableSupplier.setItems(listM);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= REFRESH =================
    @FXML
    private void Refresh() {
        clearFields();
        loadSuppliers();
    }

    private void clearFields() {
        SupplierName.clear();
        SupplierPhone.clear();
        Supplieremail.clear();
        statusCombo1.getSelectionModel().clearSelection();

        lbname.setText("");
        lbPhone.setText("");
        lbEmail.setText("");
        lbStatus.setText("");
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

        if (SupplierName.getText().trim().isEmpty() || SupplierPhone.getText().trim().isEmpty() || Supplieremail.getText().trim().isEmpty()
                || statusCombo1.getSelectionModel().isEmpty() || !SupplierName.getText().matches("[a-zA-Zأ-ي\\s]*")
                || SupplierName.getText().length() < 3 || !SupplierPhone.getText().matches("\\d+") || SupplierPhone.getText().length() != 11) {

            if (SupplierName.getText().trim().isEmpty()) {
                lbname.setText(currentLocale.getLanguage().equals("en")
                        ? "Enter the name" : "ادخل الأسم");
            } else if (!SupplierName.getText().matches("[a-zA-Zأ-ي\\s]*")) {
                lbname.setText(currentLocale.getLanguage().equals("en")
                        ? "You must enter letters" : "يجب عليك إدخال الحروف");
            } else if (SupplierName.getText().length() < 3) {
                lbname.setText(currentLocale.getLanguage().equals("en")
                        ? "It must be more than three letters" : "يجب أن يكون أكثر من  ثلاث احرف");
            } else {
                lbname.setText("");
            }

            if (SupplierPhone.getText().trim().isEmpty()) {
                lbPhone.setText(currentLocale.getLanguage().equals("en")
                        ? "Enter the Phone number" : "ادخل الرقم");
            } else if (!SupplierPhone.getText().matches("\\d+")) {
                lbPhone.setText(currentLocale.getLanguage().equals("en")
                        ? "You must enter numbers only." : "يجب عليك ادخال ارقام فقط");
            } else if (SupplierPhone.getText().length() != 11) {
                lbPhone.setText(currentLocale.getLanguage().equals("en")
                        ? "It must be eleven digits" : "يجب أن يكون احدا عشر رقم");
            } else {
                lbPhone.setText("");
            }
            if (Supplieremail.getText().trim().isEmpty()) {

                lbEmail.setText(currentLocale.getLanguage().equals("en")
                        ? "Enter your email" : "ادخل البريد الالكتروني");
            } else if (!Supplieremail.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                lbEmail.setText(currentLocale.getLanguage().equals("en")
                        ? "Enter a valid email address" : "ادخل بريد الكتروني صحيح");
            } else {
                lbEmail.setText("");
            }

            if (statusCombo1.getSelectionModel().isEmpty()) {
                lbStatus.setText(currentLocale.getLanguage().equals("en")
                        ? "Choose the status" : "اختر الحالة");
            } else {
                lbStatus.setText("");
            }
            return false;
        }

        return valid;
    }

    @FXML
    private void SupplierName() {
        if (!SupplierName.getText().trim().isEmpty()) {

            lbname.setText("");
        }
    }

    @FXML
    private void SupplierPhone() {
        if (!SupplierPhone.getText().trim().isEmpty()) {
            lbPhone.setText("");
        }
    }

    @FXML
    private void Supplieremail() {
        if (!Supplieremail.getText().trim().isEmpty()) {
            lbEmail.setText("");
        }
    }

    @FXML
    private void SupplierStatus() {
        lbStatus.setText("");
    }

}
