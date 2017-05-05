package sdu.dk.iot.contextaware;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jesper on 05/05/2017.
 */

public class Arrival {

    @SerializedName("name")
    private String name;
    @SerializedName("arrival")
    private boolean arrived;

    public Arrival(String name, boolean arrived) {
        this.name = name;
        this.arrived = arrived;
    }

    public Arrival() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isArrived() {
        return arrived;
    }

    public void setArrived(boolean arrived) {
        this.arrived = arrived;
    }

    @Override
    public String toString() {
        return "Arrival{" +
                "name='" + name + '\'' +
                ", arrived=" + arrived +
                '}';
    }
}
