package a.gautham.smartswitchboard.navigation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Type;
import java.util.ArrayList;

import a.gautham.smartswitchboard.R;
import a.gautham.smartswitchboard.databinding.NavigationHomeBinding;
import a.gautham.smartswitchboard.helpers.ListAdapterSSB;
import a.gautham.smartswitchboard.helpers.NewConnectionIsAwesome;
import a.gautham.smartswitchboard.helpers.SharingIsCaringSSB;
import a.gautham.smartswitchboard.helpers.Utils;

import static android.content.Context.MODE_PRIVATE;

public class Home extends Fragment implements ListAdapterSSB.customButtonListener {

    private NavigationHomeBinding binding;
    private boolean isFABOpen = false;
    private Animation fab_open, fab_close, rotate_forward, rotate_backward;

    SharedPreferences preferences;

    static boolean Fire_persistence_state = false;
    public int Cam_request_code = 321;
    public int Loca_request_code = 111;
    public String data;
    Utils utils = new Utils();
    ListAdapterSSB adapter;
    ArrayList<String> original_fire_list = new ArrayList<String>();
    ArrayList<String> original_name_list = new ArrayList<String>();
    ArrayList<String> temp_fire_list = new ArrayList<String>();
    ArrayList<String> temp_name_list = new ArrayList<String>();
    ArrayList<ArrayList<String>> fire_list;
    GridView listView;
    Boolean check_deep_link = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = NavigationHomeBinding.inflate(inflater, container, false);

        fab_open = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getActivity(), R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_backward);

        listView = binding.gridview;

        binding.wificonfig.setOnClickListener(view -> animateFAB());

        binding.newConnectionContainer.setOnClickListener(view -> {

            animateFAB();

            NewConnectionIsAwesome new_connection_is_awesome =
                    new NewConnectionIsAwesome(requireActivity(), requireActivity(), adapter);
            new_connection_is_awesome.User_own_wifi_or_not();
        });

        binding.shareCodeContainer.setOnClickListener(view -> {
            animateFAB();
            SharingIsCaringSSB sharingIsCaringSSB = new SharingIsCaringSSB(requireActivity(),
                    requireActivity(), adapter);
            sharingIsCaringSSB.getting_secret_code();
        });

        if (!Fire_persistence_state) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            Fire_persistence_state = true;
        }

        Bundle bundle = getArguments();
        if (bundle != null) {
            String s = bundle.getString("SUCCESS");
            if (s != null) {
                if (s.equals("SUCCESS")) {
                    utils.Successful_alertdialog(requireActivity());
                    Toast.makeText(requireActivity(), "Connected Successfully!", Toast.LENGTH_LONG).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        WifiManager wifiManager = (WifiManager) requireActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        String current_wifi_ssid = wifiManager.getConnectionInfo().getSSID();
                        if (current_wifi_ssid.contains("SSB")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                            builder.setTitle("Please Disconnect 'SSB'");
                            builder.setCancelable(false);
                            builder.setPositiveButton("Open WiFi settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(new Intent(Settings.Panel.ACTION_WIFI));
                                    dialogInterface.dismiss();
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }
                    }
                }
            }
        }

        fire_list = getArrayList("fire_db_temp");

        if (fire_list == null || fire_list.isEmpty()) {
            binding.message.setVisibility(View.VISIBLE);
            binding.message.setText(R.string.not_connected_to_ssb);
            Toast.makeText(requireActivity(), getString(R.string.not_connected_to_ssb), Toast.LENGTH_SHORT).show();
        } else {
            binding.message.setVisibility(View.GONE);
            Start_Custom_adaptor();
        }

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        if (bundle != null) data = bundle.getString("data");
        if (data != null && check_deep_link) {

            if (data.contains("https://smart.switch.board/")) {

                SharingIsCaringSSB shaing_is_caring = new SharingIsCaringSSB(requireActivity(), requireActivity(), adapter);
                shaing_is_caring.adding_secret_key_from_scaned_qr_copy_paste_deep_link(data);
                check_deep_link = false;
            } else {
                Toast.makeText(requireActivity(), "Invalid link!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(requireActivity(), "Please Scan valid QR code", Toast.LENGTH_SHORT).show();
            } else {
                if (result.getContents().contains("https://smart.switch.board/")) {
                    SharingIsCaringSSB shaing_is_caring = new SharingIsCaringSSB(requireActivity()
                            , requireActivity(), adapter);
                    shaing_is_caring.adding_secret_key_from_scaned_qr_copy_paste_deep_link(result.getContents());
                } else {
                    Toast.makeText(requireActivity(), "Please Scan valid QR code", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(requireActivity(), "Please Scan valid QR code", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Cam_request_code) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireActivity(), "Permission Granted!", Toast.LENGTH_SHORT).show();
                SharingIsCaringSSB shaing_is_caring = new SharingIsCaringSSB(requireActivity()
                        , requireActivity(), adapter);
                shaing_is_caring.openScanner();
            } else {
                Toast.makeText(requireActivity(), "Permission Denied! Please Allow Permission.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == Loca_request_code) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireActivity(), "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireActivity(), "Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void Start_Custom_adaptor() {

//        for (int i = 0; i < fire_list.size(); i++){
//            int sizeee = fire_list.get(i).size();
//            for (int j = 1; j < sizeee; j++){
//                temp_fire_list.add(fire_list.get(i).get(0) + "/" + "Switch : " + String.valueOf(j));
//                temp_name_list.add(fire_list.get(i).get(j));
//            }
//        }

        for (int i = 0; i < fire_list.size(); i++) {
//                String[] temp = fire_list.get(i)[0].split(":"); //Finding Relay numbers
//                int relay_counts = Integer.parseInt(temp[temp.length - 1]);
            int sizeee = fire_list.get(i).size();

//                for (int j = 0; j < relay_counts; j++) {
//                    list.add(fire_list.get(i) + "/" + "Switch : " + String.valueOf(j + 1));
//                }
            for (int j = 1; j < sizeee; j++) {
                if (!fire_list.get(i).get(j).equals("BLANK")) {
                    original_fire_list.add(fire_list.get(i).get(0) + "/" + "Switch : " + j);
                    original_name_list.add(fire_list.get(i).get(j));
                }
            }
//                adapter.notifyDataSetChanged();
        }

        adapter = new ListAdapterSSB(requireActivity(), original_fire_list, original_fire_list.size(), original_name_list);
        adapter.setCustomButtonListner(this);
        listView.setAdapter(adapter);

    }

    public ArrayList<ArrayList<String>> getArrayList(String key) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("DB_temp", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<ArrayList<String>>>() {
        }.getType();
        return gson.fromJson(json, type);
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

    @Override
    public void onButtonClickListner(int position, String value) {

    }

    @Override
    public void onToggleClickListner(int position, String value, Boolean ans) {

    }
}
