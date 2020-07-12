package a.gautham.smartswitchboard.navigation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yashhajare.ssb_connect.MainActivitySSB;

import java.lang.reflect.Type;
import java.util.ArrayList;

import a.gautham.smartswitchboard.R;
import a.gautham.smartswitchboard.databinding.NavigationHomeBinding;

public class Home extends Fragment {

    private NavigationHomeBinding binding;
    private boolean isFABOpen = false;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;

    SharedPreferences preferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = NavigationHomeBinding.inflate(inflater, container, false);

        fab_open = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_backward);

        binding.wificonfig.setOnClickListener(view -> animateFAB());

        binding.newConnectionContainer.setOnClickListener(view -> {
            startActivity(new Intent(getActivity(), MainActivitySSB.class));
        });

        preferences = requireActivity().getSharedPreferences("DB", Context.MODE_PRIVATE);

        ArrayList<String> fire_list = getArrayList("Firecode_DB_1");

        if (fire_list != null) {
            ArrayList<String> list = new ArrayList<>();
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>
                    (requireActivity(), R.layout.fire_button_layout, R.id.create_txt, list);
            binding.listView.setAdapter(arrayAdapter);

            for (int i = 0; i < fire_list.size(); i++) {
                String[] temp = fire_list.get(i).split(":"); //Finding Relay numbers
                int relay_counts = Integer.parseInt(temp[temp.length - 1]);

                for (int j = 0; j < relay_counts; j++) {
                    list.add(fire_list.get(i) + "Switch :" + j);
                }
                arrayAdapter.notifyDataSetChanged();
            }
        } else {
            binding.message.setText(R.string.not_connected_to_ssb);
            Toast.makeText(requireActivity(), getString(R.string.not_connected_to_ssb), Toast.LENGTH_SHORT).show();
        }

        return binding.getRoot();
    }

    public ArrayList<String> getArrayList(String key) {
        Gson gson = new Gson();
        String json = preferences.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    private void animateFAB() {

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

}
