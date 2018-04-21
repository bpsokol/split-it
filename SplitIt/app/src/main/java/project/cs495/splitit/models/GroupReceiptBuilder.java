package project.cs495.splitit.models;

import java.util.Map;

public class GroupReceiptBuilder extends ReceiptBuilder{
    private String groupId;

    public GroupReceiptBuilder setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    @Override
    public GroupReceiptBuilder setReceiptId(String receiptId) {
        super.setReceiptId(receiptId);
        return this;
    }

    @Override
    public GroupReceiptBuilder setCreator(String creator) {
        super.setCreator(creator);
        return this;
    }

    @Override
    public GroupReceiptBuilder setVendor(String vendor) {
        super.setVendor(vendor);
        return this;
    }

    @Override
    public GroupReceiptBuilder setDatePurchased(String datePurchased) {
        super.setDatePurchased(datePurchased);
        return this;
    }

    @Override
    public GroupReceiptBuilder setPrice(float price) {
        super.setPrice(price);
        return this;
    }

    @Override
    public GroupReceiptBuilder setItems(Map<String, Boolean> items) {
        super.setItems(items);
        return this;
    }

    public GroupReceipt createGroupReceipt() {
        return new GroupReceipt(receiptId, creator, vendor, datePurchased, price, groupId, items);
    }
}