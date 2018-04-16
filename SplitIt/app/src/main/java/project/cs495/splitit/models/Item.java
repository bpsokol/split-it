package project.cs495.splitit.models;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Item implements EntityInterface {
    private String itemId;
    private Map<String, Boolean> receiptIds;
    private String code;
    private String description;
    private float price;
    private int quantity;
    private float unitPrice;
    private String assignedUser;

    public Item() {
        this.receiptIds = new HashMap<>();
    }

    public Item(String itemId, String code, String description, float price, int quantity, float unitPrice) {
        this.itemId = itemId;
        this.receiptIds = new HashMap<>();
        this.code = code;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Item(String itemId, Map<String, Boolean> receiptIds, String code, String description, float price, int quantity, float unitPrice) {
        this.itemId = itemId;
        this.receiptIds = receiptIds;
        this.code = code;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public void assignTo(String member) {
        setAssignedUser(member);
    }

    public void chageToVenmo(double price) {
        return;
    }

    public void notifyOfPayment(UUID receiptCreator) {
        return;
    }

    @Override
    public void commitToDB(DatabaseReference mDatabase) {
        DatabaseReference itemsRef = mDatabase.child("items");
        itemsRef.child(this.itemId).setValue(this);
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public Map<String, Boolean> getReceiptIds() {
        return receiptIds;
    }

    public void addReceiptId(String receiptId) {
        this.receiptIds.put(receiptId, true);
    }

    public void removeReceiptId(String receiptId) {
        this.receiptIds.remove(receiptId);
    }

    public void setReceiptIds(Map<String, Boolean> receiptIds) {
        this.receiptIds = receiptIds;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(float unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setAssignedUser(String assignedUser) {
        this.assignedUser = assignedUser;
    }
    public String getAssignedUser() {
        return assignedUser;
    }
}
