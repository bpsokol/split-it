package project.cs495.splitit.models;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Item implements EntityInterface {
    private String itemId;
    private List<String> receiptIds;
    private String code;
    private String description;
    private float price;
    private int quantity;
    private float unitPrice;

    public Item(String itemId, String code, String description, float price, int quantity, float unitPrice) {
        this.itemId = itemId;
        this.receiptIds = new ArrayList<String>();
        this.code = code;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Item(String itemId, List<String> receiptIds, String code, String description, float price, int quantity, float unitPrice) {
        this.itemId = itemId;
        this.receiptIds = receiptIds;
        this.code = code;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public void assignTo(UUID member) {
        return;
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
        itemsRef.child(this.itemId.toString()).setValue(this);
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public List<String> getReceiptIds() {
        return receiptIds;
    }

    public void addReceiptId(String receiptId) {
        this.receiptIds.add(receiptId);
    }

    public void removeReceiptId(String receiptId) {
        this.receiptIds.remove(receiptId);
    }

    public void setReceiptIds(List<String> receiptIds) {
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
}
