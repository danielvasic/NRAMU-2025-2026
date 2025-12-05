package ba.sum.fsre.fitness.models.request;

import com.google.gson.annotations.SerializedName;

public class ExcerciseRequest {

    private String name;

    private String description;

    @SerializedName("image_url")
    private String imageUrl;


    public ExcerciseRequest(String name, String description, String imageUrl) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
