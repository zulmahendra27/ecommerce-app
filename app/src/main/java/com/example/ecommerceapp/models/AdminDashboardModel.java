package com.example.ecommerceapp.models;

import java.io.Serializable;

public class AdminDashboardModel implements Serializable {
    String name;
    String type;
    String img_url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public AdminDashboardModel(String name, String type, String img_url) {
        this.name = name;
        this.type = type;
        this.img_url = img_url;
    }

    public AdminDashboardModel() {
    }
}
