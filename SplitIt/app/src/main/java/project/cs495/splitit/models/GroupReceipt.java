package project.cs495.splitit.models;

import java.util.Map;

public class GroupReceipt extends Receipt {
    private String groupId;

    public GroupReceipt(String receiptId, String creator, String vendor, String datePurchased, float price, String groupId, Map<String, Boolean> items) {
        super(receiptId, creator, vendor, datePurchased, price, items);
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
