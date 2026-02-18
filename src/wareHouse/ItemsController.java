package wareHouse;

import Models.SupplierModel;
import Models.ItemModel;
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
import javafx.scene.control.*;
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

public class ItemsController {

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

        T_item.setText(bundle.getString("item.t1"));
        nameField.setPromptText(bundle.getString("item.nameField"));
        typeField.setPromptText(bundle.getString("item.typeField"));
        priceField.setPromptText(bundle.getString("item.priceField"));
        quantityField.setPromptText(bundle.getString("item.quantityField"));
        txtSearch.setPromptText(bundle.getString("item.txtSearch"));
        statusCombo.setPromptText(bundle.getString("status.Combo"));
        supplierCombo.setPromptText(bundle.getString("supplier.Combo"));
        colId.setText(bundle.getString("col.Id"));
        colName.setText(bundle.getString("col.Name"));
        colType.setText(bundle.getString("col.Type"));
        colQuantity.setText(bundle.getString("col.Quantity"));
        colPrice.setText(bundle.getString("col.Price"));
        colsupplier_name.setText(bundle.getString("col.supplier_name"));
        colStatus.setText(bundle.getString("col.Status"));
        item_add.setText(bundle.getString("item.add"));
        item_update.setText(bundle.getString("item.update"));
        item_delete.setText(bundle.getString("item.delete"));
        item_refresh.setText(bundle.getString("item.refresh"));

    }

    @FXML
    private TextField nameField;
    @FXML
    private TextField typeField;
    @FXML
    private TextField priceField;
    @FXML
    private TextField quantityField;
    @FXML
    private TextField txtSearch;

    @FXML
    private Button item_add;

    @FXML
    private Button item_delete;

    @FXML
    private Button item_refresh;

    @FXML
    private Button item_update;

    @FXML
    private ComboBox<String> statusCombo;
    @FXML
    private ComboBox<SupplierModel> supplierCombo;

    @FXML
    private Label lbname;
    @FXML
    private Label lbtype;
    @FXML
    private Label lbquantity;
    @FXML
    private Label lbprice;
    @FXML
    private Label lbsupplier;
    @FXML
    private Label lbstatus;
    @FXML
    private Label T_item;

    @FXML
    private TableView<ItemModel> tableItems;
    @FXML
    private TableColumn<ItemModel, Integer> colId;
    @FXML
    private TableColumn<ItemModel, String> colName;
    @FXML
    private TableColumn<ItemModel, String> colType;
    @FXML
    private TableColumn<ItemModel, Integer> colQuantity;
    @FXML
    private TableColumn<ItemModel, Double> colPrice;
    @FXML
    private TableColumn<ItemModel, String> colsupplier_name;
    @FXML
    private TableColumn<ItemModel, String> colStatus;

    ObservableList<ItemModel> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        lodSupplier();
        // إعداد الكومبو
        statusCombo.setItems(FXCollections.observableArrayList("Available", "Unavailable"));

        // ربط الأعمدة
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colsupplier_name.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // منع تعديل الجدول
        tableItems.setEditable(false);

        // عرض البيانات
        tableItems.setItems(loadItems());

        nameField.setTextFormatter(arabicEnglish1);
        typeField.setTextFormatter(arabicEnglish2);
        priceField.setTextFormatter(numbersOnly1);
        quantityField.setTextFormatter(numbersOnly2);
    }

    TextFormatter<String> arabicEnglish1 = new TextFormatter<>(change -> {
        if (change.getControlNewText().matches("[a-zA-Zأ-ي\\s\\d*]*")) {
            return change;
        }
        return null;
    });

    TextFormatter<String> arabicEnglish2 = new TextFormatter<>(change -> {
        if (change.getControlNewText().matches("[a-zA-Zأ-ي\\s]*")) {
            return change;
        }
        return null;
    });

    TextFormatter<String> numbersOnly1 = new TextFormatter<>(change -> {
        if (change.getControlNewText().matches("\\d*(\\.\\d*)?")) {
            return change;
        }
        return null;
    });
    TextFormatter<String> numbersOnly2 = new TextFormatter<>(change -> {
        if (change.getControlNewText().matches("\\d*")) {
            return change;
        }
        return null;
    });

    private ObservableList<ItemModel> loadItems() {
        ObservableList<ItemModel> list = FXCollections.observableArrayList();
        String sql = """
        SELECT i.item_id,
               i.item_name,
               i.item_type,
               i.quantity,
               i.price_per_one,
               s.supplier_name,
               i.status
        FROM Items i
        JOIN Suppliers s ON i.supplier_id = s.supplier_id
    """;

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new ItemModel(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("item_type"),
                        rs.getInt("quantity"),
                        rs.getDouble("price_per_one"),
                        rs.getString("supplier_name"),
                        rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    //    // lodSupplier(تحميل الموردين)
    private void lodSupplier() {
        supplierCombo.getItems().clear();

        String sql = "SELECT supplier_id, supplier_name FROM Suppliers";

        try (Connection conn = DBConnection.getConnection(); ResultSet rs = conn.createStatement().executeQuery(sql)) {

            while (rs.next()) {
                supplierCombo.getItems().add(
                        new SupplierModel(
                                rs.getInt("supplier_id"),
                                rs.getString("supplier_name")
                        )
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================
    // ADD NEW ITEM
    // =============================
    @FXML
    void addItem(ActionEvent event) {

// ---------------- VALIDATION ----------------
        if (!validate()) {
            return;
        }

        int quantity;
        double price;

        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
        } catch (NumberFormatException e) {
            lbquantity.setText(currentLocale.getLanguage().equals("en")
                    ? "The quantity must be a number" : "الكمية لازم تكون رقم");
            return;
        }

        try {
            price = Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException e) {
            lbprice.setText(currentLocale.getLanguage().equals("en")
                    ? "The price must be a number" : "السعر لازم يكون رقم");
            return;
        }

        String sql = """
        INSERT INTO Items (item_id, item_name, item_type, quantity, Price_per_one, supplier_id, status)
        VALUES (seq_item_id.NEXTVAL, ?, ?, ?, ?, ?, ?)
    """;

        try (Connection conn = DBConnection.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, nameField.getText());
            st.setString(2, typeField.getText());
            st.setInt(3, quantity);
            st.setDouble(4, price);
            st.setInt(5, supplierCombo.getValue().getId());
            st.setString(6, statusCombo.getValue());
            st.executeUpdate();

            showAlert("Success",
                    currentLocale.getLanguage().equals("en")
                    ? "Item added successfully!" : "تمت إضافة المنتج بنجاح");

            refreshItems();

        } catch (Exception e) {
            showAlert("faild",
                    currentLocale.getLanguage().equals("en")
                    ? "Error adding item" : "حدث خطأ أثناء إضافة المنتج");
        }
        clearFields();
    }

    // =============================
    //  LOAD SELECTED ROW INTO FIELDS
    // =============================
    @FXML
    void loadSelected() {
        ItemModel selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected != null) {
            nameField.setText(selected.getName());
            typeField.setText(selected.getType());
            priceField.setText(String.valueOf(selected.getPrice()));
            quantityField.setText(String.valueOf(selected.getQuantity()));

            for (SupplierModel s : supplierCombo.getItems()) {
                if (s.getName().equals(selected.getSupplierName())) {
                    supplierCombo.setValue(s);
                    break;
                }
            }
            statusCombo.setValue(selected.getStatus());
        }
    }

    // =============================
    // UPDATE ITEM AFTER EDITING
    // =============================
    @FXML
    void updateItem(ActionEvent event) {

        ItemModel selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected == null) {

            showAlert("faild",
                    currentLocale.getLanguage().equals("en")
                    ? "Choose an item to edit!" : "اختر منتج لتعديله!");

            return;
        }

// ---------------- VALIDATION ----------------
        if (!validate()) {
            return;
        }

        int itemId = selected.getId();
        String name = nameField.getText();
        String type = typeField.getText();
        int quantity = Integer.parseInt(quantityField.getText());
        double price = Double.parseDouble(priceField.getText());
        int supplierId = supplierCombo.getValue().getId();
        String status = statusCombo.getValue();

        String sql = """
        UPDATE Items
        SET item_name = ?, item_type = ?, quantity = ?, PRICE_PER_ONE = ?, supplier_id = ?, status = ?
        WHERE item_id = ?
    """;
        try (Connection conn = DBConnection.getConnection(); PreparedStatement st = conn.prepareStatement(sql)) {

            st.setString(1, name);
            st.setString(2, type);
            st.setInt(3, quantity);
            st.setDouble(4, price);
            st.setInt(5, supplierId);
            st.setString(6, status);
            st.setInt(7, itemId);

            st.executeUpdate();

            showAlert("Success",
                    currentLocale.getLanguage().equals("en")
                    ? "The data updated successfully" : "تم تعديل البيانات بنجاح!");

            selected.setName(name);
            selected.setType(type);
            selected.setPrice(price);
            selected.setQuantity(quantity);
            selected.setSupplier_id(supplierId);
            selected.setStatus(status);
            tableItems.refresh();

            refreshItems();
        } catch (Exception e) {
            showAlert("faild",
                    currentLocale.getLanguage().equals("en")
                    ? "An error occurred while modifying the product." : "حدث خطأ أثناء تعديل المنتج");
        }
        clearFields();
    }

    // =============================
    // DELETE ITEM
    // =============================
    @FXML
    void deleteItem(ActionEvent event) {
        ItemModel selected = tableItems.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("faild",
                    currentLocale.getLanguage().equals("en")
                    ? "Please select a product to delete" : "يرجى اختيار منتج للحذف");
            return;
        }

        Integer id = selected.getId();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, currentLocale.getLanguage().equals("en")
                ? "Delete item ID" + id + " ?" : "حذف المنتج" + id + " ?", ButtonType.YES, ButtonType.NO);

        if (confirm.showAndWait().get() == ButtonType.NO) {
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            String sql = "DELETE FROM Items WHERE item_id = ?";
            PreparedStatement pre = conn.prepareStatement(sql);

            pre.setInt(1, id);

            pre.execute();

            showAlert("Success",
                    currentLocale.getLanguage().equals("en")
                    ? "Deleted successfully" : "تم الحذف بنجاح");

            refreshItems();

        } catch (Exception e) {
            showAlert("faild",
                    currentLocale.getLanguage().equals("en")
                    ? "An error occurred while Deleted the product." : "حدث خطأ أثناء حذف المنتج");

        }
        clearFields();
    }

//    // =============================
//    // refresh(تجديد)
//    // =============================
    @FXML
    private void refreshItems() {
        clearFields();
        tableItems.setItems(loadItems());
    }

    // =============================
    // search
    // ============================= 
    @FXML
    private void searchItems() {

        String keyword = txtSearch.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {
            refreshItems();
            return;
        }

        list.clear();

        try (Connection conn = DBConnection.getConnection()) {

            String sql = """
    SELECT i.item_id,
           i.item_name,
           i.item_type,
           i.quantity,
           i.price_per_one,
           s.supplier_name,
           i.status
    FROM Items i
    JOIN Suppliers s ON i.supplier_id = s.supplier_id
    WHERE LOWER(i.item_name) LIKE ?
       OR LOWER(i.item_type) LIKE ?
       OR LOWER(i.status) LIKE ?
       OR LOWER(s.supplier_name) LIKE ?
       OR TO_CHAR(price_per_one) LIKE ?
       OR TO_CHAR(i.item_id) LIKE ?
       OR TO_CHAR(i.quantity) LIKE ?
""";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, "%" + keyword + "%");  // name
            ps.setString(2, "%" + keyword + "%");  // type
            ps.setString(3, "%" + keyword + "%");  // status
            ps.setString(4, "%" + keyword + "%");  // supplier_name
            ps.setString(5, "%" + keyword + "%");  // price
            ps.setString(6, "%" + keyword + "%");  // item_id
            ps.setString(7, "%" + keyword + "%");  // quantity

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new ItemModel(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("item_type"),
                        rs.getInt("quantity"),
                        rs.getDouble("price_per_one"),
                        rs.getString("supplier_name"),
                        rs.getString("status")
                ));
            }

            tableItems.setItems(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================
    // Back botton
    // =============================
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

    // =============================
    // CLEAR INPUT FIELDS
    // =============================
    void clearFields() {
        nameField.clear();
        typeField.clear();
        priceField.clear();
        quantityField.clear();
        supplierCombo.getSelectionModel().clearSelection();
        statusCombo.getSelectionModel().clearSelection();

        lbname.setText("");
        lbtype.setText("");
        lbprice.setText("");
        lbquantity.setText("");
        lbsupplier.setText("");
        lbstatus.setText("");
    }
//    // =============================
//    // showAlert
//    // =============================

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

// ---------------- VALIDATION ----------------
        if (nameField.getText().trim().isEmpty() || typeField.getText().trim().isEmpty() || quantityField.getText().trim().isEmpty()
                || priceField.getText().trim().isEmpty() || supplierCombo.getSelectionModel().isEmpty() || statusCombo.getSelectionModel().isEmpty()) {

            if (nameField.getText().trim().isEmpty()) {
                lbname.setText(currentLocale.getLanguage().equals("en")
                        ? "Enter the name" : "ادخل الأسم");
            } else if (!nameField.getText().matches("[a-zA-Zأ-ي\\s]*")) {
                lbname.setText(currentLocale.getLanguage().equals("en")
                        ? "You must enter letters" : "يجب عليك إدخال الحروف");
            } else if (nameField.getText().length() < 3) {
                lbname.setText(currentLocale.getLanguage().equals("en")
                        ? "It must be more than three letters" : "يجب أن يكون أكثر من  ثلاث احرف");
            } else {
                lbname.setText("");
            }

            if (typeField.getText().trim().isEmpty()) {
                lbtype.setText(currentLocale.getLanguage().equals("en")
                        ? "Enter the type" : "ادخل النوع");
            } else if (!nameField.getText().matches("[a-zA-Zأ-ي\\s]*")) {
                lbtype.setText(currentLocale.getLanguage().equals("en")
                        ? "You must enter letters" : "يجب عليك إدخال حروف");
            } else if (typeField.getText().length() < 3) {
                lbtype.setText(currentLocale.getLanguage().equals("en")
                        ? "It must be more than three letters" : "يجب أن يكون أكثر من  ثلاث احرف");
            } else {
                lbtype.setText("");
            }

            if (quantityField.getText().trim().isEmpty()) {
                lbquantity.setText(currentLocale.getLanguage().equals("en")
                        ? "Enter the quantity" : "ادخل الكمية");
            } else {
                lbquantity.setText("");
            }

            if (priceField.getText().trim().isEmpty()) {
                lbprice.setText(currentLocale.getLanguage().equals("en")
                        ? "Enter the price" : "ادخل السعر");
            } else {
                lbprice.setText("");
            }

            if (supplierCombo.getSelectionModel().isEmpty()) {
                lbsupplier.setText(currentLocale.getLanguage().equals("en")
                        ? "Choose the supplier" : "اختر المورد");
            } else {
                lbsupplier.setText("");
            }

            if (statusCombo.getSelectionModel().isEmpty()) {
                lbstatus.setText(currentLocale.getLanguage().equals("en")
                        ? "Choose the status" : "اختر الحالة");
            } else {
                lbstatus.setText("");
            }
            return false;
        }

        return valid;
    }

    @FXML
    private void txtitemname() {
        if (!nameField.getText().trim().isEmpty()) {

            lbname.setText("");
        }
    }

    @FXML
    private void txtitemtyep() {
        if (!typeField.getText().trim().isEmpty()) {
            lbtype.setText("");
        }
    }

    @FXML
    private void txtitemtquantity() {
        if (!quantityField.getText().trim().isEmpty()) {
            lbquantity.setText("");
        }
    }

    @FXML
    private void txtitemtprice() {
        if (!priceField.getText().trim().isEmpty()) {
            lbprice.setText("");
        }
    }

    @FXML
    private void txtitemtsupplier() {
        lbsupplier.setText("");
    }

    @FXML
    private void txtitemtstatus() {
        lbstatus.setText("");
    }
}

//    TextFormatter<String> arabicEnglish = new TextFormatter<>(change -> {
//    if (change.getControlNewText().matches("[a-zA-Zأ-ي\\s]*")) {
//        return change;
//    }
//    return null;
//});
//
//    TextFormatter<String> lettersOnly = new TextFormatter<>(change -> {
//    if (change.getControlNewText().matches("[a-zA-Z\\s]*")) {
//        return change;
//    }
//    return null;
//});
//
//TextFormatter<String> numbersOnly = new TextFormatter<>(change -> {
//    if (change.getControlNewText().matches("\\d*")) {
//        return change;
//    }
//    return null;
//});
//
//TextFormatter<String> phoneFormatter = new TextFormatter<>(change -> {
//    String newText = change.getControlNewText();
//    if (newText.matches("\\d*") && newText.length() <= 11) {
//        return change;
//    }
//    return null;
//});
