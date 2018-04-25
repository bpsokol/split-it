package project.cs495.splitit.models;

import com.google.firebase.database.DatabaseReference;

import java.util.Map;

public class UserReceipt extends Receipt {
    private String userId;

    public UserReceipt() {
        super();
    }

    public UserReceipt(String receiptId, String creator, String vendor, String datePurchased, float price, float subtotal, float tax, Map<String, Boolean> items, String groupId, String userId) {
        super(receiptId, creator, vendor, datePurchased, price, subtotal, tax, items, groupId);
        this.userId = userId;
    }

    @Override
    public void commitToDB(DatabaseReference mDatabase) {
        mDatabase.child("users").child(userId).child("receipts").child(receiptId).setValue(this);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
