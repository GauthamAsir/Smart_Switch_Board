package a.gautham.smartswitchboard;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

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

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Map<String, List<CurrentDevice>> currentDeviceMap = new HashMap<>();
    private boolean isFABOpen = false;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        getAccountInfo();

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_backward);

    }

    @Override
    protected void onStart() {
        super.onStart();

        binding.wificonfig.setOnClickListener(view -> animateFAB());

    }

    public void animateFAB() {

        if (isFABOpen) {
            binding.wificonfig.startAnimation(rotate_backward);
            binding.newConnectionContainer.startAnimation(fab_close);
            binding.shareCodeContainer.startAnimation(fab_close);
            isFABOpen = false;
        } else {
            binding.wificonfig.startAnimation(rotate_forward);
            binding.newConnectionContainer.startAnimation(fab_open);
            binding.shareCodeContainer.startAnimation(fab_open);
            isFABOpen = true;
        }
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

}