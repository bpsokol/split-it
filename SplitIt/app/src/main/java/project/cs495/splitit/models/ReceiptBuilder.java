package project.cs495.splitit.models;

import java.util.Map;

public class ReceiptBuilder {
    protected String receiptId;
    protected String vendor;
    protected String datePurchased;
    protected float price;
    protected Map<String, Boolean> items;
    protected String creator;

    public ReceiptBuilder setReceiptId(String receiptId) {
        this.receiptId = receiptId;
        return this;
    }

    public ReceiptBuilder setVendor(String vendor) {
        this.vendor = vendor;
        return this;
    }

    public ReceiptBuilder setDatePurchased(String datePurchased) {
        this.datePurchased = datePurchased;
        return this;
    }

    public ReceiptBuilder setPrice(float price) {
        this.price = price;
        return this;
    }

    public ReceiptBuilder setItems(Map<String, Boolean> items) {
        this.items = items;
        return this;
    }

    public Receipt createReceipt() {
        return new Receipt(receiptId, creator, vendor, datePurchased, price, items);
    }

    public ReceiptBuilder setCreator(String creator) {
        this.creator = creator;
        return this;
    }
}