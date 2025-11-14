package ba.sum.fsre.fitness.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import ba.sum.fsre.fitness.R;
import ba.sum.fsre.fitness.api.RetrofitClient;
import ba.sum.fsre.fitness.models.Excercise;
import ba.sum.fsre.fitness.models.request.ExcerciseRequest;
import ba.sum.fsre.fitness.models.response.ExcerciseResponse;
import ba.sum.fsre.fitness.utils.AuthManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExcerciseActivity extends AppCompatActivity {

    Button addExcerciseButton;

    EditText excerciseNameEditText;

    EditText excerciseDescriptionEditText;

    AuthManager authManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excercise);

        authManager = new AuthManager(this);

        initViews();
        setupListeners();
    }

    private void initViews (){
        addExcerciseButton = findViewById(R.id.addNewExcerciseBtn);
        excerciseNameEditText = findViewById(R.id.excerciseNameTxt);
        excerciseDescriptionEditText = findViewById(R.id.excerciseDescTxt);
    }

    private void setupListeners () {
        addExcerciseButton.setOnClickListener(v -> {
            String name = excerciseNameEditText.getText().toString().trim();
            String description = excerciseDescriptionEditText.getText().toString().trim();

            android.util.Log.d("ExerciseActivity", "Name: " + name);
            android.util.Log.d("ExerciseActivity", "Description: " + description);

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter exercise name", Toast.LENGTH_SHORT).show();
                return;
            }

            ExcerciseRequest request = new ExcerciseRequest(name, description);
            String token = "Bearer " + authManager.getToken();

            android.util.Log.d("ExerciseActivity", "Token: " + token);
            android.util.Log.d("ExerciseActivity", "Starting API call...");

            RetrofitClient.getInstance().getApi().createExcercise(token, request)
                    .enqueue(new Callback<List<Excercise>>() {
                        @Override
                        public void onResponse(Call<List<Excercise>> call, Response<List<Excercise>> response) {
                            android.util.Log.d("ExerciseActivity", "Response code: " + response.code());
                            android.util.Log.d("ExerciseActivity", "Response successful: " + response.isSuccessful());

                            if (response.isSuccessful()) {
                                List<Excercise> exercises = response.body();
                                android.util.Log.d("ExerciseActivity", "Response body: " + (exercises != null ? exercises.size() : "null"));

                                if (exercises != null && !exercises.isEmpty()) {
                                    Excercise created = exercises.get(0);
                                    android.util.Log.d("ExerciseActivity", "Created exercise: " + created.getName());
                                    Toast.makeText(getApplicationContext(),
                                            "Exercise added: " + created.getName(),
                                            Toast.LENGTH_SHORT).show();

                                    excerciseNameEditText.setText("");
                                    excerciseDescriptionEditText.setText("");
                                } else {
                                    android.util.Log.e("ExerciseActivity", "Response body is empty");
                                    Toast.makeText(getApplicationContext(),
                                            "Response empty",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                try {
                                    String errorBody = response.errorBody() != null ?
                                            response.errorBody().string() : "No error body";
                                    android.util.Log.e("ExerciseActivity", "Error body: " + errorBody);
                                    android.util.Log.e("ExerciseActivity", "Error code: " + response.code());
                                    Toast.makeText(getApplicationContext(),
                                            "Error " + response.code() + ": " + errorBody,
                                            Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    android.util.Log.e("ExerciseActivity", "Error reading error body", e);
                                    Toast.makeText(getApplicationContext(),
                                            "Error: " + response.code(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Excercise>> call, Throwable t) {
                            android.util.Log.e("ExerciseActivity", "API call failed", t);
                            Toast.makeText(getApplicationContext(),
                                    "Network error: " + t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }


}