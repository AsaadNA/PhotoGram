package com.asaadna.photogram.models;

import java.util.ArrayList;
import java.util.List;

public class Post {

    //Personal use
    private String id = "";

    public String description = "";
    public List<String> hearts = new ArrayList<String>();
    public String image_url = "";
    public User user = null;
    public long creation_time_ms;

    public String getID() {
        return this.id;
    }

    public void setID(String id) {
        this.id = id;
    }
}
