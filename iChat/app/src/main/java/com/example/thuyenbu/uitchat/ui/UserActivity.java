package com.example.thuyenbu.uitchat.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.thuyenbu.uitchat.R;
import com.example.thuyenbu.uitchat.data.StaticConfig;
import com.example.thuyenbu.uitchat.model.AllUser;
import com.example.thuyenbu.uitchat.utils.ImageUtils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView allUserView;

    private DatabaseReference allUserReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if(mToolbar != null) {
            getSupportActionBar().setTitle("Người dùng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setSupportActionBar(mToolbar);
        }

        allUserView = (RecyclerView)findViewById(R.id.alluser);
        allUserView.setHasFixedSize(true);
        allUserView.setLayoutManager(new LinearLayoutManager(this));

        allUserReference = FirebaseDatabase.getInstance().getReference().child("user");

    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseRecyclerAdapter<AllUser, AllUserViewHolder> firebaseRecyclerAdapter
                 = new FirebaseRecyclerAdapter<AllUser, AllUserViewHolder>
                (
                        AllUser.class,
                        R.layout.rc_all_user,
                        AllUserViewHolder.class,
                        allUserReference

                ) {
            @Override
            protected void populateViewHolder(AllUserViewHolder viewHolder, AllUser model, final int position) {

                if(!getRef(position).getKey().equals(StaticConfig.UID))
                {
                    viewHolder.setUser_Name(model.getName());
                    viewHolder.setEmail(model.getEmail());
                    viewHolder.setImageAvatar(model.getAvata());

                    viewHolder.mview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String visitUserID = getRef(position).getKey();

                            FirebaseDatabase.getInstance().getReference().child("friend").child(StaticConfig.UID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue() != null) {
                                        HashMap mapRecord = (HashMap) dataSnapshot.getValue();
                                        Iterator listKey = mapRecord.keySet().iterator();
                                        while (listKey.hasNext()) {
                                            String key = listKey.next().toString();
                                            if(getRef(position).getKey().equals(mapRecord.get(key).toString()))
                                            {
                                                Intent profileIntent = new Intent(UserActivity.this, ProfileActivity.class);
                                                profileIntent.putExtra("visitUserID", getRef(position).getKey());
                                                profileIntent.putExtra("isFriend", "true");
                                                startActivity(profileIntent);

                                                return;
                                            }
                                        }

                                        Intent profileIntent = new Intent(UserActivity.this, ProfileActivity.class);
                                        profileIntent.putExtra("visitUserID", getRef(position).getKey());
                                        profileIntent.putExtra("isFriend", "false");
                                        startActivity(profileIntent);

                                    } else {
                                        Intent profileIntent = new Intent(UserActivity.this, ProfileActivity.class);
                                        profileIntent.putExtra("visitUserID", getRef(position).getKey());
                                        profileIntent.putExtra("isFriend", "false");
                                        startActivity(profileIntent);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                    });
                }
            }
        };

        allUserView.setAdapter(firebaseRecyclerAdapter);
    }

    public  static class AllUserViewHolder extends RecyclerView.ViewHolder
    {
        View mview;
        public AllUserViewHolder(View itemView) {
            super(itemView);
            mview = itemView;
        }

        public void setUser_Name(String user_name){
            TextView name = (TextView)mview.findViewById(R.id.txtName_Person);
            name.setText(user_name);
        }

        public void setEmail(String _email){
            TextView email = (TextView)mview.findViewById(R.id.txtEmail_Person);
            email.setText(_email);
        }

        public void setImageAvatar(String imgBase64){
            CircleImageView avatar = (CircleImageView)mview.findViewById(R.id.icon_avata_Person);

            if (imgBase64.equals(StaticConfig.STR_DEFAULT_BASE64)) {
                avatar.setImageResource(R.drawable.default_avata);
            } else {
                byte[] decodedString = Base64.decode(imgBase64, Base64.DEFAULT);
                Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                avatar.setImageBitmap(src);
            }
        }
    }


}
