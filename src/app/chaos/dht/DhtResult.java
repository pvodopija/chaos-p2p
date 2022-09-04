package app.chaos.dht;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DhtResult implements Serializable {

    @Serial
    private static final long serialVersionUID = -2822603794411115601L;

    private String key;
    private java.util.List<Point> values;

    private boolean poison;

    public DhtResult( String key, java.util.List<Point> values ) {
        this.key = key;
        this.values = new ArrayList<>( values );
        poison = false;
    }

    public DhtResult( String key, boolean poison ) {
        this.key = key;
        this.values = new ArrayList<>();
        this.poison = poison;
    }

    public String getKey() {
        return key;
    }

    public List<Point> getValues() {
        return values;
    }

    public void setPoison(boolean poison) {
        this.poison = poison;
    }

    public boolean isPoison() {
        return poison;
    }

    @Override
    public String toString() {
        if( poison )
            return "";

        return "DhtResult{" +
                "key='" + key + '\'' +
                ", values=" + values +
                '}';
    }
}