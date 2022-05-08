package com.asaadna.photogram.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String email = "";
    public String username = "";
    public List<String> followers = new ArrayList<String>();
    public List<String> following = new ArrayList<String>();
    public List<String> hearts = new ArrayList<String>();
}
