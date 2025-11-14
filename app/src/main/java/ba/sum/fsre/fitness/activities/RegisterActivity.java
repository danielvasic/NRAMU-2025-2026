package ba.sum.fsre.fitness.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import ba.sum.fsre.fitness.R;
import ba.sum.fsre.fitness.api.ApiCallback;
import ba.sum.fsre.fitness.api.RetrofitClient;
import ba.sum.fsre.fitness.models.request.RegisterRequest;
import ba.sum.fsre.fitness.models.response.AuthResponse;
import ba.sum.fsre.fitness.utils.AuthManager;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button registerBtn, backToLoginBtn;
    private ProgressBar progressBar;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authManager = new AuthManager(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        usernameInput = findViewById(R.id.usernameTxt);
        emailInput = findViewById(R.id.emailTxt);
        passwordInput = findViewById(R.id.passwordTxt);
        confirmPasswordInput = findViewById(R.id.passwordCnfTxt);
        registerBtn = findViewById(R.id.registerBtn);
        backToLoginBtn = findViewById(R.id.backToLoginBtn);
        progressBar = findViewById(R.id.registerProgressBar);
    }

    private void setupListeners() {
        registerBtn.setOnClickListener(v -> registerUser());

        backToLoginBtn.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (!validateInput(username, email, password, confirmPassword)) {
            return;
        }

        setLoading(true);

        RegisterRequest request = new RegisterRequest(email, password, username);

        RetrofitClient.getInstance()
                .getApi()
                .signup(request)
                .enqueue(new ApiCallback<AuthResponse>() {
                    @Override
                    public void onSuccess(AuthResponse response) {
                        setLoading(false);
                        handleRegisterSuccess(response);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInput(String username, String email, String password, String confirmPassword) {
        // Validacija username-a
        if (username.isEmpty()) {
            usernameInput.setError("Unesite korisničko ime");
            usernameInput.requestFocus();
            return false;
        }

        if (username.length() < 3) {
            usernameInput.setError("Korisničko ime mora imati najmanje 3 znaka");
            usernameInput.requestFocus();
            return false;
        }

        // Validacija email-a
        if (email.isEmpty()) {
            emailInput.setError("Unesite email");
            emailInput.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Unesite ispravan email");
            emailInput.requestFocus();
            return false;
        }

        // Validacija lozinke
        if (password.isEmpty()) {
            passwordInput.setError("Unesite lozinku");
            passwordInput.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordInput.setError("Lozinka mora imati najmanje 6 znakova");
            passwordInput.requestFocus();
            return false;
        }

        // Validacija potvrde lozinke
        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.setError("Potvrdite lozinku");
            confirmPasswordInput.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Lozinke se ne podudaraju");
            confirmPasswordInput.requestFocus();
            return false;
        }

        return true;
    }

    private void handleRegisterSuccess(AuthResponse response) {
        authManager.saveToken(response.getAccessToken());
        authManager.saveEmail(response.getUser().getEmail());

        Toast.makeText(this, "Registracija uspješna!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        registerBtn.setEnabled(!isLoading);
        usernameInput.setEnabled(!isLoading);
        emailInput.setEnabled(!isLoading);
        passwordInput.setEnabled(!isLoading);
        confirmPasswordInput.setEnabled(!isLoading);
    }
}