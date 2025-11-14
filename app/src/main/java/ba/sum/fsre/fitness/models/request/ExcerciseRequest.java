package ba.sum.fsre.fitness.models.request;

import com.google.gson.annotations.SerializedName;

public class ExcerciseRequest {

    private String name;

    private String description;

    @SerializedName("created_at")
    private String createdAt;


    public ExcerciseRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
