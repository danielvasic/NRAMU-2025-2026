package ba.sum.fsre.fitness.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import ba.sum.fsre.fitness.R;
import ba.sum.fsre.fitness.utils.AuthManager;

public class DashboardActivity extends AppCompatActivity {

    AuthManager authManager;
    Button logoutBtn;
    Button addExcerciseBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        authManager = new AuthManager(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        logoutBtn = findViewById(R.id.logoutBtn);
        addExcerciseBtn = findViewById(R.id.addNewExcerciseBtn);
    }

    private void setupListeners() {
        logoutBtn.setOnClickListener(v -> {
            authManager.logout();
            startActivity(
                    new Intent(
                            this,
                            LoginActivity.class
                    )
            );
        });

        addExcerciseBtn.setOnClickListener(v -> {
            startActivity(
                    new Intent(
                            this,
                            ExcerciseActivity.class
                    )
            );
        });
    }
}