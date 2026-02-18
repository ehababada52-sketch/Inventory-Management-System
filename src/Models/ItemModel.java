package Models;

public class ItemModel {

    private Integer id;
    private String name;
    private String type;
    private Double price;
    private Integer quantity;
    private Integer supplier_id;
    private String supplierName;
    private String status;

    public ItemModel(int id, String name, String type,
            int quantity, double price,
            int supplierId,
            String status) {

        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.supplier_id = supplierId;
        this.status = status;
    }

    public ItemModel(int id, String name, String type,
            int quantity, double price,
            int supplierId,
            String supplierName,
            String status) {

        this.id = id;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.supplier_id = supplierId;
        this.supplierName = supplierName;
        this.status = status;
    }

    public ItemModel(int id, String name, String type,
            int quantity, double price,
            String supplierName,
            String status) {

        this.id = id;
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.supplierName = supplierName;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getSupplier_id() {
        return supplier_id;
    }

    public void setSupplier_id(Integer supplier_id) {
        this.supplier_id = supplier_id;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
