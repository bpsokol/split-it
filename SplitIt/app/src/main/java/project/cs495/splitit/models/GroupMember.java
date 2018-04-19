package project.cs495.splitit.models;

public class GroupMember {
    private User member;
    private String groupId;

    public GroupMember(User member, String groupId) {
        this.member = member;
        this.groupId = groupId;
    }

    public User getMember() {
        return member;
    }

    public String getGroupId() {
        return groupId;
    }
}
