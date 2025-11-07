package ba.sum.fsre.fitness;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface SupabaseAPI {

    @Headers("Content-Type: application/json")
    @POST("auth/v1/token?grant_type=password")
    Call<LoginResponse> login(@Body LoginRequest request);
}
