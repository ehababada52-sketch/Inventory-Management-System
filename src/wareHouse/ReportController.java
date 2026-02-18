package wareHouse;

import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
//// Excel
   import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//// PDF
   import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import DB.DBConnection;

public class ReportController {

    @FXML
    private Button back_btn;

    @FXML
    private Button report_btn;

    @FXML
    private Button exportExc;

    @FXML
    private Button exportPdf;

    @FXML
    private TextField txtSearch;

    @FXML
    private Label lbcombo;

    @FXML
    private Label report;

    private String currentUser, currentRole;

    @FXML
    private ComboBox<String> myComboBox;
    @FXML
    private TableView<ObservableList<String>> table;

    private ObservableList<ObservableList<String>> masterData
            = FXCollections.observableArrayList();

    public class ReportType {

        public static final String CURRENT_STOCK = "CURRENT_STOCK";
        public static final String MONTHLY_MOVEMENT = "MONTHLY_MOVEMENT";
        public static final String LOW_STOCK = "LOW_STOCK";
        public static final String TRANSACTIONS = "TRANSACTIONS";
    }

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

        exportExc.setText(bundle.getString("report.exportExc"));
        exportPdf.setText(bundle.getString("report.exportPdf"));
        report_btn.setText(bundle.getString("report.report_btn"));
        myComboBox.setPromptText(bundle.getString("report.myComboBox"));
        report.setText(bundle.getString("report.report"));
        report_btn.setText(bundle.getString("report.generate"));
        exportExc.setText(bundle.getString("report.exportExcel"));
        exportPdf.setText(bundle.getString("report.exportPdf"));
        myComboBox.setPromptText(bundle.getString("report.choose"));
        txtSearch.setPromptText(bundle.getString("report.txtSearch"));
    }

    @FXML
    public void initialize() {
        table.setVisible(false);
        exportExc.setDisable(true);
        exportPdf.setDisable(true);

        myComboBox.getItems().addAll(
                ReportType.TRANSACTIONS,
                ReportType.CURRENT_STOCK,
                ReportType.MONTHLY_MOVEMENT,
                ReportType.LOW_STOCK
        );

        myComboBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(bundle.getString("report." + item));
                }
            }
        });

        myComboBox.setButtonCell(myComboBox.getCellFactory().call(null));

    }

    // -----------------------------------------------------------
    // MAIN REPORT LOADER
    // -----------------------------------------------------------
    @FXML
    void handleGenerate(ActionEvent event) {

        String selected = myComboBox.getValue();

        if (selected == null) {
            lbcombo.setText(currentLocale.getLanguage().equals("en")
                    ? "Choose report!" : "اختر التقرير!");

            return;
        } else {
            lbcombo.setText("");
        }

        table.setVisible(true);
        table.getItems().clear();
        table.getColumns().clear();

        switch (selected) {

            case ReportType.CURRENT_STOCK:
                loadReport(
                        new String[]{
                            bundle.getString("col.itemId"),
                            bundle.getString("col.itemName"),
                            bundle.getString("col.price"),
                            bundle.getString("col.qty"),
                            bundle.getString("col.lastDate")
                        },
                        "SELECT * FROM Current_Stock"
                );
                break;

            case ReportType.MONTHLY_MOVEMENT:
                loadReport(
                        new String[]{
                            bundle.getString("col.itemId"),
                            bundle.getString("col.itemName"),
                            bundle.getString("col.in"),
                            bundle.getString("col.out"),
                            bundle.getString("col.net")
                        },
                        "SELECT * FROM Monthly_Stock_Movement_Report"
                );
                break;

            case ReportType.LOW_STOCK:
                loadReport(
                        new String[]{
                            bundle.getString("col.itemId"),
                            bundle.getString("col.itemName"),
                            bundle.getString("col.qty"),
                            bundle.getString("col.supplier"),
                            bundle.getString("col.phone")
                        },
                        "SELECT * FROM low_stock_report"
                );
                break;

            case ReportType.TRANSACTIONS:
                loadReport(
                        new String[]{
                            bundle.getString("col.itemName"),
                            bundle.getString("col.type"),
                            bundle.getString("col.qty"),
                            bundle.getString("col.date"),
                            bundle.getString("col.user")
                        },
                        "SELECT * FROM Reports"
                );
                break;
        }
        lbcombo.setText("");
    }

    // -----------------------------------------------------------
    // LOAD REPORT INTO TABLE
    // -----------------------------------------------------------
    private void loadReport(String[] columns, String sql) {

        try (Connection conn = DBConnection.getConnection(); ResultSet rs = conn.createStatement().executeQuery(sql)) {

//            // Create columns
//            for (int i = 0; i < columns.length; i++) {
//                final int colIndex = i;
//                TableColumn<ObservableList<String>, String> col =
//                        new TableColumn<>(columns[i]);
//                col.setCellValueFactory(
//                        param -> new ReadOnlyStringWrapper(param.getValue().get(colIndex))
//                );
//                table.getColumns().add(col);
//            }
            // Create columns
            for (int i = 0; i < columns.length; i++) {
                final int colIndex = i;
                TableColumn<ObservableList<String>, String> col
                        = new TableColumn<>(columns[i]);
                col.setCellValueFactory(
                        param -> {

                            ObservableList<String> row = param.getValue();
                            if (row == null || row.size() <= colIndex) {
                                return new ReadOnlyStringWrapper("");
                            }
                            String val = row.get(colIndex);
                            return new ReadOnlyStringWrapper(val == null ? "" : val);
                        }
                );

                col.setPrefWidth(150);
                table.getColumns().add(col);
            }
            // Fill data
            ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= columns.length; i++) {
                    String v = rs.getString(i);
                    row.add(v == null ? "" : v);
                }
                data.add(row);
            }

            table.setItems(data);

            masterData.clear();
            masterData.addAll(data);

        } catch (Exception e) {

            new Alert(Alert.AlertType.ERROR, currentLocale.getLanguage().equals("en")
                    ? "An error occurred while loading the report." : "حدث خطأ أثناء تحميل التقرير").show();
        }
        lbcombo.setText("");

        exportExc.setDisable(false);
        exportPdf.setDisable(false);

    }

    // -----------------------------------------------------------
    // EXPORT TO EXCEL
    // -----------------------------------------------------------
    @FXML
    private void exportExcel(ActionEvent event) {

        if (!table.isVisible() || table.getItems().isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, currentLocale.getLanguage().equals("en")
                    ? "No data available for export" : "لا يوجد بيانات للتصدير").show();
            return;
        }
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle(currentLocale.getLanguage().equals("en")
                    ? "Save as Excel" : "حفظ كملف Excel");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel File (*.xlsx)", "*.xlsx")
            );
            File file = chooser.showSaveDialog(null);
            if (file == null) {
                return;
            }

            Workbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet("Report");

            // Header
            Row header = sheet.createRow(0);
            for (int i = 0; i < table.getColumns().size(); i++) {
                header.createCell(i).setCellValue(table.getColumns().get(i).getText());
            }
            // Rows
            for (int r = 0; r < table.getItems().size(); r++) {
                Row row = sheet.createRow(r + 1);
                ObservableList<String> rowData = table.getItems().get(r);
                for (int c = 0; c < table.getColumns().size(); c++) {
                    String cellVal = "";
                    if (rowData != null && rowData.size() > c && rowData.get(c) != null) {
                        cellVal = rowData.get(c);
                    }
                    row.createCell(c).setCellValue(cellVal);
                }
            }
            FileOutputStream out = new FileOutputStream(file);
            wb.write(out);
            out.close();
            wb.close();

            new Alert(Alert.AlertType.INFORMATION, currentLocale.getLanguage().equals("en")
                    ? "The report has been successfully exported to Excel!" : "تم تصدير التقرير إلى Excel بنجاح!").show();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, currentLocale.getLanguage().equals("en")
                    ? "Error while exporting to Excel" : "خطأ أثناء تصدير Excel").show();
        }
    }

    // -----------------------------------------------------------
    // EXPORT TO PDF
    // -----------------------------------------------------------    
    @FXML
    private void exportPDF(ActionEvent event) {

        if (!table.isVisible() || table.getItems().isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, currentLocale.getLanguage().equals("en")
                    ? "No data available for export" : "لا يوجد بيانات للتصدير").show();
            return;
        }

        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle(currentLocale.getLanguage().equals("en")
                    ? "Save as PDF" : "حفظ كملف PDF");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF File (*.pdf)", "*.pdf")
            );

            File file = chooser.showSaveDialog(null);
            if (file == null) {
                return;
            }

            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();

            PdfPTable pdfTable = new PdfPTable(table.getColumns().size());
            pdfTable.setWidthPercentage(100);

            // Header
            for (TableColumn<?, ?> col : table.getColumns()) {
                PdfPCell header = new PdfPCell(new Phrase(col.getText()));
                header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                pdfTable.addCell(header);
            }

            // Data
            for (ObservableList<String> row : table.getItems()) {
                for (int i = 0; i < table.getColumns().size(); i++) {
                    String cell = (row != null && row.size() > i && row.get(i) != null)
                            ? row.get(i)
                            : "";
                    pdfTable.addCell(new Phrase(cell));
                }
            }

            doc.add(pdfTable);
            doc.close();

            new Alert(Alert.AlertType.INFORMATION, currentLocale.getLanguage().equals("en")
                    ? "The report has been successfully exported to PDF!" : "تم تصدير التقرير إلى PDF بنجاح!").show();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, currentLocale.getLanguage().equals("en")
                    ? "Error while exporting to PDF" : "خطأ أثناء تصدير PDF").show();
        }
    }

    // -----------------------------------------------------------
    // GO BACK
    // -----------------------------------------------------------
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
// -----------------------------------------------------------
//    Search
// -----------------------------------------------------------

    @FXML
    private void Search() {

        String keyword = txtSearch.getText().toLowerCase().trim();

        if (keyword.isEmpty()) {
            table.setItems(masterData);
            return;
        }

        ObservableList<ObservableList<String>> filtered
                = FXCollections.observableArrayList();

        for (ObservableList<String> row : masterData) {
            for (String cell : row) {
                if (cell != null && cell.toLowerCase().contains(keyword)) {
                    filtered.add(row);
                    break;
                }
            }
        }

        table.setItems(filtered);
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.show();
    }

    @FXML
    private void ReportType() {
        lbcombo.setText("");
    }

}
