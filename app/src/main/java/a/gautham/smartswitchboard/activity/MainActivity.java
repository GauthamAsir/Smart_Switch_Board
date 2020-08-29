package a.gautham.smartswitchboard.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

import a.gautham.library.AppUpdater;
import a.gautham.library.helper.Display;
import a.gautham.smartswitchboard.Common;
import a.gautham.smartswitchboard.R;
import a.gautham.smartswitchboard.databinding.ActivityMainBinding;
import a.gautham.smartswitchboard.helpers.ListAdapterSSB;
import a.gautham.smartswitchboard.helpers.NewConnectionIsAwesome;
import a.gautham.smartswitchboard.helpers.SharingIsCaringSSB;
import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, ListAdapterSSB.customButtonListener {

    private ActivityMainBinding binding;
    private long back_pressed;
    static boolean Fire_persistence_state = false;
    private FirebaseFirestore firestore;
    public int Cam_request_code = 321;
    public int localRequestCode = 111;
    public Uri data;
    ListAdapterSSB adapter;
    ArrayList<String> original_fire_list = new ArrayList<>();
    ArrayList<String> original_name_list = new ArrayList<>();
    ArrayList<ArrayList<String>> fire_list;
    boolean sync, check_deep_link = true;
    private SharedPreferences userPreferences;
    private boolean isFABOpen = false;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;

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

        String uid = Objects.requireNonNull(userPreferences.getString("uid", ""));

        if (!uid.isEmpty()) Common.uid = uid;

        sync = userPreferences.getBoolean("sync", false);
        if (sync) {
            binding.gridview.setVisibility(View.GONE);
            binding.syncContainer.setVisibility(View.VISIBLE);
            binding.fabBts.setVisibility(View.GONE);
            binding.message.setVisibility(View.GONE);
        } else {
            binding.gridview.setVisibility(View.VISIBLE);
            binding.syncContainer.setVisibility(View.GONE);
            binding.fabBts.setVisibility(View.VISIBLE);
            getList();
        }

        binding.ignore.setOnClickListener(v -> {
            binding.syncContainer.setVisibility(View.GONE);
            binding.gridview.setVisibility(View.VISIBLE);
            binding.fabBts.setVisibility(View.VISIBLE);
            binding.message.setVisibility(View.VISIBLE);
            sync = false;
            userPreferences.edit().putBoolean("sync", false).apply();
            getList();
        });

        binding.sync.setOnClickListener(v -> {
            syncSSBList(uid);
            binding.syncContainer.setVisibility(View.GONE);
            binding.gridview.setVisibility(View.VISIBLE);
            binding.fabBts.setVisibility(View.VISIBLE);
            binding.message.setVisibility(View.VISIBLE);
            sync = false;
            userPreferences.edit().putBoolean("sync", false).apply();
            getList();
        });

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

        binding.wificonfig.setOnClickListener(v -> animateFAB());

        binding.newConnectionContainer.setOnClickListener(v -> {

            animateFAB();

            NewConnectionIsAwesome new_connection_is_awesome =
                    new NewConnectionIsAwesome(this, this, adapter);
            new_connection_is_awesome.User_own_wifi_or_not();
        });

        binding.shareCodeContainer.setOnClickListener(v -> {
            animateFAB();
            SharingIsCaringSSB sharingIsCaringSSB = new SharingIsCaringSSB(MainActivity.this, MainActivity.this, adapter);
            sharingIsCaringSSB.getting_secret_code();
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String s = bundle.getString("SUCCESS");
            if (s != null) {
                if (s.equals("SUCCESS")) {
                    new Common().Successful_alertdialog(MainActivity.this);
                    Toast.makeText(getApplicationContext(), R.string.connected_successfully, Toast.LENGTH_LONG).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        WifiManager wifiManager = (WifiManager) getApplicationContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        String current_wifi_ssid = wifiManager.getConnectionInfo().getSSID();
                        if (current_wifi_ssid.contains("SSB")) {
                            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(R.string.please_disconnect_ssb);
                            builder.setCancelable(false);
                            builder.setPositiveButton(R.string.open_wifi_settings, (dialogInterface, i) -> {
                                startActivity(new Intent(Settings.Panel.ACTION_WIFI));
                                dialogInterface.dismiss();
                            });
                            android.app.AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }
                    }
                }
            }
        }

        if (!Fire_persistence_state) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Fire_persistence_state = true;
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

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
                appUpdater.setUpGithub(getString(R.string.git_username), getString(R.string.git_repo_name));
                appUpdater.start();
            }

        } else {
            Common.toastShort(getApplicationContext(), getString(R.string.no_interent), 0, 0);
        }

    }

    private void getList() {
        fire_list = getArrayList("fire_db_temp");

        if (fire_list == null || fire_list.size() <= 0) {
            binding.message.setVisibility(View.VISIBLE);
            binding.message.setText(R.string.not_connected_to_ssb);
            Toast.makeText(getApplicationContext(), getString(R.string.not_connected_to_ssb), Toast.LENGTH_SHORT).show();
        } else {
            binding.message.setVisibility(View.GONE);
            Start_Custom_adaptor();
        }
    }

    private void syncSSBList(String uid) {

        SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

        SharedPreferences prefs = getSharedPreferences("DB_temp", MODE_PRIVATE);
        preferenceChangeListener = (sharedPreferences, s) -> {
            if (fire_list != null) fire_list.clear();
            getList();
        };
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        DocumentReference docRef = FirebaseFirestore.getInstance().collection("Users")
                .document(uid);

        docRef.get().addOnCompleteListener(task -> {

            DocumentSnapshot document = task.getResult();

            if (task.isSuccessful() && document != null) {
                if (document.get("ssb_list") != null) {
                    ArrayList<String> arrayList = (ArrayList<String>) Objects.requireNonNull(document.get("ssb_list"));
                    ArrayList<ArrayList<String>> ssbList = new ArrayList<>();
                    if (arrayList.size() > 0) {
                        for (String s : arrayList) {
                            ArrayList<String> myList = new ArrayList<>(Arrays.asList(s.split(",")));
                            ssbList.add(myList);
                        }
                        new Common().saveArrayList(MainActivity.this, ssbList, "fire_db_temp");
                        prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
                    }
                }
            } else {
                Common.toastLong(getApplicationContext(), getString(R.string.unable_to_sync_old_data), 0, 0);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent deep_Intent = getIntent();
        data = deep_Intent.getData();
        if (data != null && check_deep_link) {

            if (data.toString().contains("https://smart.switch.board/")) {

                SharingIsCaringSSB shaing_is_caring = new SharingIsCaringSSB(MainActivity.this, MainActivity.this, adapter);
                shaing_is_caring.adding_secret_key_from_scaned_qr_copy_paste_deep_link(data.toString());
                check_deep_link = false;
            } else {
                Toast.makeText(this, R.string.invalid_link, Toast.LENGTH_SHORT).show();
            }
        }

        Common.SETTINGS_ENABLED = Common.getConnectivityStatus(getApplicationContext());
        if (!Common.getConnectivityStatus(getApplicationContext())) {
            Common.toastShort(getApplicationContext(), getString(R.string.no_interent), 0, 0);
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
                binding.drawerLayout.closeDrawer(GravityCompat.START);
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

                builder.setPositiveButton(R.string.yes, (dialogInterface, i) -> {

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

                builder.setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.dismiss());

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

        //SnackBar
        Snackbar snackbar = Snackbar.make(binding.drawerLayout, R.string.press_again_to_exit, Snackbar.LENGTH_SHORT);

        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (back_pressed + 2000 > System.currentTimeMillis()) {
                finish();
                System.exit(1);
            } else {
                snackbar.setText(R.string.press_again_to_exit);
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
                    Common.toastShort(getApplicationContext(), getString(R.string.something_went_wrong), Color.RED, Color.WHITE);
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
                                Common.toastShort(getApplicationContext(), getString(R.string.something_went_wrong), Color.RED, Color.WHITE);
                                logOut();
                                startActivity(new Intent(getApplicationContext(), Login.class));
                                finish();
                            });
                            return;
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
                                            getString(R.string.password_changed_recently_warning), Color.RED, Color.WHITE);
                                    startActivity(new Intent(getApplicationContext(), Login.class));
                                    finish();
                                });
                            }
                        }

                    });
        }).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(getApplicationContext(), R.string.please_scan_valid_qr, Toast.LENGTH_SHORT).show();
            } else {
                if (result.getContents().contains("https://smart.switch.board/")) {
                    SharingIsCaringSSB shaing_is_caring = new SharingIsCaringSSB(MainActivity.this
                            , MainActivity.this, adapter);
                    shaing_is_caring.adding_secret_key_from_scaned_qr_copy_paste_deep_link(result.getContents());
                } else {
                    Toast.makeText(getApplicationContext(), R.string.please_scan_valid_qr, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.please_scan_valid_qr, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Cam_request_code) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission Granted!", Toast.LENGTH_SHORT).show();
                SharingIsCaringSSB shaing_is_caring = new SharingIsCaringSSB(MainActivity.this
                        , MainActivity.this, adapter);
                shaing_is_caring.openScanner();
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied! Please Allow Permission.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == localRequestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void Start_Custom_adaptor() {

        for (int i = 0; i < fire_list.size(); i++) {
            int sizeee = fire_list.get(i).size();

            for (int j = 1; j < sizeee; j++) {
                if (!fire_list.get(i).get(j).equals("BLANK")) {
                    original_fire_list.add(fire_list.get(i).get(0) + "/" + "Switch : " + j);
                    original_name_list.add(fire_list.get(i).get(j));
                }
            }
        }

        adapter = new ListAdapterSSB(MainActivity.this, original_fire_list, original_name_list);
        adapter.setCustomButtonListner(this);
        binding.gridview.setAdapter(adapter);

    }

    private void animateFAB() {

        if (isFABOpen) {
            binding.wificonfig.startAnimation(rotate_backward);
            binding.newConnectionContainer.startAnimation(fab_close);
            binding.shareCodeContainer.startAnimation(fab_close);
            isFABOpen = false;
            binding.newConnectionContainer.setEnabled(false);
            binding.shareCodeContainer.setEnabled(false);
        } else {
            binding.newConnectionContainer.setEnabled(true);
            binding.shareCodeContainer.setEnabled(true);
            binding.wificonfig.startAnimation(rotate_forward);
            binding.newConnectionContainer.startAnimation(fab_open);
            binding.shareCodeContainer.startAnimation(fab_open);
            isFABOpen = true;
        }
    }

    public ArrayList<ArrayList<String>> getArrayList(String key) {
        SharedPreferences prefs = getSharedPreferences("DB_temp", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<ArrayList<String>>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    @Override
    public void onButtonClickListener(int position, String value) {

    }

    @Override
    public void onToggleClickListener(int position, String value, Boolean ans) {

    }

}