package project.cs495.splitit;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import project.cs495.splitit.models.Bill;
import project.cs495.splitit.models.Item;
import project.cs495.splitit.models.Receipt;
import project.cs495.splitit.models.User;

public class BillHandler {
    public BillHandler() {
    }

    static void syncBills() {
        Utils.getDatabaseReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    userSnapshot.child("bills").getRef().removeValue();
                }
                parseReceiptsForBillAmounts();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    static void parseReceiptsForBillAmounts() {
        Utils.getDatabaseReference().child("receipts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot receiptSnapshot : dataSnapshot.getChildren()) {
                    Receipt receipt = receiptSnapshot.getValue(Receipt.class);
                    adjustBillsForSingleReceipt(receipt);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    static void adjustBillsForSingleReceipt(Receipt receipt) {
        final String creatorId = receipt.getCreator();
        float receiptSubtotal = receipt.getSubtotal();
        if (receiptSubtotal == 0) {
            receiptSubtotal = receipt.getPrice();
        }
        final float receiptTax = receipt.getTax();
        final float finalReceiptSubtotal = receiptSubtotal;
        Utils.getDatabaseReference().child("items").orderByChild("receiptIds/" + receipt.getReceiptId()).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Float> assigneeDebtMap = new HashMap<>();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    Item item = itemSnapshot.getValue(Item.class);
                    String assignee = item.getAssignedUser();
                    if (assignee != null && !creatorId.equals(assignee)) {
                        float portionOfSubtotal = item.getPrice() / finalReceiptSubtotal;
                        float itemTax = portionOfSubtotal * receiptTax;
                        float itemTotalCost = itemTax + item.getPrice();
                        if (assigneeDebtMap.containsKey(assignee)) {
                            float currentDebt = assigneeDebtMap.get(assignee);
                            assigneeDebtMap.put(assignee, currentDebt + itemTotalCost);
                        } else {
                            assigneeDebtMap.put(assignee, itemTotalCost);
                        }
                    }
                }
                for (Map.Entry<String, Float> assigneeDebtEntry : assigneeDebtMap.entrySet()) {
                    String assignee = assigneeDebtEntry.getKey();
                    float itemTotalCost = assigneeDebtEntry.getValue();
                    adjustCreatorAssigneeBillByAmount(creatorId, assignee, itemTotalCost);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    static void adjustCreatorAssigneeBillByAmount(String creatorId, String assignee, float itemTotalCost) {
        adjustUserBillByAmount(creatorId, assignee, -1 * itemTotalCost);
        adjustUserBillByAmount(assignee, creatorId, itemTotalCost);
    }

    static void adjustUserBillByAmount(final String userId, final String billTargetUserId, final float itemTotalCost) {
        Utils.getDatabaseReference().child("users").child(userId).child("bills").orderByChild("uid").equalTo(billTargetUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Bill bill;
                Iterator<DataSnapshot> dataSnapshotIterator = dataSnapshot.getChildren().iterator();
                if (dataSnapshotIterator.hasNext()) {
                    DataSnapshot billSnapshot = dataSnapshotIterator.next();
                    bill = billSnapshot.getValue(Bill.class);
                    Float billCurrentAmount = bill.getAmountAsFloat();
                    bill.setAmountFromFloat(billCurrentAmount + itemTotalCost);
                    billSnapshot.getRef().child("amount").setValue(bill.getAmount());
                } else {
                    bill = new Bill();
                    bill.setUid(billTargetUserId);
                    bill.setAmountFromFloat(itemTotalCost);
                    String billKey = Utils.getDatabaseReference().child("users").child(userId).child("bills").push().getKey();
                    Utils.getDatabaseReference().child("users").child(userId).child("bills").child(billKey).setValue(bill);
                    saveTargetUserDetails(userId, billTargetUserId, itemTotalCost, billKey);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    static void saveTargetUserDetails(final String userId, final String billTargetUserId, final float itemTotalCost, final String billKey) {
        Utils.getDatabaseReference().child("users").child(billTargetUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User billTargetUser = dataSnapshot.getValue(User.class);
                DatabaseReference databaseReference = Utils.getDatabaseReference().child("users").child(userId).child("bills").child(billKey);
                databaseReference.child("name").setValue(billTargetUser.getName());
                databaseReference.child("email").setValue(billTargetUser.getEmail());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}