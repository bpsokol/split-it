package project.cs495.splitit.models;


public class Bill {

    private String name = "";
    private String email = "";
    private String amount = "";
    private String uid = "";

    public Bill(String name, String email, String amount, String uid) {
        this.name = name;
        this.email = email;
        this.amount = amount;
        this.uid = uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAmount() {
        return amount;
    }

    public void setUid(String uid) { this.uid = uid; }

    public String getUid(String uid) { return uid; }

}
