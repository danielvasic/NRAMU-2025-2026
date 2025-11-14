package ba.sum.fsre.fitness.api;

import java.util.List;

import ba.sum.fsre.fitness.models.Excercise;
import ba.sum.fsre.fitness.models.request.ExcerciseRequest;
import ba.sum.fsre.fitness.models.request.LoginRequest;
import ba.sum.fsre.fitness.models.request.RegisterRequest;
import ba.sum.fsre.fitness.models.response.AuthResponse;
import ba.sum.fsre.fitness.models.response.ExcerciseResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SupabaseAPI {

    // Auth endpoints
    @Headers("Content-Type: application/json")
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest request);

    @Headers("Content-Type: application/json")
    @POST("auth/v1/signup")
    Call<AuthResponse> signup(@Body RegisterRequest request);


    @Headers("Content-Type: application/json")
    @POST("rest/v1/excercise")
    Call<List<Excercise>> createExcercise (
            @Header("Authorization") String token,
            @Body ExcerciseRequest request
    );
}