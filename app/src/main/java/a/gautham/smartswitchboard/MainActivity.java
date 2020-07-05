package a.gautham.smartswitchboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import a.gautham.smartswitchboard.databinding.ActivityMainBinding;
import a.gautham.smartswitchboard.models.CurrentDevice;
import a.gautham.smartswitchboard.navigation.Home;
import a.gautham.smartswitchboard.navigation.Settings;
import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private Map<String, List<CurrentDevice>> currentDeviceMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.overlayBackground));

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.menu_home);

        binding.navView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Home()).commit();
            binding.navView.setCheckedItem(R.id.nav_home);
        }

        getAccountInfo();
    }

    private void getAccountInfo() {

        new Thread(() -> {

            if (Common.uid.equals("default")) {
                FirebaseAuth.getInstance().signOut();
                runOnUiThread(() -> {
                    Common.toastShort(getApplicationContext(), "Something went wrong", Color.RED, Color.WHITE);
                    startActivity(new Intent(getApplicationContext(), Login.class));
                });
                return;
            }

            DocumentReference docRef = FirebaseFirestore.getInstance().collection("Users")
                    .document(Common.uid);

            docRef.get()
                    .addOnCompleteListener(task -> {

                        DocumentSnapshot document = task.getResult();

                        if (!task.isSuccessful() || document == null) {
                            FirebaseAuth.getInstance().signOut();
                            runOnUiThread(() -> {
                                Common.toastShort(getApplicationContext(), "Something went wrong", Color.RED, Color.WHITE);
                                startActivity(new Intent(getApplicationContext(), Login.class));
                            });
                            return;
                        }

                        Common.NAME = Objects.requireNonNull(document.get("name")).toString();
                        Common.EMAIL = Objects.requireNonNull(document.get("email")).toString();
                        Common.PHONE_NUMBER = Objects.requireNonNull(document.get("phone_number")).toString();

                        View headerView = binding.navView.getHeaderView(0);
                        final TextView t1 = headerView.findViewById(R.id.header_txt);
                        t1.setText(String.format(Locale.getDefault(), "Hello, %s", Common.NAME));

                        currentDeviceMap = (Map<String, List<CurrentDevice>>) document.get("current_device");

                        Date d = new Date();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        String date = dateFormat.format(d);

                        if (currentDeviceMap.containsKey(date)) {
                            List<CurrentDevice> list = currentDeviceMap.get(date);
                            if (list != null) {
                                list.add(new CurrentDevice(Build.BRAND, Build.MODEL,
                                        String.valueOf(Build.VERSION.SDK_INT)));
                                currentDeviceMap.put(date, list);

                                FirebaseFirestore.getInstance().collection("Users")
                                        .document(Common.uid)
                                        .update("current_device", currentDeviceMap);

                            }
                        } else {
                            List<CurrentDevice> currentDeviceList = new ArrayList<>();
                            currentDeviceList.add(new CurrentDevice(Build.BRAND, Build.MODEL,
                                    String.valueOf(Build.VERSION.SDK_INT)));
                            currentDeviceMap.put(date, currentDeviceList);
                            FirebaseFirestore.getInstance().collection("Users")
                                    .document(Common.uid)
                                    .update("current_device", currentDeviceMap);
                        }

                    });

        }).start();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Home()).commit();
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.menu_home);
                break;
            case R.id.nav_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Settings()).commit();
                Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.menu_settings);
                break;
            case R.id.nav_logout:

                android.app.AlertDialog alertDialog = new SpotsDialog.Builder()
                        .setContext(this)
                        .setCancelable(false)
                        .setMessage("Logging out...")
                        .setTheme(R.style.DialogCustom)
                        .build();

                alertDialog.show();

                FirebaseAuth.getInstance().signOut();
                SharedPreferences preferences = getSharedPreferences("User", Context.MODE_PRIVATE);
                preferences.edit().clear().apply();
                Common.uid = "default";
                Common.PHONE_NUMBER = "default";
                Common.EMAIL = "default";
                Common.NAME = "default";

                new Handler().postDelayed(() -> startActivity(new Intent(getApplicationContext(), Login.class)), 1000);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}