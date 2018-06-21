package com.example.thuyenbu.uitchat.ui;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thuyenbu.uitchat.MainActivity;
import com.example.thuyenbu.uitchat.R;
import com.example.thuyenbu.uitchat.model.MessageNotification;
import com.example.thuyenbu.uitchat.service.FriendChatService;
import com.example.thuyenbu.uitchat.utils.ImageUtils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.thuyenbu.uitchat.data.SharedPreferenceHelper;
import com.example.thuyenbu.uitchat.data.StaticConfig;
import com.example.thuyenbu.uitchat.model.Consersation;
import com.example.thuyenbu.uitchat.model.Message;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView recyclerChat;
    public static final int VIEW_TYPE_USER_MESSAGE = 0;
    public static final int VIEW_TYPE_FRIEND_MESSAGE = 1;
    private ListMessageAdapter adapter;
    private String roomId;
    private ArrayList<CharSequence> idFriend;
    private Consersation consersation;
    private ImageButton btnSend;
    private EditText editWriteMessage;
    private LinearLayoutManager linearLayoutManager;
    public static HashMap<String, Bitmap> bitmapAvataFriend;
    public Bitmap bitmapAvataUser;

    private Toolbar mToolbar;
    private static final int PICK_IMAGE = 1994;

    private String userReceiver;
    private String chatGroup;
    private String contentGroup;

    private StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if(mToolbar != null) {
            getSupportActionBar().setTitle("Nhắn tin");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setSupportActionBar(mToolbar);
        }

        mImageStorage = FirebaseStorage.getInstance().getReference();
        Intent intentData = getIntent();
        idFriend = intentData.getCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID);
        roomId = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID);
        String nameFriend = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND);
        userReceiver = intentData.getStringExtra("UserReceiver");
        chatGroup = intentData.getStringExtra("CHAT_GROUP");

        consersation = new Consersation();
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        String base64AvataUser = SharedPreferenceHelper.getInstance(this).getUserInfo().avata;
        if (!base64AvataUser.equals(StaticConfig.STR_DEFAULT_BASE64)) {
            byte[] decodedString = Base64.decode(base64AvataUser, Base64.DEFAULT);
            bitmapAvataUser = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } else {
            bitmapAvataUser = null;
        }

        editWriteMessage = (EditText) findViewById(R.id.editWriteMessage);
        if (idFriend != null && nameFriend != null) {
            getSupportActionBar().setTitle(nameFriend);
            linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            recyclerChat = (RecyclerView) findViewById(R.id.recyclerChat);
            recyclerChat.setLayoutManager(linearLayoutManager);
            adapter = new ListMessageAdapter(this, consersation, bitmapAvataFriend, bitmapAvataUser);
            FirebaseDatabase.getInstance().getReference().child("message/" + roomId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getValue() != null) {
                        HashMap mapMessage = (HashMap) dataSnapshot.getValue();
                        Message newMessage = new Message();
                        newMessage.idSender = (String) mapMessage.get("idSender");
                        newMessage.idReceiver = (String) mapMessage.get("idReceiver");
                        newMessage.text = (String) mapMessage.get("text");
                        newMessage.timestamp = (long) mapMessage.get("timestamp");
                        newMessage.type = (String) mapMessage.get("type");
                        consersation.getListMessageData().add(newMessage);
                        adapter.notifyDataSetChanged();
                        linearLayoutManager.scrollToPosition(consersation.getListMessageData().size() - 1);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            recyclerChat.setAdapter(adapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Intent result = new Intent();
            result.putExtra("idFriend", idFriend.get(0));
            setResult(RESULT_OK, result);
            this.finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putExtra("idFriend", idFriend.get(0));
        setResult(RESULT_OK, result);
        this.finish();
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnSend) {
            String content = editWriteMessage.getText().toString().trim();
            if (content.length() > 0) {
                contentGroup = content;
                editWriteMessage.setText("");
                Message newMessage = new Message();
                newMessage.text = content;
                newMessage.idSender = StaticConfig.UID;
                newMessage.idReceiver = roomId;
                newMessage.timestamp = System.currentTimeMillis();
                newMessage.type = "text";
                FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage);

                if(chatGroup.equals("false")) // Chat one to one
                {
                    MessageNotification messageNotification = new MessageNotification();
                    messageNotification.message = content;
                    messageNotification.from = StaticConfig.UID;
                    FirebaseDatabase.getInstance().getReference()
                            .child("messagenotification/" + roomId + "/" +  userReceiver)
                            .push().setValue(messageNotification);
                }
                else if(chatGroup.equals("true")) // Chat group
                {
                    FirebaseDatabase.getInstance().getReference().child("group/"+ roomId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.getValue() != null){

                                HashMap mapGroup = (HashMap) dataSnapshot.getValue();
                                ArrayList<String> member = (ArrayList<String>) mapGroup.get("member");
                                HashMap mapGroupInfo = (HashMap) mapGroup.get("groupInfo");

                                String admin = (String) mapGroupInfo.get("admin");

                                for(String idMember: member){
                                    if(!admin.equals(idMember))
                                    {
                                        MessageNotification messageNotification = new MessageNotification();
                                        messageNotification.message = contentGroup;
                                        messageNotification.from = StaticConfig.UID;
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("messagegroupnotification/" + roomId + "/" +  idMember)
                                                .push().setValue(messageNotification);
                                    }

                                }
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
            else
            {
               // Intent intent = new Intent();
              //  intent.setType("image/*");
               // intent.setAction(Intent.ACTION_PICK);
               // startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        }
//        else if(view.getId() == R.id.btnChooseImage)
//        {
//             Intent intent = new Intent();
//             intent.setType("image/*");
//             intent.setAction(Intent.ACTION_PICK);
//             startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_LONG).show();
                return;
            }
            try {

                InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
                Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);
                imgBitmap = ImageUtils.cropToSquare(imgBitmap);
                InputStream is = ImageUtils.convertBitmapToInputStream(imgBitmap);
                final Bitmap liteImage = ImageUtils.makeImageLite(is,
                        imgBitmap.getWidth(), imgBitmap.getHeight(),
                        ImageUtils.AVATAR_WIDTH, ImageUtils.AVATAR_HEIGHT);

                String imageBase64 = ImageUtils.encodeBase64(liteImage);

                Message newMessage = new Message();
                newMessage.text = imageBase64;
                newMessage.idSender = StaticConfig.UID;
                newMessage.idReceiver = roomId;
                newMessage.timestamp = System.currentTimeMillis();
                newMessage.type = "image";
                FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage);

                if(chatGroup.equals("false")) // Chat one to one
                {
                    MessageNotification messageNotification = new MessageNotification();
                    messageNotification.message = "Đã gửi hình ảnh";
                    messageNotification.from = StaticConfig.UID;
                    FirebaseDatabase.getInstance().getReference()
                            .child("messagenotification/" + roomId + "/" +  userReceiver)
                            .push().setValue(messageNotification);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

class ListMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private Consersation consersation;
    private HashMap<String, Bitmap> bitmapAvata;
    private HashMap<String, DatabaseReference> bitmapAvataDB;
    private Bitmap bitmapAvataUser;

    public ListMessageAdapter(Context context, Consersation consersation, HashMap<String, Bitmap> bitmapAvata, Bitmap bitmapAvataUser) {
        this.context = context;
        this.consersation = consersation;
        this.bitmapAvata = bitmapAvata;
        this.bitmapAvataUser = bitmapAvataUser;
        bitmapAvataDB = new HashMap<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ChatActivity.VIEW_TYPE_FRIEND_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_friend, parent, false);
            return new ItemMessageFriendHolder(view);
        } else if (viewType == ChatActivity.VIEW_TYPE_USER_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_user, parent, false);
            return new ItemMessageUserHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemMessageFriendHolder) {


                ((ItemMessageFriendHolder) holder).txtContent.setText(consersation.getListMessageData().get(position).text);

                String timeFriend = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(consersation.getListMessageData().get(position).timestamp));
                String todayFriend = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(System.currentTimeMillis()));

                if (todayFriend.equals(timeFriend)) {
                    ((ItemMessageFriendHolder) holder).txtTimeFriend.setText(new SimpleDateFormat("HH:mm").format(new Date(consersation.getListMessageData().get(position).timestamp)));
                } else {
                    ((ItemMessageFriendHolder) holder).txtTimeFriend.setText(new SimpleDateFormat("MMM d").format(new Date(consersation.getListMessageData().get(position).timestamp)));
                }



            Bitmap currentAvata = bitmapAvata.get(consersation.getListMessageData().get(position).idSender);
            if (currentAvata != null) {
                ((ItemMessageFriendHolder) holder).avata.setImageBitmap(currentAvata);
            } else {
                final String id = consersation.getListMessageData().get(position).idSender;
                if(bitmapAvataDB.get(id) == null){
                    bitmapAvataDB.put(id, FirebaseDatabase.getInstance().getReference().child("user/" + id + "/avata"));
                    bitmapAvataDB.get(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                String avataStr = (String) dataSnapshot.getValue();
                                if(!avataStr.equals(StaticConfig.STR_DEFAULT_BASE64)) {
                                    byte[] decodedString = Base64.decode(avataStr, Base64.DEFAULT);
                                    ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                                }else{
                                    ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avata));
                                }
                                notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        } else if (holder instanceof ItemMessageUserHolder) {

                ((ItemMessageUserHolder) holder).txtContent.setText(consersation.getListMessageData().get(position).text);

                String timeUser = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(consersation.getListMessageData().get(position).timestamp));
                String todayUser = new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(System.currentTimeMillis()));

                if (todayUser.equals(timeUser)) {
                    ((ItemMessageUserHolder) holder).txtTimeUser.setText(new SimpleDateFormat("HH:mm").format(new Date(consersation.getListMessageData().get(position).timestamp)));
                } else {
                    ((ItemMessageUserHolder) holder).txtTimeUser.setText(new SimpleDateFormat("MMM d").format(new Date(consersation.getListMessageData().get(position).timestamp)));
                }



            if (bitmapAvataUser != null) {
                ((ItemMessageUserHolder) holder).avata.setImageBitmap(bitmapAvataUser);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return consersation.getListMessageData().get(position).idSender.equals(StaticConfig.UID) ? ChatActivity.VIEW_TYPE_USER_MESSAGE : ChatActivity.VIEW_TYPE_FRIEND_MESSAGE;
    }

    @Override
    public int getItemCount() {
        return consersation.getListMessageData().size();
    }
}

class ItemMessageUserHolder extends RecyclerView.ViewHolder {
    public TextView txtContent;
    public TextView txtTimeUser;
    public CircleImageView avata;

    public ItemMessageUserHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentUser);
        txtTimeUser = (TextView) itemView.findViewById(R.id.txtTimeUser);
        avata = (CircleImageView) itemView.findViewById(R.id.imageView2);
    }
}

class ItemMessageFriendHolder extends RecyclerView.ViewHolder {
    public TextView txtContent;
    public TextView txtTimeFriend;
    public CircleImageView avata;

    public ItemMessageFriendHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentFriend);
        txtTimeFriend = (TextView) itemView.findViewById(R.id.txtTimeFriend);
        avata = (CircleImageView) itemView.findViewById(R.id.imageView3);
    }
}

