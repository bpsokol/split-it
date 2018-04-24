package project.cs495.splitit.models;

import java.util.Map;

public class UserReceiptBuilder extends ReceiptBuilder {
    private String receiptId;
    private String creator;
    private String vendor;
    private String datePurchased;
    private float price;
    private Map<String, Boolean> items;
    private String groupId;
    private String userId;

    public UserReceiptBuilder setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public UserReceipt createReceipt() {
        return new UserReceipt(super.receiptId, super.creator, super.vendor, super.datePurchased, super.price, super.subtotal, super.tax, super.items, super.groupId, userId);
    }
}