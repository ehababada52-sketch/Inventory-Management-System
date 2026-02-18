package wareHouse;

import DB.DBConnection;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.sql.*;

public class HomeController {

    @FXML
    Label welcome_label;

    @FXML
    private Label t_home;

    @FXML
    private Button btnItems, btnSuppliers, btnTransactions,
            btnReports, btnUsers, btnLogout;

    private String currentUser;
    private String currentRole;

    private Locale currentLocale = new Locale("");
    private ResourceBundle bundle;

    private int currentUserId;
    
    @FXML private Label lblItemsCount;
@FXML private Label lblSuppliersCount;
@FXML private Label lblUsersCount;
@FXML private Label lblTransactionsCount;
@FXML private Label lblTotalStockValue;


    public void initData(String user, String role, Locale currentLocale, int user_id) {
        this.currentUser = user;
        this.currentRole = role;
        this.currentLocale = currentLocale;
        this.currentUserId = user_id;
        loadLanguage();

        welcome_label.setText(currentLocale.getLanguage().equals("en")
                ? "Welcome, " + user + " (" + role + ")" : "مرحباً، " + user + " (" + role + ")");
        applyRolePermissions();
    }

    private void loadLanguage() {
        bundle = ResourceBundle.getBundle("lang.lang", currentLocale);

        t_home.setText(bundle.getString("dashboard.title"));
        btnItems.setText(bundle.getString("dashboard.items"));
        btnSuppliers.setText(bundle.getString("dashboard.suppliers"));
        btnTransactions.setText(bundle.getString("dashboard.transaction"));
        btnReports.setText(bundle.getString("dashboard.Reports"));
        btnUsers.setText(bundle.getString("dashboard.users"));
        btnLogout.setText(bundle.getString("dashboard.logout"));
    }

@FXML
public void initialize() {
    lblItemsCount.setText(String.valueOf(getCount("Items")));
    lblSuppliersCount.setText(String.valueOf(getCount("Suppliers")));
    lblUsersCount.setText(String.valueOf(getCount("Users")));
    lblTransactionsCount.setText(String.valueOf(getCount("Transactions")));
    lblTotalStockValue.setText(String.valueOf(getTotalStockValue()));
}

    
    private int getCount(String tableName) {
    String sql = "SELECT COUNT(*) FROM " + tableName;
    try (Connection con = DBConnection.getConnection();
         ResultSet rs = con.createStatement().executeQuery(sql)) {

        if (rs.next()) return rs.getInt(1);

    } catch (Exception e) {
        e.printStackTrace();
    }
    return 0;
}
    private int getTotalStockValue() {
    String sql = """
                 SELECT
                     SUM(quantity * Price_per_one) AS TOTAL_STOCK_VALUE
                 FROM Items""";
    try (Connection con = DBConnection.getConnection();
         ResultSet rs = con.createStatement().executeQuery(sql)) {

        if (rs.next()) return rs.getInt(1);

    } catch (Exception e) {
        e.printStackTrace();
    }
    return 0;
}

    private void applyRolePermissions() {

        if (currentRole == null) {
            System.out.println("⚠ role is null!");
            return;
        }

        switch (currentRole) {

            case "Admin":
                // كل حاجة متاحة
                break;

            case "Inventory Manager":
                btnUsers.setDisable(true);
                btnSuppliers.setDisable(true);
                break;

            case "purchasing manager":
                btnUsers.setDisable(true);
                btnTransactions.setDisable(true);
                btnReports.setDisable(true);
                break;

            case "Data Entry":
                btnUsers.setDisable(true);
                btnSuppliers.setDisable(true);
                btnReports.setDisable(true);
                break;

            case "Viewer":
                // يشوف التقارير فقط
                btnUsers.setDisable(true);
                btnItems.setDisable(true);
                btnSuppliers.setDisable(true);
                btnTransactions.setDisable(true);
                break;

            default:
                
                btnUsers.setDisable(true);
                btnItems.setDisable(true);
                btnSuppliers.setDisable(true);
                btnTransactions.setDisable(true);
                btnReports.setDisable(true);
                break;
        }
    }

    
    @FXML
    private void loadPage(ActionEvent event) {
        Object src = event.getSource();
        String fxml = "";
        if (src == btnItems) {
            fxml = "items.fxml";
        } else if (src == btnSuppliers) {
            fxml = "Supplier.fxml";
        } else if (src == btnTransactions) {
            fxml = "Transaction.fxml";
        } else if (src == btnReports) {
            fxml = "Report.fxml";
        } else if (src == btnUsers) {
            fxml = "User.fxml";
        }

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            
            try {
                Object controller = loader.getController();
                controller.getClass()
                        .getMethod("initData", String.class, String.class, Locale.class, int.class)
                        .invoke(controller, currentUser, currentRole, currentLocale, currentUserId);

            } catch (Exception ignored) {
            }

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

    @FXML
    private void logout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            FadeTransition ft = new FadeTransition(Duration.millis(250), root);
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
}
