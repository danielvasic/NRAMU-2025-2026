package ba.sum.fsre.fitness.models.request;

public class RegisterRequest extends BaseRequest {
    private String email;
    private String password;
    private String username; // Dodatno polje za registraciju

    public RegisterRequest(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }

    // Getteri
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getUsername() { return username; }
}