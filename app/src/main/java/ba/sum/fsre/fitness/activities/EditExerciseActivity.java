package ba.sum.fsre.fitness.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import ba.sum.fsre.fitness.R;
import ba.sum.fsre.fitness.models.Excercise;
import ba.sum.fsre.fitness.repository.ExerciseRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditExerciseActivity extends AppCompatActivity {
    private EditText nameInput, descInput;
    private Button saveBtn, deleteBtn;
    private ExerciseRepository repository;

    private String exerciseId;
    private String currentImageUrl;
    private String currentExerciseName; // Pamtimo ime za notifikaciju

    // Metoda za slanje notifikacije
    private void sendDeletionNotification(String name) {
        String CHANNEL_ID = "fitness_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Obavijesti o vježbama",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Obavijesti o brisanju i ažuriranju vježbi");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Vježba obrisana")
                .setContentText("Vježba '" + name + "' je trajno uklonjena.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Provjera dozvole za Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Nema dozvole za notifikacije", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            notificationManager.notify(1, builder.build());
        } catch (SecurityException e) {
            Toast.makeText(this, "Greška prilikom slanja notifikacije", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_exercise);

        // 1. Povezivanje UI elemenata (XML -> Java)
        nameInput = findViewById(R.id.editName);
        descInput = findViewById(R.id.editDesc);
        saveBtn = findViewById(R.id.btnSave);
        deleteBtn = findViewById(R.id.btnDelete);

        // Inicijalizacija repozitorija
        repository = new ExerciseRepository(this);

        // 2. Prijem podataka iz prošle aktivnosti
        if (getIntent().getExtras() != null) {
            // Preuzimamo ključne podatke (ID je najvažniji!)
            exerciseId = getIntent().getStringExtra("id");
            currentImageUrl = getIntent().getStringExtra("image");
            currentExerciseName = getIntent().getStringExtra("name"); // Spremamo ime za notifikaciju

            // Popunjavamo polja starim podacima
            nameInput.setText(currentExerciseName);
            descInput.setText(getIntent().getStringExtra("desc"));
        }

        // 3. LOGIKA ZA SPREMANJE (UPDATE)
        saveBtn.setOnClickListener(v -> {
            String newName = nameInput.getText().toString().trim();
            String newDesc = descInput.getText().toString().trim();

            // Validacija
            if (newName.isEmpty()) {
                Toast.makeText(EditExerciseActivity.this, "Unesite naziv vježbe", Toast.LENGTH_SHORT).show();
                return;
            }

            repository.update(exerciseId, newName, newDesc, currentImageUrl).enqueue(new Callback<List<Excercise>>() {
                @Override
                public void onResponse(Call<List<Excercise>> call, Response<List<Excercise>> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(EditExerciseActivity.this, "Ažurirano!", Toast.LENGTH_SHORT).show();
                        // finish() zatvara ekran i vraća nas na Dashboard
                        finish();
                    } else {
                        Toast.makeText(EditExerciseActivity.this, "Greška pri ažuriranju: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Excercise>> call, Throwable t) {
                    Toast.makeText(EditExerciseActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // 4. LOGIKA ZA BRISANJE (DELETE)
        deleteBtn.setOnClickListener(v -> {
            repository.delete(exerciseId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        // Šaljemo notifikaciju prije izlaska
                        sendDeletionNotification(currentExerciseName);
                        Toast.makeText(EditExerciseActivity.this, "Vježba obrisana!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditExerciseActivity.this, "Greška pri brisanju: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(EditExerciseActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    } // Kraj metode onCreate
}

