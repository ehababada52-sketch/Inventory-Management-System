package wareHouse;

import Models.TransactionModel;
import Models.ItemModel;
import javafx.scene.control.*;
import java.sql.*;
import javafx.scene.control.Alert;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
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

public class TransactionController {

    private String currentUser, currentRole;
    private int currentUserId;

    private Locale currentLocale = new Locale("");

    private ResourceBundle bundle;

    public void initData(String user, String role, Locale currentLocale, int user_id) {
        this.currentUser = user;
        this.currentRole = role;
        this.currentLocale = currentLocale;
        this.currentUserId = user_id;
        loadLanguage();

    }

    private void loadLanguage() {
        bundle = ResourceBundle.getBundle("lang.lang", currentLocale);

        compoitems.setPromptText(bundle.getString("transaction.compoitems"));
        compotype.setPromptText(bundle.getString("transaction.compotype"));
        quantityFil.setPromptText(bundle.getString("transaction.Quantity"));
        textSerch.setPromptText(bundle.getString("transaction.search"));
        datePick.setPromptText(bundle.getString("transaction.datePick"));
        add.setText(bundle.getString("transaction.add"));
        refresh.setText(bundle.getString("transaction.refresh"));
        correction.setText(bundle.getString("transaction.correction"));
        colId.setText(bundle.getString("transaction.colId"));
        colItem.setText(bundle.getString("transaction.colItem"));
        colUser.setText(bundle.getString("transaction.colUser"));
        colType.setText(bundle.getString("transaction.colType"));
        colQty.setText(bundle.getString("transaction.colQty"));
        colDate.setText(bundle.getString("transaction.colDate"));
        transaction.setText(bundle.getString("transaction.transaction"));

    }

    // ================= UI =================
    @FXML
    private ComboBox<ItemModel> compoitems;

    @FXML
    private ComboBox<String> compotype;
    @FXML
    private TextField quantityFil;
    @FXML
    private TextField textSerch;
    @FXML
    private DatePicker datePick;

    @FXML
    private Label LbItem;

    @FXML
    private Label Lbquantity;
    @FXML
    private Label Lbtype;
    @FXML
    private TableView<TransactionModel> table;
    @FXML
    private TableColumn<TransactionModel, Integer> colId;
    @FXML
    private TableColumn<TransactionModel, String> colItem;
    @FXML
    private TableColumn<TransactionModel, String> colUser;
    @FXML
    private TableColumn<TransactionModel, String> colType;
    @FXML
    private TableColumn<TransactionModel, Integer> colQty;
    @FXML
    private TableColumn<TransactionModel, Date> colDate;

    private final ObservableList<TransactionModel> list = FXCollections.observableArrayList();

    @FXML
    private Label transaction;

    @FXML
    private Button correction;

    @FXML
    private Button add;

    @FXML
    private Button refresh;

    // ================= INIT =================
    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        colItem.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colType.setCellValueFactory(new PropertyValueFactory<>("movementType"));
        colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));

        table.setItems(list);

        compotype.setItems(FXCollections.observableArrayList("IN", "OUT"));

        table.setEditable(false);
        datePick.setEditable(false);

        quantityFil.setTextFormatter(numbersOnly);
        loadItems();
        loadTransactions();
    }

    TextFormatter<String> numbersOnly = new TextFormatter<>(change -> {
        if (change.getControlNewText().matches("\\d*")) {
            return change;
        }
        return null;
    });

    // ================= LOADERS =================
    private void loadItems() {
        compoitems.getItems().clear();

        String sql = "SELECT item_id, item_name, item_type, quantity, Price_per_one, supplier_id, status FROM Items";
        try (Connection c = DBConnection.getConnection(); ResultSet rs = c.createStatement().executeQuery(sql)) {

            while (rs.next()) {
                compoitems.getItems().add(new ItemModel(
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getString("item_type"),
                        rs.getInt("quantity"),
                        rs.getDouble("Price_per_one"),
                        rs.getInt("supplier_id"),
                        rs.getString("status")
                )
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        compoitems.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(ItemModel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        compoitems.setButtonCell(compoitems.getCellFactory().call(null));
    }

    private void loadTransactions() {
        list.clear();

        String sql = """
        SELECT t.transaction_id,
               t.item_id,
               i.item_name,
               u.user_id,
               u.username,
               t.movement_type,
               t.quantity,
               t.transaction_date
        FROM Transactions t
        JOIN Items i ON t.item_id = i.item_id
        JOIN Users u ON t.user_id = u.user_id
        ORDER BY t.transaction_id DESC
    """;
        try (Connection c = DBConnection.getConnection(); ResultSet rs = c.createStatement().executeQuery(sql)) {

            while (rs.next()) {
                list.add(new TransactionModel(
                        rs.getInt(1),
                        rs.getInt(2),
                        rs.getString(3),
                        rs.getInt(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getInt(7),
                        rs.getDate(8)
                ));
            }
            table.setItems(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= ADD =================
    @FXML
    private void addTransaction() {
        // ---------------- VALIDATION ----------------
        if (!validate()) {

            return;
        }

//        if (compotype.getValue().equals("OUT")) {
//    if (quantity > compoitems.getValue().getQuantity()) {
//        alert("faild", "Not enough stock");
//        return;
//    }
//}
        LocalDate date = (datePick.getValue() == null)
                ? LocalDate.now()
                : datePick.getValue();

        String sql = """
        INSERT INTO Transactions
        (transaction_id, item_id, user_id, movement_type, quantity, transaction_date)
        VALUES (seq_transaction_id.NEXTVAL, ?, ?, ?, ?, ?)
    """;

        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, compoitems.getValue().getId());
            ps.setInt(2, currentUserId);
            ps.setString(3, compotype.getValue());
            ps.setInt(4, Integer.parseInt(quantityFil.getText()));
            ps.setDate(5, Date.valueOf(date));

            ps.executeUpdate();

           refresh();

        } catch (Exception e) {
            alert(currentLocale.getLanguage().equals("en")
                    ? "erorr" : "حدث خطأ أثناء إضافة التحرك", e.getMessage());
        }
    }

    // ================= CORRECT =================
    @FXML
    private void correctTransaction() {

        TransactionModel old = table.getSelectionModel().getSelectedItem();
        if (old == null) {

            alert("faild",
                    currentLocale.getLanguage().equals("en")
                    ? "Choose a transaction to correct" : "اختار حركة للتصحيح");
            return;
        }

        if (compotype.getValue() == null || quantityFil.getText().isEmpty()) {

            alert("faild",
                    currentLocale.getLanguage().equals("en")
                    ? "Enter the new data" : "ادخل البيانات الجديدة");
            return;
        }

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);

            // 1️⃣ Reverse old transaction
            String reverse = old.getMovementType().equals("IN") ? "OUT" : "IN";

            PreparedStatement reversePs = c.prepareStatement("""
                INSERT INTO Transactions
                (transaction_id, item_id, user_id, movement_type, quantity)
                VALUES (seq_transaction_id.NEXTVAL, ?, ?, ?, ?)
            """);

            PreparedStatement correctPs = c.prepareStatement("""
                INSERT INTO Transactions
                (transaction_id, item_id, user_id, movement_type, quantity)
                VALUES (seq_transaction_id.NEXTVAL, ?, ?, ?, ?)
            """);

            reversePs.setInt(1, old.getItemId());
            reversePs.setInt(2, currentUserId);
            reversePs.setString(3, reverse);
            reversePs.setInt(4, old.getQuantity());
            reversePs.executeUpdate();

            // 2️⃣ Insert corrected transaction
            correctPs.setInt(1, compoitems.getValue().getId());
            correctPs.setString(3, compotype.getValue());
            correctPs.setInt(4, Integer.parseInt(quantityFil.getText()));
            correctPs.executeUpdate();

            c.commit();
            loadTransactions();
            clear();

        } catch (Exception e) {
            alert(currentLocale.getLanguage().equals("en")
                    ? "erorr" : "حدث خطأ أثناء تصحيح التحرك", e.getMessage());

        }
    }

    //------------Back button---------
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

    // ================= SEARCH =================
    @FXML
    private void search() {

        String key = textSerch.getText().trim().toLowerCase();

        list.clear();

        if (key.isEmpty()) {
            loadTransactions();
            return;
        }
        String sql = """
       SELECT t.transaction_id,
               t.item_id,
               i.item_name,
               u.user_id,
               u.username,
               t.movement_type,
               t.quantity,
               t.transaction_date
        FROM Transactions t
        JOIN Items i ON t.item_id = i.item_id
        JOIN Users u ON t.user_id = u.user_id
        WHERE LOWER(i.item_name) LIKE ?
           OR LOWER(u.username) LIKE ?
           OR LOWER(t.movement_type) LIKE ?
           OR TO_CHAR(t.transaction_id) LIKE ?
        ORDER BY t.transaction_id DESC
""";

        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, "%" + key + "%");
            ps.setString(2, "%" + key + "%");
            ps.setString(3, "%" + key + "%");
            ps.setString(4, "%" + key + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new TransactionModel(
                        rs.getInt("transaction_id"),
                        rs.getInt("item_id"),
                        rs.getString("item_name"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("movement_type"),
                        rs.getInt("quantity"),
                        rs.getDate("transaction_date")
                ));
            }
            table.setItems(list);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= clear =================
    private void clear() {
        compoitems.setValue(null);
        compotype.setValue(null);
        quantityFil.clear();
        datePick.setValue(null);
        Lbquantity.setText("");
        LbItem.setText("");
        Lbtype.setText("");
    }

    // ================== REFRESH ==================
    @FXML
    private void refresh() {
        clear();
        loadTransactions();
    }

    private void alert(String title, String msg) {
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
        if (quantityFil.getText().trim().isEmpty() || compoitems.getSelectionModel().isEmpty()
                || compotype.getSelectionModel().isEmpty()) {

            if (quantityFil.getText().trim().isEmpty()) {
                Lbquantity.setText(currentLocale.getLanguage().equals("en")
                        ? "Enter quantity" : "ادخل الكمية");
            } else if (!quantityFil.getText().matches("\\d+")) {
                Lbquantity.setText(currentLocale.getLanguage().equals("en")
                        ? "You must enter numbers only." : "يجب عليك ادخال ارقام فقط");
            } else {
                Lbquantity.setText("");
            }

            if (compoitems.getSelectionModel().isEmpty()) {
                LbItem.setText(currentLocale.getLanguage().equals("en")
                        ? "Choose item id" : "اختر الأسم");
            } else {
                LbItem.setText("");
            }
            if (compotype.getSelectionModel().isEmpty()) {

                Lbtype.setText(currentLocale.getLanguage().equals("en")
                        ? "Choose the type of move" : "اختر نوع التحرك");
            } else {
                Lbtype.setText("");
            }
            return false;
        }

        return valid;
    }

    @FXML
    private void TransactionItem() {
        LbItem.setText("");
    }

    @FXML
    private void Transactiontype() {
        Lbtype.setText("");
    }

    @FXML
    private void Transactionquanti() {
        if (!quantityFil.getText().trim().isEmpty()) {
            Lbquantity.setText("");
        }
    }
}
