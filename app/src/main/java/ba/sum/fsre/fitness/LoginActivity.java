package ba.sum.fsre.fitness;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginBtn = findViewById(R.id.loginBtn);
        progressBar = findViewById(R.id.progressBar);

        loginBtn.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInput.setError("Unesite email");
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Unesite lozinku");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        loginBtn.setEnabled(false);

        LoginRequest request = new LoginRequest(email, password);

        RetrofitClient.getInstance().getApi().login(request)
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        loginBtn.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse loginResponse = response.body();

                            // Spremi token
                            SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                            prefs.edit()
                                    .putString("token", loginResponse.getAccessToken())
                                    .putString("email", loginResponse.getUser().getEmail())
                                    .apply();

                            Toast.makeText(LoginActivity.this, "Dobrodošli!", Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(LoginActivity.this, Dashboard.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Pogrešan email ili lozinka", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        loginBtn.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}