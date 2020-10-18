package a.gautham.smartswitchboard.services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Map;
import java.util.Objects;

import a.gautham.smartswitchboard.Common;
import a.gautham.smartswitchboard.R;
import a.gautham.smartswitchboard.activity.Login;
import a.gautham.smartswitchboard.activity.Splash;

public class UserCheck extends Service {

    IBinder mBinder;
    FirebaseFirestore firestore;
    SharedPreferences userPreferences;

    @Override
    public void onCreate() {
        firestore = FirebaseFirestore.getInstance();
        userPreferences = getSharedPreferences("User", MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Common.uid.equals("default")) {
            FirebaseAuth.getInstance().signOut();
            Common.toastShort(getApplicationContext(), getString(R.string.something_went_wrong), Color.RED, Color.WHITE);
            logOut();
            startActivity(new Intent(getApplicationContext(), Login.class));
            ((Activity) getApplicationContext()).finish();
            return super.onStartCommand(intent, flags, startId);
        }

        firestore.collection("Users")
                .document(Common.uid).addSnapshotListener((document, error) -> {

            if (error != null) {
                Common.toastShort(getApplicationContext(), error.getMessage(), Color.RED, Color.WHITE);
                logOut();
                return;
            }

            if (document == null || document.get("phone_number") == null) {
                Common.toastShort(getApplicationContext(), getString(R.string.something_went_wrong), Color.RED, Color.WHITE);
                logOut();
                return;
            }

            String id = Common.DEVICE_ID;

            if (id != null && !id.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> devicesList = (Map<String, Map<String, Object>>) document.get("devices");
                if (devicesList != null) {

                    if (devicesList.get(id) != null && !(boolean) Objects.requireNonNull(devicesList.get(id)).get("logged_in")) {
                        Common.toastShort(getApplicationContext(), getString(R.string.logged_out_for_security_reasons), Color.RED, Color.WHITE);
                        logOut();
                        return;
                    }
                }
            }

            String pass = userPreferences.getString("password", "");

            if (pass != null && !pass.isEmpty() && document.get("password") != null) {
                if (!pass.equals(Objects.requireNonNull(document.get("password")).toString())) {
                    Common.toastShort(getApplicationContext(), getString(R.string.password_changed_recently_warning), Color.RED, Color.WHITE);
                    logOut();
                }
            }

        });

        return super.onStartCommand(intent, flags, startId);
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences userPreferences = getSharedPreferences("User", MODE_PRIVATE);
        int theme = userPreferences.getInt("theme", 0);
        userPreferences.edit().clear().apply();
        userPreferences.edit().putInt("theme", theme).apply();
        SharedPreferences prefs = getSharedPreferences("DB_temp", MODE_PRIVATE);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.uid);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.DEVICE_ID);
        prefs.edit().clear().apply();
        Common.uid = "default";
        Common.PHONE_NUMBER = "default";
        Common.EMAIL = "default";
        Common.NAME = "default";
        startActivity(new Intent(getApplicationContext(), Splash.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
