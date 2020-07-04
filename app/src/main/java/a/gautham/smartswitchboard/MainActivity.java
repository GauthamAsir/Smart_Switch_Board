package a.gautham.smartswitchboard;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import a.gautham.smartswitchboard.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }

    @Override
    protected void onStart() {
        super.onStart();

        binding.wificonfig.setOnClickListener(view -> {
            //Intent intent = new Intent(getApplicationContext(), EsptouchDemoActivity.class);
            //startActivity(intent);
        });

    }
}