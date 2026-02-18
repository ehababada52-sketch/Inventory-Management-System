package Models;

import java.sql.Date;

public class TransactionModel {

    private int transactionId;
    private int itemId;
    private String itemName;
    private int userId;
    private String userName;
    private int quantity;
    private String movementType;
    private Date transactionDate;

    public TransactionModel(int transactionId,
            int itemId,
            String itemName,
            int userId,
            String userName,
            String movementType,
            int quantity,
            Date transactionDate) {

        this.transactionId = transactionId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.userId = userId;
        this.userName = userName;
        this.movementType = movementType;
        this.quantity = quantity;
        this.transactionDate = transactionDate;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getMovementType() {
        return movementType;
    }

    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

}
