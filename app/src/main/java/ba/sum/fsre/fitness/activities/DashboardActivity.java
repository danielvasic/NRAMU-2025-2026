package ba.sum.fsre.fitness.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ba.sum.fsre.fitness.R;
import ba.sum.fsre.fitness.api.RetrofitClient;
import ba.sum.fsre.fitness.models.Excercise;
import ba.sum.fsre.fitness.utils.AuthManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    AuthManager authManager;
    Button logoutBtn;
    Button addExcerciseBtn;

    ProgressBar workoutProgress;

    ListView workoutListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        authManager = new AuthManager(this);

        // initialize UI first so buttons are ready even if network is slow
        initViews();
        setupListeners();

        this.workoutProgress.setVisibility(View.VISIBLE);

        // Use Bearer <token> for Authorization (Supabase expects Bearer token)
        String rawToken = authManager.getToken();
        String token = rawToken != null ? "Bearer " + rawToken : null;

        Log.d("DashboardActivity", "Using token: " + (rawToken != null ? "[REDACTED]" : "null"));

        if (token == null) {
            Toast.makeText(DashboardActivity.this, "Niste prijavljeni. Prijavite se ponovo.", Toast.LENGTH_LONG).show();
            this.workoutProgress.setVisibility(View.INVISIBLE);
            return;
        }

        RetrofitClient
            .getInstance()
            .getApi()
            .getAll(token).enqueue(new Callback<List<Excercise>>() {
                    @Override
                    public void onResponse(Call<List<Excercise>> call, Response<List<Excercise>> response) {
                        Log.d("DashboardActivity", "API onResponse code=" + response.code());

                        if (!response.isSuccessful()) {
                            String err = "Server error: " + response.code();
                            try {
                                if (response.errorBody() != null) err += " - " + response.errorBody().string();
                            } catch (Exception ex) {
                                Log.e("DashboardActivity", "Error reading errorBody", ex);
                            }
                            Toast.makeText(DashboardActivity.this, err, Toast.LENGTH_LONG).show();
                            workoutProgress.setVisibility(View.INVISIBLE);
                            return;
                        }

                        List<Excercise> excerciseList = response.body();
                        if (excerciseList == null) {
                            Toast.makeText(DashboardActivity.this, "Nema podataka (response body je null)", Toast.LENGTH_LONG).show();
                            workoutProgress.setVisibility(View.INVISIBLE);
                            return;
                        }

                        List<HashMap<String, String>> data = new ArrayList<>();

                        for (Excercise e : excerciseList) {
                            HashMap<String, String> item = new HashMap<>();
                            item.put("name", e.getName());
                            item.put("description", e.getDescription());
                            item.put("image", e.getImageUrl());
                            data.add(item);
                        }

                        String [] from = {"name", "description", "image"};
                        int [] to = {R.id.textView2, R.id.textView3, R.id.itemImage};

                        SimpleAdapter adapter = new SimpleAdapter(
                                DashboardActivity.this,
                                data, R.layout.listview_item,
                                from, to
                        );

                        adapter.setViewBinder((view, data1, text) -> {
                            if (view.getId() == R.id.itemImage) {
                                String url = (String) data1;
                                Picasso.get().load(url).into((ImageView) view);
                                return true;
                            }
                            return false;
                        });

                        workoutListView.setAdapter(adapter);

                        Toast.makeText(
                                DashboardActivity.this,
                                "Učitano: " + excerciseList.size() + " vježbi",
                                Toast.LENGTH_LONG
                        ).show();
                        workoutProgress.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onFailure(Call<List<Excercise>> call, Throwable t) {
                        Log.e("DashboardActivity", "API call failed", t);
                        Toast.makeText(
                                DashboardActivity.this,
                                "Greška pri učitavanju vježbi: " + t.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                        workoutProgress.setVisibility(View.INVISIBLE);
                    }
            });
    }

    private void initViews() {
        logoutBtn = findViewById(R.id.logoutBtn);
        addExcerciseBtn = findViewById(R.id.addNewExcerciseBtn);
        workoutProgress = findViewById(R.id.workoutProgress);
        workoutListView = findViewById(R.id.workoutListView);
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