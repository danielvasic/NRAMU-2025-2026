package ba.sum.fsre.fitness.api;

import java.util.List;

import ba.sum.fsre.fitness.models.Excercise;
import ba.sum.fsre.fitness.models.request.ExcerciseRequest;
import ba.sum.fsre.fitness.models.request.LoginRequest;
import ba.sum.fsre.fitness.models.request.RegisterRequest;
import ba.sum.fsre.fitness.models.response.AuthResponse;
import ba.sum.fsre.fitness.models.response.ExcerciseResponse;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SupabaseAPI {

    // Auth endpoints
    @Headers("Content-Type: application/json")
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest request);

    @Headers("Content-Type: application/json")
    @POST("auth/v1/signup")
    Call<AuthResponse> signup(@Body RegisterRequest request);


    @Headers({"Content-Type: application/json", "Prefer: return=representation"})
    @POST("rest/v1/excercise")
    Call<List<Excercise>> createExcercise (
            @Header("Authorization") String token,
            @Body ExcerciseRequest request
    );

    @GET("rest/v1/excercise")
    Call<List<Excercise>> getAll (
            @Header("Authorization") String token
    );

    @Headers({"Content-Type: application/json", "Prefer: return=representation"})
    @PATCH("rest/v1/excercise")
    Call<List<Excercise>> updateExcercise(
            @Header("Authorization") String token,
            @Query("id") String filter, // ovdje proslijediti "eq." + id
            @Body ExcerciseRequest request // tijelo sa podacima za update
    );

    @DELETE("rest/v1/excercise")
    Call<Void> deleteExcercise(
            @Header("Authorization") String token,
            @Query("id") String filter // "eq." + id
    );

    @GET("rest/v1/excercise")
    Call<List<Excercise>> getById(
            @Header("Authorization") String token,
            @Query("id") String filter // "eq." + id
    );

    @Headers("Prefer: return=representation")
    @Multipart
    @POST("storage/v1/object/exercise-images/{filename}")
    Call<ResponseBody> uploadExerciseImage(
            @Header("Authorization") String token,
            @Header("apikey") String apiKey,
            @Path("filename") String filename, // npr. "exercise-123.jpg"
            @Part MultipartBody.Part filePart // sadržaj slike
    );


    @DELETE("storage/v1/object/exercise-images/{filename}")
    Call<Void> deleteExerciseImage(
            @Header("Authorization") String token,
            @Header("apikey") String apiKey,
            @Path("filename") String filename
    );
}