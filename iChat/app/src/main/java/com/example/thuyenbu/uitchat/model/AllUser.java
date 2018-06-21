package com.example.thuyenbu.uitchat.model;

public class AllUser {
    public String name;
    public String email;
    public String avata;

    public AllUser() {
    }

    public AllUser(String name, String email, String avata) {
        this.name = name;
        this.email = email;
        this.avata = avata;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email = email;
    }

    public String getAvata(){
        return avata;
    }
    public void setAvata(String avata){
        this.avata = avata;
    }
}
