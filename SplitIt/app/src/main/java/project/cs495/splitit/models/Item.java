package project.cs495.splitit.models;

import com.google.firebase.database.DatabaseReference;

import java.util.UUID;

public class Item implements Entity {
    private UUID itemId;
    private String code;
    private String description;
    private float price;
    private int quantity;
    private float unitPrice;

    public Item(String code, String description, float price, int quantity, float unitPrice) {
        this.itemId = UUID.randomUUID();
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

    public UUID getItemId() {
        return itemId;
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
