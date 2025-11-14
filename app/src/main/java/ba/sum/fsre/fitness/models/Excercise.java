package ba.sum.fsre.fitness.models;

import com.google.gson.annotations.SerializedName;

public class Excercise {

    private String id;

    private String name;

    private String description;

    @SerializedName("created_at")
    private String createdAt;

    public Excercise(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
