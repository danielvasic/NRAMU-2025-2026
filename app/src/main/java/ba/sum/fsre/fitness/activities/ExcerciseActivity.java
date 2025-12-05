package ba.sum.fsre.fitness.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import ba.sum.fsre.fitness.R;
import ba.sum.fsre.fitness.api.RetrofitClient;
import ba.sum.fsre.fitness.models.Excercise;
import ba.sum.fsre.fitness.models.request.ExcerciseRequest;
import ba.sum.fsre.fitness.models.response.ExcerciseResponse;
import ba.sum.fsre.fitness.repository.ExerciseRepository;
import ba.sum.fsre.fitness.utils.AuthManager;
import ba.sum.fsre.fitness.utils.Constants;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExcerciseActivity extends AppCompatActivity {

    Button addExcerciseButton;

    EditText excerciseNameEditText;

    EditText excerciseDescriptionEditText;

    AuthManager authManager;

    private Uri selectedImageUri;
    private ImageButton imagePreview;

    private ExerciseRepository repository;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excercise);

        authManager = new AuthManager(this);
        repository = new ExerciseRepository(this);

        initViews();
        setupListeners();
    }

    private void initViews (){
        addExcerciseButton = findViewById(R.id.addNewExcerciseBtn);
        excerciseNameEditText = findViewById(R.id.excerciseNameTxt);
        excerciseDescriptionEditText = findViewById(R.id.excerciseDescTxt);
        imagePreview = findViewById(R.id.imageButton);
    }

    private void resetForm() {
        excerciseNameEditText.setText("");
        excerciseDescriptionEditText.setText("");
        imagePreview.setImageResource(R.drawable.outline_arrow_upload_ready_24); // reset image preview
        selectedImageUri = null;
    }

    private void setupListeners () {
        addExcerciseButton.setOnClickListener(v -> {
            String name = excerciseNameEditText.getText().toString().trim();
            String description = excerciseDescriptionEditText.getText().toString().trim();

            android.util.Log.d("ExerciseActivity", "Naziv: " + name);
            android.util.Log.d("ExerciseActivity", "Opis: " + description);

            if (name.isEmpty()) {
                Toast.makeText(this, "Molimo unesite naziv vježbe", Toast.LENGTH_SHORT).show();
                return;
            }

            String filename = UUID.randomUUID().toString() + ".jpg";
            InputStream is = null;

            byte[] bytes = null;
            try {
                is = getContentResolver().openInputStream(selectedImageUri);
                bytes = IOUtils.toByteArray(is);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String mimeType = getContentResolver().getType(selectedImageUri);
            RequestBody body = RequestBody.create(bytes, MediaType.parse(mimeType));
            MultipartBody.Part part =
                    MultipartBody.Part.createFormData("file", filename, body);


            repository.uploadImage(filename, part).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody>
                        response) {
                    if (!response.isSuccessful()) {
                        return;
                    }
                    String imageUrl = Constants.BASE_URL
                            + "storage/v1/object/public/exercise-images/"
                            + filename;

                    repository.create(name, description, imageUrl);
                    resetForm();

                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                }
            });
        });
        imagePreview.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });
    }


    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedImageUri = uri;
                            imagePreview.setImageURI(uri); // prikaz previewa
                        }
                    });


}