package project.cs495.splitit.models;

import com.google.firebase.database.DatabaseReference;

import java.util.UUID;

public class Item implements Entity {

    private String code;
    private String description;
    private double price;
    private int quantity;
    private double unitPrice;

    public Item(String code, String description, double price, int quantity, double unitPrice) {
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
        mDatabase.child("items").child(this.code).setValue(this);
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
}
