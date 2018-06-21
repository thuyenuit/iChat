package com.example.thuyenbu.uitchat.ui;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thuyenbu.uitchat.R;
import com.example.thuyenbu.uitchat.data.FriendDB;
import com.example.thuyenbu.uitchat.data.RequestDB;
import com.example.thuyenbu.uitchat.data.RequestDB2;
import com.example.thuyenbu.uitchat.data.StaticConfig;
import com.example.thuyenbu.uitchat.model.Friend;
import com.example.thuyenbu.uitchat.model.ListFriend;
import com.example.thuyenbu.uitchat.model.User;
import com.example.thuyenbu.uitchat.model.UserRequest;
import com.example.thuyenbu.uitchat.service.ServiceUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendRequestFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static Friend objInput;
    public static String senderId;
    private RecyclerView recyclerListFrends;
    private ListFriendsAdapter2 adapter;
    public FragFriendClickFloatButton onClickFloatButton;
    private List<UserRequest> dataListFriend = null;
    private ArrayList<String> listFriendID = null;
    private LovelyProgressDialog dialogFindAllFriend;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    public static int ACTION_START_CHAT = 1;

    public static final String ACTION_DELETE_FRIEND = "com.android.ichat.DELETE_FRIEND";

    private BroadcastReceiver deleteFriendReceiver;

    public FriendRequestFragment() {
        onClickFloatButton = new FragFriendClickFloatButton();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //List<UserRequest> dataListFriend2 = RequestDB2.getInstance(getContext()).getListRequest(StaticConfig.UID);
        RequestDB2.getInstance(getContext()).deleteAllRequest();
        //RequestDB2.getInstance(getContext()).dropDB();

        if (dataListFriend == null) {
            dataListFriend = RequestDB2.getInstance(getContext()).getListRequest(StaticConfig.UID);
            if (dataListFriend.size() > 0) {
                listFriendID = new ArrayList<>();
                for (UserRequest friend : dataListFriend) {
                    listFriendID.add(friend.userIdSender);
                }
            }
        }
        listFriendID = new ArrayList<>();
        dataListFriend = new ArrayList<>();
        getListFriendUId();

        View layout = inflater.inflate(R.layout.fragment_people, container, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerListFrends = (RecyclerView) layout.findViewById(R.id.recycleListFriend);
        recyclerListFrends.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        adapter = new ListFriendsAdapter2(getContext(), dataListFriend, this);
        recyclerListFrends.setAdapter(adapter);
        dialogFindAllFriend = new LovelyProgressDialog(getContext());
        if (listFriendID == null) {
            listFriendID = new ArrayList<>();

            getListFriendUId();
        }

        deleteFriendReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String idDeleted = intent.getExtras().getString("idFriend");
                /*for (Friend friend : dataListFriend) {
                    if(idDeleted.equals(friend.id)){
                        ArrayList<Friend> friends = dataListFriend();
                        friends.remove(friend);
                        break;
                    }
                }*/
                adapter.notifyDataSetChanged();
            }
        };

        IntentFilter intentFilter = new IntentFilter(ACTION_DELETE_FRIEND);
        getContext().registerReceiver(deleteFriendReceiver, intentFilter);

        return layout;
    }

    @Override
    public void onDestroyView (){
        super.onDestroyView();

        getContext().unregisterReceiver(deleteFriendReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ACTION_START_CHAT == requestCode && data != null && ListFriendsAdapter2.mapMark != null) {
            ListFriendsAdapter2.mapMark.put(data.getStringExtra("idFriend"), false);
        }
    }

    @Override
    public void onRefresh() {
        listFriendID.clear();
        dataListFriend.clear();
        adapter.notifyDataSetChanged();
        //RequestDB2.getInstance(getContext()).dropDB();
        getListFriendUId();
    }

    public void DeleteDB(){
        RequestDB2.getInstance(getContext()).deleteRequest(StaticConfig.USER_SENDER_ID, StaticConfig.UID);
    }

    public void AddUserToDB(){
        Friend objFriend = new Friend();
        objFriend.name = StaticConfig.inputName;
        objFriend.avata = StaticConfig.inputAvata;
        objFriend.email = StaticConfig.inputEmail;
        objFriend.id = StaticConfig.inputID;
        objFriend.idRoom = StaticConfig.inputDdRoom;
        FriendDB.getInstance(getContext()).addFriend(objFriend);
    }

    public class FragFriendClickFloatButton implements View.OnClickListener{

        Context context;
        public FriendRequestFragment.FragFriendClickFloatButton getInstance(Context context){
            this.context = context;
            return this;
        }

        @Override
        public void onClick(View view) {
            startActivity(new Intent(getContext(), UserActivity.class));
        }
    }


    /**
     * Lay danh sach yeu cau ket ban tren server
     */
    private void getListFriendUId() {
        FirebaseDatabase.getInstance().getReference().child("friendrequests/" + StaticConfig.UID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap mapRecord = (HashMap) dataSnapshot.getValue();
                    Iterator listKey = mapRecord.keySet().iterator();
                    while (listKey.hasNext()) {
                        String key = listKey.next().toString();

                        if(!listFriendID.contains(key))
                        {
                            listFriendID.add(key);
                        }
                    }

                    if(listFriendID != null)
                    {
                        if(listFriendID.size() > 0 && listFriendID.contains(StaticConfig.UID))
                        {
                            int pos = listFriendID.indexOf(StaticConfig.UID);
                            listFriendID.remove(pos);
                        }
                    }

                    getAllFriendInfo(0);

                } else {
                    dialogFindAllFriend.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * Truy cap bang user lay thong tin id nguoi dung
     */
    private void getAllFriendInfo(final int index) {
        if (index == listFriendID.size()) {
            //save list friend
            adapter.notifyDataSetChanged();
            dialogFindAllFriend.dismiss();
            mSwipeRefreshLayout.setRefreshing(false);
        } else {

            final String id = listFriendID.get(index);
            FirebaseDatabase.getInstance().getReference().child("user/" + id)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        Friend user = new Friend();
                        HashMap mapUserInfo = (HashMap) dataSnapshot.getValue();
                        user.name = (String) mapUserInfo.get("name");
                        user.email = (String) mapUserInfo.get("email");
                        user.avata = (String) mapUserInfo.get("avata");
                        user.gender = (String) mapUserInfo.get("gender");
                        user.id = id;

                        if( id != StaticConfig.UID)
                        {
                            UserRequest request = new UserRequest();
                            request.name =  user.name;
                            request.gender = user.gender;
                            request.avata =  user.avata;
                            request.userIdSender =  id;
                            request.userIdReceiver =  StaticConfig.UID;
                            request.requestType = "request_sent";

                            dataListFriend.add(request);
                           // RequestDB2.getInstance(getContext()).addRequest(request);
                        }
                    }
                    getAllFriendInfo(index + 1);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }
}

class ListFriendsAdapter2 extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public String userIdSender;
    private  List<UserRequest> listFriend;
    private Context context;
    public static Map<String, Query> mapQuery;
    public static Map<String, DatabaseReference> mapQueryOnline;
    public static Map<String, ChildEventListener> mapChildListener;
    public static Map<String, ChildEventListener> mapChildListenerOnline;
    public static Map<String, Boolean> mapMark;
    private FriendRequestFragment fragment;
    LovelyProgressDialog dialogWaitDeleting;

    public ListFriendsAdapter2(Context context, List<UserRequest> listFriend, FriendRequestFragment fragment) {
        this.listFriend = listFriend;
        this.context = context;
        mapQuery = new HashMap<>();
        mapChildListener = new HashMap<>();
        mapMark = new HashMap<>();
        mapChildListenerOnline = new HashMap<>();
        mapQueryOnline = new HashMap<>();
        this.fragment = fragment;
        dialogWaitDeleting = new LovelyProgressDialog(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_user_request, parent, false);
        return new ItemFriendViewHolder2(context, view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final String name = listFriend.get(position).name;
        final String id = listFriend.get(position).userIdSender;
        final String avata = listFriend.get(position).avata;
        final String gender = listFriend.get(position).gender;
        final boolean isOnline = listFriend.get(position).isOnline;

        ((ItemFriendViewHolder2) holder).txtName.setText(name);
        ((ItemFriendViewHolder2) holder).txtIsOnline.setBackgroundResource(isOnline ? R.drawable.circle_online : R.drawable.circle_offline);

        ((View) ((ItemFriendViewHolder2) holder).txtName.getParent().getParent().getParent())
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String userID = listFriend.get(position).userIdSender;
                        //ShowToast("MÃ: " + userID);
                    }
                });

        ((View) ((ItemFriendViewHolder2) holder).btnAgreeApply)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StaticConfig.USER_SENDER_ID =  listFriend.get(position).userIdSender;
                        StaticConfig.USER_SENDER_NAME = name;
                        AgreeApplyFriend();
                    }
                });

        ((View) ((ItemFriendViewHolder2) holder).btnDeclineApply)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        StaticConfig.USER_SENDER_ID =  listFriend.get(position).userIdSender;
                        StaticConfig.USER_SENDER_NAME = name;
                        DeclineAddFriend();
                    }
                });

        if(listFriend != null && listFriend.size() > 0)
        {
            if (listFriend.get(position).avata.equals(StaticConfig.STR_DEFAULT_BASE64)) {
                ((ItemFriendViewHolder2) holder).icon_avata.setImageResource(R.drawable.default_avata);
            } else {
                byte[] decodedString = Base64.decode(listFriend.get(position).avata, Base64.DEFAULT);
                Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ((ItemFriendViewHolder2) holder).icon_avata.setImageBitmap(src);
            }
        }

    }

    @Override
    public int getItemCount() {
        return listFriend != null ? listFriend.size() : 0;
    }


    // Đồng ý kết bạn
    private void AgreeApplyFriend() {
        FirebaseDatabase.getInstance().getReference().child("user").child(StaticConfig.USER_SENDER_ID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    // not found
                    ShowToast("Thất bại, không tìm thấy " + StaticConfig.USER_SENDER_NAME);
                }
                else {
                    if (StaticConfig.USER_SENDER_ID.equals(StaticConfig.UID)) {
                        ShowToast("Thất bại, không tìm thấy " + StaticConfig.USER_SENDER_NAME);
                    }
                    else {
                        HashMap userMap = (HashMap) ((HashMap) dataSnapshot.getValue());
                        Friend objUser = new Friend();
                        objUser.name = (String) userMap.get("name");
                        objUser.email = (String) userMap.get("email");
                        objUser.avata = (String) userMap.get("avata");
                        objUser.id = StaticConfig.USER_SENDER_ID;
                        objUser.idRoom = StaticConfig.USER_SENDER_ID.compareTo(StaticConfig.UID) > 0 ? (StaticConfig.UID + StaticConfig.USER_SENDER_ID).hashCode() + "" : "" + (StaticConfig.USER_SENDER_ID + StaticConfig.UID).hashCode();

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
                                    fragment.AddUserToDB();
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
                    .child(StaticConfig.USER_SENDER_ID).child(StaticConfig.UID).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                FirebaseDatabase.getInstance().getReference().child("friendrequests")
                                        .child(StaticConfig.UID).child(StaticConfig.USER_SENDER_ID).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    ShowToast("Bạn và " + StaticConfig.USER_SENDER_NAME + " đã trở thành bạn bè");
                                                    fragment.DeleteDB();
                                                    fragment.onRefresh();
                                                }
                                            }
                                        });
                            }
                        }
                    });
        }
    }

    //Từ chối kết bạn
    private void DeclineAddFriend(){
        FirebaseDatabase.getInstance().getReference().child("friendrequests")
                .child(StaticConfig.USER_SENDER_ID).child(StaticConfig.UID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            FirebaseDatabase.getInstance().getReference().child("friendrequests")
                                    .child(StaticConfig.UID).child(StaticConfig.USER_SENDER_ID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                ShowToast("Bạn đã từ chối kết bạn với " + StaticConfig.USER_SENDER_NAME);
                                                fragment.DeleteDB();
                                                fragment.onRefresh();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void ShowToast(String mesage){
        Toast.makeText(context, mesage, Toast.LENGTH_LONG).show();
    }

    /**
     * Delete friend
     *
     * @param idFriend
     */
    private void deleteFriend(final String idFriend) {
        if (idFriend != null) {
            FirebaseDatabase.getInstance().getReference().child("friend").child(StaticConfig.UID)
                    .orderByValue().equalTo(idFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.getValue() == null) {
                        //email not found
                        dialogWaitDeleting.dismiss();
                        new LovelyInfoDialog(context)
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

                                        FirebaseDatabase.getInstance().getReference().child("friend")
                                                .child(idFriend ).child(StaticConfig.UID).removeValue();
                                        FriendDB.getInstance(context).deleteFriend(idFriend);

                                        dialogWaitDeleting.dismiss();
                                        new LovelyInfoDialog(context)
                                                .setTopColorRes(R.color.colorAccent)
                                                .setTitle("Thành công")
                                                .setMessage("Xóa thành công")
                                                .show();

                                        Intent intentDeleted = new Intent(FriendsFragment.ACTION_DELETE_FRIEND);
                                        intentDeleted.putExtra("idFriend", idFriend);
                                        context.sendBroadcast(intentDeleted);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialogWaitDeleting.dismiss();
                                        new LovelyInfoDialog(context)
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

                }
            });
        } else {
            dialogWaitDeleting.dismiss();
            new LovelyInfoDialog(context)
                    .setTopColorRes(R.color.colorPrimary)
                    .setTitle("Lỗi")
                    .setMessage("Đã xảy ra lỗi khi xóa bạn bè")
                    .show();
        }
    }
}

class ItemFriendViewHolder2 extends RecyclerView.ViewHolder{
    public CircleImageView icon_avata;
    public TextView txtName, txtIsOnline;
    public Button btnAgreeApply, btnDeclineApply;
    private Context context;


    ItemFriendViewHolder2(Context context, View itemView) {
        super(itemView);
        icon_avata = (CircleImageView) itemView.findViewById(R.id.img_User_Request);
        txtName = (TextView) itemView.findViewById(R.id.txtUserName_UserRequest);
        txtIsOnline = (TextView)itemView.findViewById(R.id.txtIsOnline_User_Request);
        btnAgreeApply=  (Button)itemView.findViewById(R.id.btnAgreeApply);
        btnDeclineApply = (Button) itemView.findViewById(R.id.btnDeclineApply);
        this.context = context;
    }
}

