package a.gautham.smartswitchboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class Splash extends AppCompatActivity {

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SharedPreferences preferences = getSharedPreferences("User", Context.MODE_PRIVATE);
        String uid = preferences.getString("uid", "default");

        if (uid == null || uid.equals("default")) {
            handler.postDelayed(() -> {
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }, 500);
            return;
        }

        Common.uid = uid;

        handler.postDelayed(() -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }, 500);

    }
}