package project.cs495.splitit;


public class Bill {

    private String name = "";
    private String email = "";
    private String amount = "";

    public Bill(String name, String email, String amount) {
        this.name = name;
        this.email = email;
        this.amount = amount;
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

}
