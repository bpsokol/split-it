package project.cs495.splitit;

public class GroupOwner {
    private String managerUID;
    private String managerName;

    public GroupOwner() {

    }

    public GroupOwner(String managerUID, String managerName) {
        this.managerUID = managerUID;
        this.managerName = managerName;
    }

    public String getManagerUID() {
        return managerUID;
    }

    public String getManagerName() {
        return managerName;
    }
}
