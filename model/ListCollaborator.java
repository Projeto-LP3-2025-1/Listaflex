// START OF FILE: ListCollaborator.java
package model;

public class ListCollaborator {
    private int listId;
    private int userId;
    private String role; // Ex: "OWNER", "ADMIN", "EDITOR", "VIEWER"

    public ListCollaborator(int listId, int userId, String role) {
        this.listId = listId;
        this.userId = userId;
        this.role = role;
    }

    public int getListId() { return listId; }
    public void setListId(int listId) { this.listId = listId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
// END OF FILE: ListCollaborator.java