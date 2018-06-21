package com.example.thuyenbu.uitchat.model;

import java.util.ArrayList;


public class ListFriendRequest {
    private ArrayList<FriendRequest> listRequest;

    public ListFriendRequest() {
        listRequest = new ArrayList<>();
    }

    public ArrayList<FriendRequest> getListRequest() {
        return listRequest;
    }

    public String getAvataById(String id){
        for(FriendRequest request: listRequest){
            if(id.equals(request.id)){
                return request.avata;
            }
        }
        return "";
    }

    public void setListFriend(ArrayList<Friend> listFriend) {
        this.listRequest = listRequest;
    }
}
