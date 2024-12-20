package application;

public abstract class BaseController {
    protected int userId;

    public void setUserId(int userId) {
        this.userId = userId;
        System.out.println("User ID set in " + getClass().getSimpleName() + ": " + userId);
    }
}
