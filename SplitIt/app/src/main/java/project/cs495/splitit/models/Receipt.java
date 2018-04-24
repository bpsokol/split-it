package project.cs495.splitit.models;

import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class Receipt implements EntityInterface {
    private String receiptId;
    private String vendor;
    private String datePurchased;
    private String creator;
    private float price;
    private String groupId;
    private Map<String, Boolean> items;

    public Receipt() {
        this.items = new HashMap<>();
    }

    public Receipt(String receiptId, String creator, String vendor, String datePurchased, float price, Map<String, Boolean> items, String groupId) {
        this.receiptId = receiptId;
        this.creator = creator;
        this.vendor = vendor;
        this.datePurchased = datePurchased;
        this.price = price;
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
