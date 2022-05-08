package com.asaadna.photogram.models;

import java.util.ArrayList;
import java.util.List;

public class Post {
    public String description = "";
    public List<String> hearts = new ArrayList<String>();
    public String image_url = "";
    public User user = null;
    public long creation_time_ms;
}
