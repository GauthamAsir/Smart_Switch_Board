package a.gautham.smartswitchboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class Splash extends AppCompatActivity {

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPreferences preferences = getSharedPreferences("User", Context.MODE_PRIVATE);
        String uid = preferences.getString("uid", "default");
        int theme = preferences.getInt("theme", 0);

        switch (theme) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        if (uid == null || uid.equals("default") || FirebaseAuth.getInstance().getCurrentUser() == null) {
            loginScreen();
            return;
        }

        FirebaseFirestore.getInstance().collection("Users")
                .whereEqualTo(FieldPath.documentId(), uid)
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (Objects.requireNonNull(task.getResult()).getDocuments().size() <= 0) {
                    loginScreen();
                } else {
                    Common.uid = uid;

                    handler.postDelayed(() -> {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }, 500);
                }
            }
        });

    }

    private void loginScreen() {
        handler.postDelayed(() -> {
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }, 500);
    }

}