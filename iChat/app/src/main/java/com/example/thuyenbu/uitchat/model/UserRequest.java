package com.example.thuyenbu.uitchat.model;

public class UserRequest extends Request {
    public String name;
    public String avata;
    public String gender;
    public boolean isOnline;

    public UserRequest(){

    }

    public UserRequest(String name, String avata, String gender, boolean isOnline) {
        this.name = name;
        this.avata = avata;
        this.gender = gender;
        this.isOnline = isOnline;
    }

    public String getName(){
        return  name;
    }
    public void setName(String _name){
        this.name= name;
    }

    public  String getAvata(){
        return  avata;
    }
    public void setAvata(){
        this.avata= avata;
    }

    public  String getGender(){
        return  gender;
    }
    public void setGender(){
        this.gender= gender;
    }

    public boolean getIsOnline(){
        return isOnline;
    }
    public void setIsOnline(){
        this.isOnline = isOnline;
    }
}
