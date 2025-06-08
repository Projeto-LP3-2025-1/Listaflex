package model;

public class UserList {
    private int id;
    private int userId;
    private String listName;
    private String listType; // "KANBAN" ou "COMUM"

    public UserList(int userId, String listName, String listType) {
        this.userId = userId;
        this.listName = listName;
        this.listType = listType;
    }

    public UserList(int id, int userId, String listName, String listType) {
        this(userId, listName, listType);
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getListName() { return listName; }
    public void setListName(String listName) { this.listName = listName; }

    public String getListType() { return listType; }
    public void setListType(String listType) { this.listType = listType; }

    @Override
    public String toString() { // Útil para JList ou debugging
        return listName + " (" + listType + ")";
    }
}