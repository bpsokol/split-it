package project.cs495.splitit.models;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class Receipt implements EntityInterface {
    protected String receiptId;
    private String vendor;
    private String datePurchased;
    private String creator;
    private float price;
    private float subtotal;
    private float tax;
    private String groupId;
    private Map<String, Boolean> items;

    public Receipt() {
        this.items = new HashMap<>();
    }

    public Receipt(String receiptId, String creator, String vendor, String datePurchased, float price, float subtotal, float tax, Map<String, Boolean> items, String groupId) {
        this.receiptId = receiptId;
        this.creator = creator;
        this.vendor = vendor;
        this.datePurchased = datePurchased;
        this.price = price;
        this.subtotal = subtotal;
        this.tax = tax;
        this.groupId = groupId;
        if (items != null) {
            this.items = items;
        } else {
            this.items = new HashMap<>();
        }
    }

    public void addItem(String item) {
        this.items.put(item, true);
    }

    public void removeItem(String item) {
        this.items.remove(item);
    }

    @Override
    public void commitToDB(DatabaseReference mDatabase) {
        mDatabase.child("receipts").child(this.receiptId).setValue(this);
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getDatePurchased() {
        return datePurchased;
    }

    public void setDatePurchased(String datePurchased) {
        this.datePurchased = datePurchased;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(float subtotal) {
        this.subtotal = subtotal;
    }

    public float getTax() {
        return tax;
    }

    public void setTax(float tax) {
        this.tax = tax;
    }

    public Map<String, Boolean> getItems() {
        return items;
    }

    public void setItems(Map<String, Boolean> items) {
        this.items = items;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
