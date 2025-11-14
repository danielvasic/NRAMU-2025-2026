package ba.sum.fsre.fitness.api;

import ba.sum.fsre.fitness.utils.Constants;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitClient instance;
    private SupabaseAPI api;

    private RetrofitClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> chain.proceed(
                        chain.request().newBuilder()
                                .addHeader("apikey", Constants.ANON_KEY)
                                .addHeader("Content-Type", "application/json")
                                .build()
                ))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(SupabaseAPI.class);
    }

    public static RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public SupabaseAPI getApi() {
        return api;
    }
}