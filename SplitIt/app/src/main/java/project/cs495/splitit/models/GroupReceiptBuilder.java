package project.cs495.splitit.models;

public class GroupReceiptBuilder extends ReceiptBuilder{

    @Override
    public GroupReceipt createReceipt() {
        return new GroupReceipt(receiptId, creator, vendor, datePurchased, price, subtotal, tax, groupId, items);
    }
}