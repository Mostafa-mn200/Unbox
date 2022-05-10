package com.example.Unbox.frinds;

public class contacts {
    public String name, states, image;

    public contacts() {
    }

    public contacts(String name, String states, String image) {
        this.name = name;
        this.states = states;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStates() {
        return states;
    }

    public void setStates(String states) {
        this.states = states;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
