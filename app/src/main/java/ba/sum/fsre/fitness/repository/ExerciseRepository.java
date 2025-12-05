package ba.sum.fsre.fitness.repository;

import android.content.Context;

import java.io.IOException;
import java.util.List;

import ba.sum.fsre.fitness.api.RetrofitClient;
import ba.sum.fsre.fitness.api.SupabaseAPI;
import ba.sum.fsre.fitness.models.Excercise;
import ba.sum.fsre.fitness.models.request.ExcerciseRequest;
import ba.sum.fsre.fitness.utils.AuthManager;
import ba.sum.fsre.fitness.utils.Constants;
import retrofit2.Call;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.gson.Gson;

public class ExerciseRepository {
    private final SupabaseAPI api = RetrofitClient.getInstance().getApi();
    private final AuthManager auth;
    public ExerciseRepository(Context ctx) { auth = new AuthManager(ctx);
    }
    private String bearer() { return "Bearer " + auth.getToken(); }
    public Call<List<Excercise>> getAll() {
        return api.getAll(bearer());
    }
    public Call<List<Excercise>> create(String name, String desc, String
            imageUrl) {
        ExcerciseRequest req = new ExcerciseRequest(name, desc, imageUrl);
        // Log request body
        System.out.println("[ExerciseRepository] create() - request: " + new Gson().toJson(req));

        Call<List<Excercise>> call = api.createExcercise(bearer(), req);
        call.enqueue(new Callback<List<Excercise>>() {
            @Override
            public void onResponse(Call<List<Excercise>> call, Response<List<Excercise>> response) {
                System.out.println("[ExerciseRepository] create() - response code: " + response.code());
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        System.out.println("[ExerciseRepository] create() - body: " + new Gson().toJson(response.body()));
                    } else {
                        // try to print error body
                        if (response.errorBody() != null) {
                            String err = response.errorBody().string();
                            System.out.println("[ExerciseRepository] create() - error body: " + err);
                        } else {
                            System.out.println("[ExerciseRepository] create() - empty error body");
                        }
                    }
                } catch (IOException e) {
                    System.out.println("[ExerciseRepository] create() - failed to read response/error body: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<List<Excercise>> call, Throwable t) {
                System.out.println("[ExerciseRepository] create() - failure: " + t.getMessage());
                t.printStackTrace();
            }
        });
        return call;
    }
    public Call<List<Excercise>> update(String id, String name, String
            desc, String imageUrl) {
        return api.updateExcercise(bearer(), "eq." + id, new
                ExcerciseRequest(name, desc, imageUrl));
    }
    public Call<Void> delete(String id) {
        return api.deleteExcercise(bearer(), "eq." + id);
    }
    public Call<ResponseBody> uploadImage(String filename,
                                          MultipartBody.Part filePart) {
        return api.uploadExerciseImage(bearer(), Constants.ANON_KEY,
                filename, filePart);
    }
}
