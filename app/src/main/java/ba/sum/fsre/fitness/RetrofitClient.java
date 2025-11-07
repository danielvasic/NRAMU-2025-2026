package ba.sum.fsre.fitness;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://uohmodaztsprcoqaynpl.supabase.co/";
    private static final String ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVvaG1vZGF6dHNwcmNvcWF5bnBsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjI0ODQyNjUsImV4cCI6MjA3ODA2MDI2NX0.ZgjGwQKKzwGlYLjjE7BS-lboknUTq8zZ75baKRnHex8";

    private static RetrofitClient instance;
    private SupabaseAPI api;

    private RetrofitClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> chain.proceed(
                        chain.request().newBuilder()
                                .addHeader("apikey", ANON_KEY)
                                .addHeader("Authorization", "Bearer " + ANON_KEY)
                                .addHeader("Content-Type", "application/json")
                                .build()
                ))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
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