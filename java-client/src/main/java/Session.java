public class Session {

    private static final Session INSTANCE = new Session();

    private String userId;
    private String userName;
    private String role; // USER or DRIVER

    private Session() {
    }

    public static Session getInstance() {
        return INSTANCE;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isLoggedIn() {
        return userId != null && !userId.isEmpty();
    }

    public void clear() {
        userId = null;
        userName = null;
        role = null;
    }
}
