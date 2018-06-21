package com.example.thuyenbu.uitchat;

import android.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.thuyenbu.uitchat.data.StaticConfig;
import com.example.thuyenbu.uitchat.service.ServiceUtils;
import com.example.thuyenbu.uitchat.ui.FriendRequestFragment;
import com.example.thuyenbu.uitchat.ui.FriendsFragment;
import com.example.thuyenbu.uitchat.ui.GroupFragment;
import com.example.thuyenbu.uitchat.ui.LoginActivity;
import com.example.thuyenbu.uitchat.ui.UserActivity;
import com.example.thuyenbu.uitchat.ui.UserProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    private ViewPager viewPager;
    private TabLayout tabLayout = null;
    public static String STR_FRIEND_FRAGMENT = "Bạn bè";
    public static String STR_GROUP_FRAGMENT = "Nhóm";
    public static String STR_INFO_FRAGMENT = "Thông tin";

    private FloatingActionButton floatButton;
    private ViewPagerAdapter adapter;
    private Toolbar mToolbar;
    private CoordinatorLayout layoutApp;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         mToolbar = (Toolbar) findViewById(R.id.toolbar);
         if(mToolbar != null) {
            setSupportActionBar(mToolbar);
            getSupportActionBar().setTitle("iChat");
         }

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        floatButton = (FloatingActionButton) findViewById(R.id.fab);
        layoutApp = (CoordinatorLayout)findViewById(R.id.bg_app);
        //layoutApp.setBackgroundResource(R.drawable.bg_start_app);

        if(CheckInternet())
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationManager mNotificationManager = (NotificationManager)getSystemService(
                        Context.NOTIFICATION_SERVICE);
                NotificationChannel infoChannel = new NotificationChannel("MY_CHANEL_01",
                        getString(R.string.notification), NotificationManager.IMPORTANCE_DEFAULT);
                infoChannel.setDescription(getString(R.string.notificationDes));
                infoChannel.enableLights(false);
                infoChannel.enableVibration(false);
                mNotificationManager.createNotificationChannel(infoChannel);
            }

            initTab();
            initFirebase();

            Intent notifyIntent = getIntent();
            String extras = getIntent().getStringExtra("KEY");;
            if (extras != null && extras.equals("YOUR VAL")) {
                floatButton.setOnClickListener(((FriendRequestFragment) adapter.getItem(2))
                        .onClickFloatButton.getInstance(this));
            }
        }
        else
        {
            Toast.makeText(this, "There is no Internet connection!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean CheckInternet()
    {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return  true;
        }

        return false;
    }

    private void initFirebase() {
        //Khoi tao thanh phan de dang nhap, dang ky
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    StaticConfig.UID = user.getUid();
                    StaticConfig.EMAIL = user.getEmail();
                } else {

                    MainActivity.this.finish();
                    // User is signed in
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                  //  Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    // Create 3 tab
    public void initTab() {
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorIndivateTab));
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
    }

    private void setupTabIcons() {
        int[] tabIcons = {
                R.drawable.ic_tab_person,
                R.drawable.ic_tab_group,
                R.drawable.ic_request_3,
                R.drawable.ic_tab_infor
        };

        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        tabLayout.getTabAt(3).setIcon(tabIcons[3]);
    }

    private void setupViewPager(final ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new FriendsFragment(), STR_FRIEND_FRAGMENT);
        adapter.addFrag(new GroupFragment(), STR_GROUP_FRAGMENT);
        adapter.addFrag(new FriendRequestFragment(), "danh sach ban be");
        adapter.addFrag(new UserProfileFragment(), STR_INFO_FRAGMENT);

        floatButton.setOnClickListener(((FriendsFragment) adapter.getItem(0)).onClickFloatButton.getInstance(this));

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                layoutApp.setBackgroundResource(android.R.color.transparent);
                ServiceUtils.stopServiceFriendChat(MainActivity.this.getApplicationContext(), false);
                if (adapter.getItem(position) instanceof FriendsFragment)
                {
                    floatButton.setVisibility(View.VISIBLE);
                    floatButton.setOnClickListener(((FriendsFragment) adapter.getItem(position)).onClickFloatButton.getInstance(MainActivity.this));
                    floatButton.setImageResource(R.drawable.ic_add_friend);
                    ((FriendsFragment) adapter.getItem(position)).onRefresh();
                }
                else if (adapter.getItem(position) instanceof GroupFragment)
                {
                    floatButton.setVisibility(View.VISIBLE);
                    floatButton.setOnClickListener(((GroupFragment) adapter.getItem(position)).onClickFloatButton.getInstance(MainActivity.this));
                    floatButton.setImageResource(R.drawable.ic_float_add_group);
                }
                else if (adapter.getItem(position) instanceof FriendRequestFragment)
                {
                    floatButton.setVisibility(View.GONE);
                    ((FriendRequestFragment) adapter.getItem(position)).onRefresh();
                }
                else {
                    floatButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        super.onOptionsItemSelected(item);

        int id = item.getItemId();

        if (id == R.id.btn_action_settings) {
            startActivity(new Intent(MainActivity.this, UserActivity.class));
        }

        if (id == R.id.btn_log_out) {
            FirebaseAuth.getInstance().signOut();
            MainActivity.this.finish();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }

        return true;
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        ServiceUtils.stopServiceFriendChat(getApplicationContext(), false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onDestroy() {
        ServiceUtils.startServiceFriendChat(getApplicationContext());
        super.onDestroy();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {

            // return null to display only the icon
            return null;
        }
    }
}
