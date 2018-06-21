package com.example.thuyenbu.uitchat.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thuyenbu.uitchat.R;
import com.example.thuyenbu.uitchat.data.FriendDB;
import com.example.thuyenbu.uitchat.data.RequestDB2;
import com.example.thuyenbu.uitchat.data.StaticConfig;
import com.example.thuyenbu.uitchat.model.Friend;
import com.example.thuyenbu.uitchat.model.UserRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ProfileActivity extends AppCompatActivity {

    private Button sendRequest;
    private Button btnCancel;

    private TextView txtName;
    private TextView txtGender;
    private ImageView avata;
    private Toolbar mToolbar;
    private DatabaseReference UsersReference;
    private DatabaseReference FriendReference;
    private DatabaseReference FriendRequestReference;
    private DatabaseReference notificationDatabase;

    private FirebaseAuth mAuth;
    private String CURRENT_STATE;
    private String sender_user_id;
    private String receiver_user_id;
    private  String FriendStr;

    private int state;
    private  boolean isFriend;
    private String name;
    private String gender;
    private String imgBase64;

    LovelyProgressDialog dialogWaitDeleting;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if(mToolbar != null) {
            getSupportActionBar().setTitle("Thông tin người dùng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setSupportActionBar(mToolbar);
        }

        state = 1;
        //1: Ban be
        //2: Gui yeu cau ket ban
        //3: Huy ket ban
        isFriend = false;

        CURRENT_STATE = "NOT_FRIEND";

        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        FriendRequestReference = FirebaseDatabase.getInstance().getReference().child("friendrequests");
        FriendReference = FirebaseDatabase.getInstance().getReference().child("friend");
        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();

        UsersReference = FirebaseDatabase.getInstance().getReference().child("user");
        receiver_user_id = getIntent().getExtras().getString("visitUserID").toString();
        FriendStr = getIntent().getExtras().getString("isFriend").toString();
        isFriend = FriendStr.equals("true") ? true: false;

        sendRequest = (Button)findViewById(R.id.btnSendRequest);
        txtName = (TextView)findViewById(R.id.txtName_info);
        txtGender = (TextView)findViewById(R.id.txtGender_info);
        avata = (ImageView)findViewById(R.id.img_avatar_info);
        btnCancel = (Button)findViewById(R.id.btnCancelRequest);

        dialogWaitDeleting = new LovelyProgressDialog(getBaseContext());
        context = getBaseContext();

        UsersReference.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("name").getValue().toString();
                gender = dataSnapshot.child("gender").getValue().toString();
                imgBase64 = dataSnapshot.child("avata").getValue().toString();

                txtName.setText(name);
                txtGender.setText((gender.equals("1") ? "Giới tính: Nam" : "Giới tính: Nữ"));

                if (imgBase64.equals(StaticConfig.STR_DEFAULT_BASE64)) {
                    avata.setImageResource(R.drawable.default_avata);
                } else {
                    byte[] decodedString = Base64.decode(imgBase64, Base64.DEFAULT);
                    Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    avata.setImageBitmap(src);
                }

                // Nếu là bạn bè
                if(isFriend)
                {
                    CURRENT_STATE = "friend";
                    sendRequest.setVisibility(View.GONE);
                    btnCancel.setText("Hủy kết bạn");

                   // ImageView myImage = (ImageView) findViewById(R.id.image_view);
                    ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) btnCancel.getLayoutParams();
                    marginParams.setMargins(marginParams.leftMargin, 90, marginParams.rightMargin, marginParams.bottomMargin);
                }
                else // Người lạ
                {
                    btnCancel.setVisibility(View.GONE);
                    FriendRequestReference.child(receiver_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(StaticConfig.UID)){
                                String request_type = dataSnapshot.child(StaticConfig.UID).child("request_type").getValue().toString();

                                if(request_type.equals("receiver")){
                                    CURRENT_STATE = "request_sent";
                                    sendRequest.setText("Hủy yêu cầu kết bạn");
                                    btnCancel.setVisibility(View.INVISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    FriendRequestReference.child(StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiver_user_id)){
                                String request_type = dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();

                                if(request_type.equals("receiver")){
                                    CURRENT_STATE = "waiting_accept";
                                    sendRequest.setText("Đồng ý kết bạn");
                                    btnCancel.setText("Từ chối kết bạn");
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        // Gửi yêu cầu kết bạn/hoặc hủy
        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCancel.setEnabled(false);

                // Gửi yêu cầu kết bạn
                if(CURRENT_STATE.equals("NOT_FRIEND")){
                    SendRequestToAPerson();
                }

                // Hủy yêu cầu kết bạn
                if(CURRENT_STATE.equals("receiver"))
                {
                    CancelRequestToAPerson();
                }

                // Đồng ý kết bạn
                if(CURRENT_STATE.equals("waiting_accept"))
                {
                    AgreeApplyFriend();
                }
            }
        });

        //Hủy kết bạn/ Từ chối kết bạn
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFriend){
                    deleteFriend();
                    /*new AlertDialog.Builder(context).setTitle("Hủy kết bạn")
                            .setMessage("Bạn có chắc muốn hủy kết bạn?")
                            .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    dialogWaitDeleting.setTitle("Đang xóa")
                                            .setCancelable(false)
                                            .setTopColorRes(R.color.colorAccent)
                                            .show();
                                    deleteFriend();
                                   // sendRequest.setEnabled(false);
                                }
                            }).setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();*/
                }
                else
                {
                    // Từ chối kết bạn
                    if(CURRENT_STATE.equals("waiting_accept"))
                    {
                        DeclineRequest();
                    }
                }
            }
        });
    }

    // Đồng ý trở thành bạn bè
    private void AgreeApplyFriend() {
        FirebaseDatabase.getInstance().getReference().child("user").child(receiver_user_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null) {
                            // not found
                            ShowToast("Thất bại, không tìm thấy " + name);
                        }
                        else {
                            if (StaticConfig.USER_SENDER_ID.equals(StaticConfig.UID)) {
                                ShowToast("Thất bại, không tìm thấy " + name);
                            }
                            else {
                                HashMap userMap = (HashMap) ((HashMap) dataSnapshot.getValue());
                                Friend objUser = new Friend();
                                objUser.name = (String) userMap.get("name");
                                objUser.email = (String) userMap.get("email");
                                objUser.avata = (String) userMap.get("avata");
                                objUser.id = receiver_user_id;
                                objUser.idRoom = receiver_user_id.compareTo(StaticConfig.UID) > 0 ? (StaticConfig.UID + receiver_user_id).hashCode() + "" : "" + (receiver_user_id + StaticConfig.UID).hashCode();

                                addFriend(objUser.id, true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void addFriend(final String idFriend, boolean isIdFriend) {
        if (idFriend != null) {
            if (isIdFriend) {
                FirebaseDatabase.getInstance().getReference().child("friend/" + StaticConfig.UID).push().setValue(idFriend)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    addFriend(idFriend, false);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                ShowToast("Thêm bạn bè thất bại!");
                            }
                        });
            }
            else {
                FirebaseDatabase.getInstance().getReference().child("friend/" + idFriend).push().setValue(StaticConfig.UID)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Friend objFriend = new Friend();
                                    objFriend.name = StaticConfig.inputName;
                                    objFriend.avata = StaticConfig.inputAvata;
                                    objFriend.email = StaticConfig.inputEmail;
                                    objFriend.id = StaticConfig.inputID;
                                    objFriend.idRoom = StaticConfig.inputDdRoom;
                                    FriendDB.getInstance(getBaseContext()).addFriend(objFriend);

                                    addFriend(null, false);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                ShowToast("Thêm bạn bè thất bại!");
                            }
                        });
            }
        }
        else {
            FirebaseDatabase.getInstance().getReference().child("friendrequests")
                    .child(StaticConfig.UID).child(receiver_user_id).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                ShowToast("Bạn và " + name + " đã trở thành bạn bè");
                                //RequestDB2.getInstance(getBaseContext())
                                //        .deleteRequest(receiver_user_id, StaticConfig.UID);
                                                    //fragment.onRefresh();
                            }
                        }
                    });
        }
    }

    // Từ chối kết bạn
    private void DeclineRequest(){
        FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            sendRequest.setEnabled(true);
                            CURRENT_STATE = "NOT_FRIEND";
                            sendRequest.setText("Gửi yêu cầu kết bạn");
                            btnCancel.setVisibility(View.INVISIBLE);
                            ShowToast("Bạn đã từ chối kết với " + name);
                           // RequestDB2.getInstance(getBaseContext()).deleteRequest(sender_user_id, receiver_user_id);
                        }
                    }
                });
    }

    private void deleteFriend() {

            FirebaseDatabase.getInstance().getReference().child("friend").child(StaticConfig.UID)
                    .orderByValue().equalTo(receiver_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.getValue() == null) {
                        //email not found
                        dialogWaitDeleting.dismiss();
                        new LovelyInfoDialog(getBaseContext())
                                .setTopColorRes(R.color.colorAccent)
                                .setTitle("Lỗi")
                                .setMessage("Đã xảy ra lỗi khi xóa bạn bè")
                                .show();
                    } else {
                        String idRemoval = ((HashMap) dataSnapshot.getValue()).keySet().iterator().next().toString();

                        FirebaseDatabase.getInstance().getReference().child("friend")
                                .child(StaticConfig.UID).child(idRemoval).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        FirebaseDatabase.getInstance().getReference().child("friend").child(receiver_user_id)
                                                .orderByValue().equalTo(StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.getValue() == null){
                                                    dialogWaitDeleting.dismiss();
                                                    new LovelyInfoDialog(getBaseContext())
                                                            .setTopColorRes(R.color.colorAccent)
                                                            .setTitle("Lỗi")
                                                            .setMessage("Đã xảy ra lỗi khi xóa bạn bè")
                                                            .show();
                                                }
                                                else
                                                {
                                                    String idRemoval2 = ((HashMap) dataSnapshot.getValue()).keySet().iterator().next().toString();

                                                    FirebaseDatabase.getInstance().getReference().child("friend")
                                                            .child(receiver_user_id).child(idRemoval2).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful())
                                                            {
                                                                FriendDB.getInstance(getBaseContext()).deleteFriend(receiver_user_id);
                                                                Toast.makeText(getBaseContext(), "Hủy kết bạn thành công", Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialogWaitDeleting.dismiss();
                                        new LovelyInfoDialog(getBaseContext())
                                                .setTopColorRes(R.color.colorAccent)
                                                .setTitle("Lỗi")
                                                .setMessage("Đã xảy ra lỗi khi xóa bạn bè")
                                                .show();
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    dialogWaitDeleting.dismiss();
                    new LovelyInfoDialog(getBaseContext())
                            .setTopColorRes(R.color.colorAccent)
                            .setTitle("Lỗi")
                            .setMessage("Đã xảy ra lỗi khi xóa bạn bè")
                            .show();
                }
            });
    }

    private void CancelFriend(){
        FriendRequestReference.child(sender_user_id).child(receiver_user_id).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            FriendRequestReference.child(receiver_user_id).child(sender_user_id)
                                    .child("request_type").setValue("receiver")
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                sendRequest.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                sendRequest.setText("Hủy yêu cầu kết bạn");
                                                ShowToast("Gửi yêu cầu kết bạn thành công.");

                                                UserRequest request = new UserRequest();
                                                request.userIdSender = sender_user_id;
                                                request.userIdReceiver = receiver_user_id;
                                                request.requestType = CURRENT_STATE;
                                                RequestDB2.getInstance(getBaseContext()).addRequest(request);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CheckIsFriend() {
        FirebaseDatabase.getInstance().getReference().child("friend/" + StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap mapRecord = (HashMap) dataSnapshot.getValue();
                    Iterator listKey = mapRecord.keySet().iterator();
                    while (listKey.hasNext()) {
                        String key = listKey.next().toString();
                        if(receiver_user_id.equals(mapRecord.get(key).toString()))
                        {
                            isFriend = true;
                            return;
                        }
                    }
                } else {
                    isFriend =  false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void CancelRequestToAPerson() {
        FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            sendRequest.setEnabled(true);
                            CURRENT_STATE = "NOT_FRIEND";
                            sendRequest.setText("Gửi yêu cầu kết bạn");
                            ShowToast("Hủy yêu cầu kết bạn thành công.");
                            RequestDB2.getInstance(getBaseContext()).deleteRequest(sender_user_id, receiver_user_id);
                        }
                    }
                });
    }

    private void SendRequestToAPerson(){
        FriendRequestReference.child(receiver_user_id).child(sender_user_id)
                                    .child("request_type").setValue("receiver")
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                HashMap<String, String> hashMap = new HashMap<>();
                                                hashMap.put("from", sender_user_id);
                                                hashMap.put("type", "request");

                                                notificationDatabase.child(receiver_user_id).push().setValue(hashMap)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        sendRequest.setEnabled(true);
                                                        CURRENT_STATE = "request_sent";
                                                        sendRequest.setText("Hủy yêu cầu kết bạn");
                                                        ShowToast("Gửi yêu cầu kết bạn thành công.");

                                                        UserRequest request = new UserRequest();
                                                        request.userIdSender = sender_user_id;
                                                        request.userIdReceiver = receiver_user_id;
                                                        request.requestType = CURRENT_STATE;
                                                        RequestDB2.getInstance(getBaseContext()).addRequest(request);


                                                    }
                                                });
                                            }
                                        }
                                    });
    }

    private void ShowToast(String message)
    {
        Toast.makeText(this, message , Toast.LENGTH_LONG).show();
    }
}
