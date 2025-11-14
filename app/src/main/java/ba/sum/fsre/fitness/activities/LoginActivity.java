package ba.sum.fsre.fitness.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import ba.sum.fsre.fitness.R;
import ba.sum.fsre.fitness.api.ApiCallback;
import ba.sum.fsre.fitness.api.RetrofitClient;
import ba.sum.fsre.fitness.models.request.LoginRequest;
import ba.sum.fsre.fitness.models.response.AuthResponse;
import ba.sum.fsre.fitness.utils.AuthManager;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginBtn, registerBtn;
    private ProgressBar progressBar;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = new AuthManager(this);

        // Ako je već prijavljen, idi na Dashboard
        if (authManager.isLoggedIn()) {
            goToDashboard();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.openRegisterBtn);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        loginBtn.setOnClickListener(v -> loginUser());
        registerBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (!validateInput(email, password)) {
            return;
        }

        setLoading(true);

        LoginRequest request = new LoginRequest(email, password);

        RetrofitClient.getInstance()
                .getApi()
                .login(request)
                .enqueue(new ApiCallback<AuthResponse>() {
                    @Override
                    public void onSuccess(AuthResponse response) {
                        setLoading(false);
                        handleLoginSuccess(response);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            emailInput.setError("Unesite email");
            return false;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Unesite lozinku");
            return false;
        }
        return true;
    }

    private void handleLoginSuccess(AuthResponse response) {
        authManager.saveToken(response.getAccessToken());
        authManager.saveEmail(response.getUser().getEmail());

        Toast.makeText(this, "Dobrodošli!", Toast.LENGTH_SHORT).show();
        goToDashboard();
    }

    private void goToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginBtn.setEnabled(!isLoading);
    }
}