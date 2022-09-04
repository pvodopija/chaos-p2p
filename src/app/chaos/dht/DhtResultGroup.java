package app.chaos.dht;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DhtResultGroup implements Serializable {

    @Serial
    private static final long serialVersionUID = -6100573619165598679L;

    private List<DhtResult> results;
    private boolean poison;

    public DhtResultGroup(List<DhtResult> results) {
        this.results = results;
        this.poison = false;
    }

    public DhtResultGroup( boolean poison ) {
        this.results = new ArrayList<>();
        this.poison = poison;
    }

    public List<DhtResult> getResults() {
        return results;
    }

    public boolean isPoison() {
        return poison;
    }

    @Override
    public String toString() {
        return "DhtResultGroup{" +
                "results_size=" + results.size() +
                ", poison=" + poison +
                '}';
    }
}
