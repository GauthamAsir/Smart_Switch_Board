package a.gautham.smartswitchboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import a.gautham.library.AppUpdater;
import a.gautham.library.helper.Display;
import a.gautham.smartswitchboard.databinding.ActivityMainBinding;
import a.gautham.smartswitchboard.helpers.Utils;
import a.gautham.smartswitchboard.navigation.Home;
import a.gautham.smartswitchboard.navigation.SettingsActivity;
import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;
    private long back_pressed;
    private SharedPreferences userPreferences, dbPrefs;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.menu_home);

        firestore = FirebaseFirestore.getInstance();

        binding.navView.setNavigationItemSelectedListener(this);

        userPreferences = getSharedPreferences("User", MODE_PRIVATE);
        dbPrefs = getSharedPreferences("DB_temp", MODE_PRIVATE);

        dbPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        /*preferenceChangeListener = (sharedPreferences, s) -> {
            ArrayList<ArrayList<String>> ssbList = new Utils().getArrayList(getApplicationContext(), "fire_db_temp");
            firestore.collection("Users")
                    .document(Common.uid).update("ssb_list", ssbList);
        };*/

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) navigateToHome();

        boolean checkUpdate = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext()).getBoolean("check_update", true);

        String mail = userPreferences.getString("email", "");
        String name = userPreferences.getString("name", "");
        String phone = userPreferences.getString("phone_number", "");

        if (mail != null && !mail.isEmpty() && name != null && !name.isEmpty() && phone != null && !phone.isEmpty()) {
            Common.NAME = name;
            Common.EMAIL = mail;
            Common.PHONE_NUMBER = phone;
        }

        if (Common.getConnectivityStatus(getApplicationContext())) {
            if (checkUpdate) {
                AppUpdater appUpdater = new AppUpdater(this);
                appUpdater.setDisplay(Display.DIALOG);
                appUpdater.setUpGithub("GauthamAsir", "Smart_Switch_Board");
                appUpdater.start();
            }

        } else {
            Common.toastShort(getApplicationContext(), "No Internet Connection", 0, 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    void navigateToHome() {

        Bundle args = new Bundle();
        if (getIntent().getData() != null) args.putString("data", getIntent().getData().toString());
        if (getIntent().getExtras() != null) args.putAll(getIntent().getExtras());

        Home home = new Home();
        home.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, home).commit();

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.menu_home);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Common.SETTINGS_ENABLED = Common.getConnectivityStatus(getApplicationContext());
        if (!Common.getConnectivityStatus(getApplicationContext())) {
            Common.toastShort(getApplicationContext(), "No Internet Connection", 0, 0);
        } else {
            checkUserData();
        }
    }

    private void setHeaderName() {
        View headerView = binding.navView.getHeaderView(0);
        final TextView t1 = headerView.findViewById(R.id.header_txt);
        t1.setText(String.format(Locale.getDefault(), "Hello, %s", Common.NAME));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_home:
            default:
                navigateToHome();
                break;
            case R.id.nav_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                binding.navView.post(() -> binding.navView.setCheckedItem(R.id.nav_home));
                break;
            case R.id.nav_logout:

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.confirmation);
                builder.setMessage(R.string.really_want_to_log_out);

                builder.setCancelable(true);

                builder.setPositiveButton("Yes", (dialogInterface, i) -> {

                    android.app.AlertDialog alertDialog = new SpotsDialog.Builder()
                            .setContext(this)
                            .setCancelable(false)
                            .setMessage("Logging out...")
                            .setTheme(R.style.DialogCustom)
                            .build();

                    alertDialog.show();

                    logOut();

                    new Handler().postDelayed(() -> startActivity(new Intent(getApplicationContext(), Login.class)), 1000);

                });

                builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(
                        getApplicationContext(), R.color.colorAccent));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(
                        getApplicationContext(), R.color.colorAccent));
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        userPreferences.edit().clear().apply();
        SharedPreferences prefs = getSharedPreferences("DB_temp", MODE_PRIVATE);
        prefs.edit().clear().apply();
        Common.uid = "default";
        Common.PHONE_NUMBER = "default";
        Common.EMAIL = "default";
        Common.NAME = "default";
    }

    @Override
    public void onBackPressed() {

        //Snackbar
        Snackbar snackbar = Snackbar.make(binding.drawerLayout, "Press Again to Exit", Snackbar.LENGTH_SHORT);

        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else if (!binding.navView.getMenu().findItem(R.id.nav_home).isChecked()) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Home()).commit();
            binding.navView.setCheckedItem(R.id.nav_home);
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.menu_home);
        } else {
            if (back_pressed + 2000 > System.currentTimeMillis()) {
                finish();
                moveTaskToBack(true);
                System.exit(1);
                android.os.Process.killProcess(android.os.Process.myPid());
                finish();
            } else {
                snackbar.setText("Press Again to Exit");
                snackbar.show();
                back_pressed = System.currentTimeMillis();
            }
        }
    }

    private void checkUserData() {
        new Thread(() -> {
            if (Common.uid.equals("default")) {
                FirebaseAuth.getInstance().signOut();
                runOnUiThread(() -> {
                    Common.toastShort(getApplicationContext(), "Something went wrong", Color.RED, Color.WHITE);
                    logOut();
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    finish();
                });
                return;
            }

            DocumentReference docRef = firestore.collection("Users")
                    .document(Common.uid);

            docRef.get()
                    .addOnCompleteListener(task -> {

                        DocumentSnapshot document = task.getResult();

                        if (!task.isSuccessful() || document == null || document.get("phone_number") == null) {
                            FirebaseAuth.getInstance().signOut();
                            runOnUiThread(() -> {
                                Common.toastShort(getApplicationContext(), "Something went wrong", Color.RED, Color.WHITE);
                                logOut();
                                startActivity(new Intent(getApplicationContext(), Login.class));
                                finish();
                            });
                            return;
                        }

                        boolean syncDbList = userPreferences.getBoolean("sync", false);

                        if (syncDbList && document.get("ssb_list") != null) {
                            ArrayList<String> arrayList = (ArrayList<String>) Objects.requireNonNull(document.get("ssb_list"));
                            ArrayList<ArrayList<String>> ssbList = new ArrayList<>();
                            for (String s : arrayList) {
                                ArrayList<String> myList = new ArrayList<>(Arrays.asList(s.split(",")));
                                ssbList.add(myList);
                            }
                            if (ssbList != null || ssbList.size() > 0) {
                                new Utils().saveArrayList(getApplicationContext(), ssbList, "fire_db_temp");
                            }
                        }

                        String mail = userPreferences.getString("email", "");
                        String name = userPreferences.getString("name", "");
                        String phone = userPreferences.getString("phone_number", "");
                        String pass = userPreferences.getString("password", "");

                        if (mail == null || mail.isEmpty() || name == null || name.isEmpty() || phone == null || phone.isEmpty()) {

                            Common.NAME = Objects.requireNonNull(document.get("name")).toString();
                            Common.EMAIL = Objects.requireNonNull(document.get("email")).toString();
                            Common.PHONE_NUMBER = Objects.requireNonNull(document.get("phone_number")).toString();

                            userPreferences.edit().putString("email", Common.EMAIL).apply();
                            userPreferences.edit().putString("name", Common.NAME).apply();
                            userPreferences.edit().putString("phone_number", Common.PHONE_NUMBER).apply();

                        } else {
                            Common.NAME = name;
                            Common.EMAIL = mail;
                            Common.PHONE_NUMBER = phone;
                        }
                        setHeaderName();

                        if (pass != null && !pass.isEmpty() && document.get("password") != null) {
                            if (!pass.equals(Objects.requireNonNull(document.get("password")).toString())) {
                                FirebaseAuth.getInstance().signOut();
                                runOnUiThread(() -> {
                                    userPreferences.edit().clear().apply();
                                    Common.uid = "default";
                                    Common.PHONE_NUMBER = "default";
                                    Common.EMAIL = "default";
                                    Common.NAME = "default";
                                    Common.toastLong(getApplicationContext(),
                                            "Your password has been changed recently, re-login to continue", Color.RED, Color.WHITE);
                                    startActivity(new Intent(getApplicationContext(), Login.class));
                                    finish();
                                });
                            }
                        }

                    });
        }).start();
    }

}