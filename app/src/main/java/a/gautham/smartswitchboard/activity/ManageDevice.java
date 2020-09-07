package a.gautham.smartswitchboard.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import a.gautham.smartswitchboard.Common;
import a.gautham.smartswitchboard.R;
import a.gautham.smartswitchboard.databinding.ActivityManageDeviceBinding;
import a.gautham.smartswitchboard.helpers.DeviceAdapter;

public class ManageDevice extends AppCompatActivity {

    private ActivityManageDeviceBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageDeviceBinding.inflate(getLayoutInflater());
        View v = binding.getRoot();
        setContentView(v);

        Toolbar toolBar = findViewById(R.id.toolbar);
        toolBar.setTitle(R.string.manage_devices);
        toolBar.setNavigationOnClickListener(view -> onBackPressed());

        binding.devicesList.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.devicesList.setLayoutManager(layoutManager);

        FirebaseFirestore.getInstance().collection("Users").document(Common.uid).addSnapshotListener((value, error) -> {
            if (value == null) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> tempMap = (Map<String, Map<String, Object>>) value.get("devices");

                List<Map<String, Object>> list = new ArrayList<>();
                Map<String, Map<String, Object>> map = new HashMap<>();

                if (tempMap != null) {

                    for (Map.Entry<String, Map<String, Object>> entry : tempMap.entrySet()) {
                        if ((boolean) Objects.requireNonNull(tempMap.get(entry.getKey())).get("logged_in")) {
                            list.add(entry.getValue());
                            map.put(entry.getKey(), entry.getValue());
                        }
                    }
                }

                DeviceAdapter adapter = new DeviceAdapter(list, map);
                adapter.notifyDataSetChanged();
                binding.devicesList.setAdapter(adapter);

                if (list.size() <= 0) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.message.setVisibility(View.VISIBLE);
                    binding.devicesList.setVisibility(View.GONE);
                    return;
                }

                binding.progressBar.setVisibility(View.GONE);
                binding.message.setVisibility(View.GONE);
                binding.devicesList.setVisibility(View.VISIBLE);
            }
        });

    }
}