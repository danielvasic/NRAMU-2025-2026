package ba.sum.fsre.fitness.models.response;

import ba.sum.fsre.fitness.models.Excercise;
import java.util.List;

public class ExcerciseResponse {
    private List<Excercise> data;

    public ExcerciseResponse(List<Excercise> data) {
        this.data = data;
    }

    public List<Excercise> getData() {
        return data;
    }

    public Excercise getFirstExercise() {
        return (data != null && !data.isEmpty()) ? data.get(0) : null;
    }

    public boolean isSuccess() {
        return data != null && !data.isEmpty();
    }
}