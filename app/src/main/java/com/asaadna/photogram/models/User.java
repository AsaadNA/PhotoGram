package com.asaadna.photogram.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    public String id = "";
    public String email = "";
    public String username = "";
    public List<String> followers = new ArrayList<String>();
    public List<String> pending = new ArrayList<String>();
    public List<String> following = new ArrayList<String>();
    public List<String> requests = new ArrayList<String>();
    public List<String> hearts = new ArrayList<String>();
}
