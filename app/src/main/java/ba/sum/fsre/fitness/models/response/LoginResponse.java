package ba.sum.fsre.fitness.models.response;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("access_token")
    private String accessToken;

    private User user;

    public String getAccessToken() {
        return accessToken;
    }

    public User getUser() {
        return user;
    }

    public static class User {
        private String email;

        public String getEmail() {
            return email;
        }
    }
}