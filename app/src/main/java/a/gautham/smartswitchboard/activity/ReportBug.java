package a.gautham.smartswitchboard.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import a.gautham.smartswitchboard.databinding.ActivityReportBugBinding;
import a.gautham.smartswitchboard.helpers.ScreenShotAdapter;

public class ReportBug extends AppCompatActivity {

    private ActivityReportBugBinding binding;
    private List<String> imgs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportBugBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        int i = 1;
        while (i <= 5) {
            imgs.add(String.valueOf(i));
            i++;
        }

        binding.screenshotImgs.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.screenshotImgs.setLayoutManager(layoutManager);

        ScreenShotAdapter adapter = new ScreenShotAdapter(imgs);
        adapter.notifyDataSetChanged();
        binding.screenshotImgs.setAdapter(adapter);
    }
}