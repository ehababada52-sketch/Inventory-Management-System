package wareHouse;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.*;
//import javafx.scene.control.*;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import DB.DBConnection;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class LoginController implements Initializable {

    @FXML
    private Label lblError;
    @FXML
    private Label lblTitle1;
    @FXML
    private Label lblTitle2;
    @FXML
    private TextField txtUser;
    @FXML
    private PasswordField txtPass;
    @FXML
    private Button btnLogin;
    @FXML
    private Button btnLang;

    
    @FXML
private TextField txtPassVisible;

@FXML
private ImageView eyeIcon;

private boolean passwordVisible = false;

    
    
    private Locale currentLocale = new Locale("en");
    private ResourceBundle bundle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadLanguage();
    }

    private void loadLanguage() {
        bundle = ResourceBundle.getBundle("lang.lang", currentLocale);

        lblTitle1.setText(bundle.getString("login.title1"));
        lblTitle2.setText(bundle.getString("login.title2"));
        txtUser.setPromptText(bundle.getString("login.username"));
        txtPass.setPromptText(bundle.getString("login.password"));
        btnLogin.setText(bundle.getString("login.button"));
        // زرار يقلب AR <-> EN
        btnLang.setText(bundle.getString("login.language"));
    }

    @FXML
    private void changeLanguage() {
        if (currentLocale.getLanguage().equals("en")) {
            currentLocale = new Locale("ar");
        } else {
            currentLocale = new Locale("en");
        }
        loadLanguage();
    }

    @FXML
private void togglePassword() {

    if (passwordVisible) {
        // اخفي الباسورد
        txtPass.setText(txtPassVisible.getText());
        txtPass.setVisible(true);
        txtPass.setManaged(true);

        txtPassVisible.setVisible(false);
        txtPassVisible.setManaged(false);

    } else {
        // اظهر الباسورد
        txtPassVisible.setText(txtPass.getText());
        txtPassVisible.setVisible(true);
        txtPassVisible.setManaged(true);

        txtPass.setVisible(false);
        txtPass.setManaged(false);
    }

    passwordVisible = !passwordVisible;
}

    
    @FXML
    public void loginAction(ActionEvent event) {

        String username = txtUser.getText().trim();
//        String password = txtPass.getText().trim();

        String password = passwordVisible
        ? txtPassVisible.getText().trim()
        : txtPass.getText().trim();

        
        Button login_btn = (Button) event.getSource();
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), login_btn);
        tt.setByY(-10);
        tt.setAutoReverse(true);
        tt.setCycleCount(2);
        tt.play();

        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText(currentLocale.getLanguage().equals("en")
                    ? "Please fill all fields" : "من فضلك املأ كل الحقول");
            return;
        }

//      -------------------------------------------------------------        
        try (Connection conn = DBConnection.getConnection()) {

            String sql = """
                SELECT u.username, r.role_name ,u.user_id
                FROM Users u
                JOIN Roles r ON u.role_id = r.role_id
                WHERE u.username = ? AND u.password = ?
            """;

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String role = rs.getString("role_name");
                int user_id = rs.getInt("user_id");

//                // افتح dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Home.fxml"));
                Parent root = loader.load();

                HomeController homeController = loader.getController();
                homeController.initData(username, role, currentLocale, user_id);

                FadeTransition ft = new FadeTransition(Duration.millis(300), root);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();

                Scene homeScene = new Scene(root);
                Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
                window.setScene(homeScene);
                window.setTitle("Warehouse");
                window.show();

            } else {
                lblError.setText(currentLocale.getLanguage().equals("en")
                        ? "Invalid username or password" : "اسم المستخدم أو كلمة المرور غير صحيحة");
            }

        } catch (Exception e) {

            lblError.setText(currentLocale.getLanguage().equals("en")
                    ? "Error connecting to database" : "خطأ في الاتصال بقاعدة البياناتة");
            System.out.println(e);
        }

    }

    @FXML
    private void loginError() {
        if (!txtUser.getText().trim().isEmpty() && !txtPass.getText().trim().isEmpty()) {
            lblError.setText("");
        }
    }
}
