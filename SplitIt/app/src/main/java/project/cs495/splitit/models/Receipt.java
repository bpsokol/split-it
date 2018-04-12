package project.cs495.splitit.models;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Receipt implements EntityInterface {
    private String receiptId;
    private String vendor;
    private String datePurchased;
    private List<String> items;

    public Receipt(String receiptId, String vendor, String datePurchased, List<String> items) {
        this.receiptId = receiptId;
        this.vendor = vendor;
        this.datePurchased = datePurchased;
        if (items != null) {
            this.items = items;
        } else {
            this.items = new ArrayList<String>();
        }
    }

    public void addItem(String item) {
        this.items.add(item);
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

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }
}
