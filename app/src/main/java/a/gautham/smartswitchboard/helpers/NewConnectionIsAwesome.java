package a.gautham.smartswitchboard.helpers;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.AsyncTask;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import a.gautham.smartswitchboard.R;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewConnectionIsAwesome extends Activity {
    public Context context;
    public Activity activity;
    public ListAdapterSSB adapter;
    public int Loca_request_code = 111;
    AlertDialog alertDialog_wifi_yes_no;


    public NewConnectionIsAwesome(Context our_context, Activity our_activity, ListAdapterSSB our_adapter) {
        this.context = our_context;
        this.activity = our_activity;
        this.adapter = our_adapter;
    }

    public void User_own_wifi_or_not() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View input = layoutInflater.inflate(R.layout.wifi_yes_or_no, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(input);
        Button wifi_yes = input.findViewById(R.id.wifi_yes);
        Button wifi_no = input.findViewById(R.id.wifi_no);

        wifi_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(context,"Please stay connected to Wi-Fi",Toast.LENGTH_SHORT).show();
                Connecting_to_SSB();
                alertDialog_wifi_yes_no.dismiss();
            }
        });
        wifi_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "This Feature will be available soon.", Toast.LENGTH_SHORT).show();
                alertDialog_wifi_yes_no.dismiss();
            }
        });
        alertDialog_wifi_yes_no = builder.create();
        alertDialog_wifi_yes_no.show();
    }

    private void Connecting_to_SSB() {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Loca_request_code);
        } else {
            if (!getLocationstate() || !getWifistate() || getMobileDataState()) {
                if (!getLocationstate()) {
                    Show_location_alert_dialog();
                } else if (!getWifistate()) {
                    Show_wifi_alert_dialog();
                } else if (getMobileDataState()) {
                    Show_mobile_data_alert_dialog();
                }
            } else {
                Toast.makeText(context, "Make sure 'Smart Switch Board' is powered up!", Toast.LENGTH_SHORT).show();

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    LayoutInflater layoutInflater = LayoutInflater.from(context);
                    View input = layoutInflater.inflate(R.layout.wifi_intent, null);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setView(input);
                    builder.setTitle("Auto-detect is not supported in this android version.");
                    builder.setPositiveButton("Open WiFi settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            if (getLocationstate() && !getMobileDataState()) {
                                context.startActivity(new Intent(Settings.Panel.ACTION_WIFI));
                                Scan_and_connect_to_SSB scan_and_connect_to_ssb = new Scan_and_connect_to_SSB();
                                scan_and_connect_to_ssb.execute();
                            } else {
                                if (!getLocationstate()) {
                                    Show_location_alert_dialog();
                                } else if (getMobileDataState()) {
                                    Show_mobile_data_alert_dialog();
                                }

                            }
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("This Android version supports Auto-Scan!");
                    builder.setMessage("Power up Smart Swtich Board! \n 'Auto-Scan': Automatically detects and connects to available Smart Switch Board.");
                    builder.setPositiveButton("Auto-Scan", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (getLocationstate() && getLocationstate() && !getMobileDataState()) {
                                Scan_and_connect_to_SSB scan_and_connect_to_ssb = new Scan_and_connect_to_SSB();
                                scan_and_connect_to_ssb.execute();
                            } else {
                                if (!getLocationstate()) {
                                    Show_location_alert_dialog();
                                } else if (!getWifistate()) {
                                    Show_wifi_alert_dialog();
                                } else if (getMobileDataState()) {
                                    Show_mobile_data_alert_dialog();
                                }
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.setNeutralButton("Manual mode", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            LayoutInflater layoutInflater = LayoutInflater.from(context);
                            View input = layoutInflater.inflate(R.layout.wifi_intent, null);
                            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setView(input);
                            builder.setTitle("Manual mode");
                            builder.setPositiveButton("Open WiFi settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    if (getLocationstate() && getLocationstate() && !getMobileDataState()) {
//                                        context.startActivity(new Intent(Settings.Panel.ACTION_WIFI));
                                        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                        Scan_and_connect_to_SSB scan_and_connect_to_ssb = new Scan_and_connect_to_SSB();
                                        scan_and_connect_to_ssb.execute();
                                    } else {
                                        if (!getLocationstate()) {
                                            Show_location_alert_dialog();
                                        } else if (!getWifistate()) {
                                            Show_wifi_alert_dialog();
                                        } else if (getMobileDataState()) {
                                            Show_mobile_data_alert_dialog();
                                        }
                                    }
                                    dialogInterface.dismiss();
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        }
    }

    public boolean getLocationstate() {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);    //locationManager.isLocationEnabled();
    }

    public boolean getMobileDataState() {
        try {
            TelephonyManager telephonyService = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Method getMobileDataEnabledMethod = Objects.requireNonNull(telephonyService).getClass().getDeclaredMethod("getDataEnabled");
            return (boolean) (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);
        } catch (Exception ex) {
            Log.e("MainActivity", "Error getting mobile data state", ex);
        }
        return false;
    }

    public boolean getWifistate() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public void Show_location_alert_dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Please Turn on Location.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void Show_wifi_alert_dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Please Turn on Wi-Fi.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void Show_mobile_data_alert_dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Attention! Please Disable Mobile Data");
        builder.setMessage("In order to Connect to Smart Switch Board, your 'mobile data' must be disabled.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //////////////////////////////////////////
    private void Display_available_wifi() {
        final List<ScanResult> scanResultList;

        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        scanResultList = wifiManager.getScanResults();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select your Wi-Fi:");

        String[] wifi_names = new String[scanResultList.size()];

        for (int i = 0; i < scanResultList.size(); i++) {
            wifi_names[i] = i + 1 + ". " + scanResultList.get(i).SSID;
        }

        builder.setItems(wifi_names, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (getLocationstate() && getWifistate() && !getMobileDataState()) {
                    Connect_to_user_wifi(scanResultList.get(i).SSID);
                    dialogInterface.dismiss();
                } else {
                    if (!getLocationstate()) {
                        Show_location_alert_dialog();
                    } else if (!getWifistate()) {
                        Show_wifi_alert_dialog();
                    } else if (getMobileDataState()) {
                        Show_mobile_data_alert_dialog();
                    }
                }
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setNeutralButton("Scan Wi-Fi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (getLocationstate() && getWifistate() && !getMobileDataState()) {
                    Display_available_wifi();
                    dialogInterface.dismiss();
                } else {
                    if (!getLocationstate()) {
                        Show_location_alert_dialog();
                    } else if (!getWifistate()) {
                        Show_wifi_alert_dialog();
                    } else if (getMobileDataState()) {
                        Show_mobile_data_alert_dialog();
                    }
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void Connect_to_user_wifi(String wifi_name) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View input = layoutInflater.inflate(R.layout.wifi_log_in, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(input);
        builder.setTitle("Enter WiFi credentials:");
        final TextView textView = input.findViewById(R.id.ssid_login);
        textView.setText(wifi_name);
        final EditText editText = input.findViewById(R.id.pass_login);
        builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (getLocationstate() && getWifistate() && !getMobileDataState()) {
                    Sending_wifi_ssid_pass_to_ssb sending_wifi_ssid_pass_to_ssb = new Sending_wifi_ssid_pass_to_ssb();
                    sending_wifi_ssid_pass_to_ssb.execute(textView.getText().toString(), editText.getText().toString());
//                    Toast.makeText(context,"Password is :" + editText.getText(),Toast.LENGTH_SHORT).show();
                } else {
                    if (!getLocationstate()) {
                        Show_location_alert_dialog();
                    } else if (!getWifistate()) {
                        Show_wifi_alert_dialog();
                    } else if (getMobileDataState()) {
                        Show_mobile_data_alert_dialog();
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    ///////////////////
    public void saveArrayList(ArrayList<ArrayList<String>> list, String key) {
        SharedPreferences prefs = context.getSharedPreferences("DB_temp", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }

    public ArrayList<ArrayList<String>> getArrayList(String key) {
        SharedPreferences prefs = context.getSharedPreferences("DB_temp", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<ArrayList<String>>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    ////////////////////////////
    public class Scan_and_connect_to_SSB extends AsyncTask<String, String, String> {
        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(context);
            p.setMessage("Please Wait....");
            p.setIndeterminate(false);
            p.setCancelable(false);
            p.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            int counts = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                if (getLocationstate() && getWifistate() && !getMobileDataState()) {
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);//remember id
                    while (!wifiManager.getConnectionInfo().getSSID().contains("SSB")) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        counts++;
                        if (counts >= 30) {
                            return "NOT";
                        } else if (!getLocationstate() || !getWifistate() || getMobileDataState()) {
                            return "NOLOCAWIFI";
                        }
                    }
                    return "OK";
                } else {
                    return "NOLOCAWIFI";
                }
            } else {
                if (getLocationstate() && getWifistate() && !getMobileDataState()) {
                    WifiConfiguration wifiConfig = new WifiConfiguration();
                    String ssid = "SSB";
                    String key = "87654321";

                    wifiConfig.SSID = String.format("\"%s\"", ssid);
                    wifiConfig.preSharedKey = String.format("\"%s\"", key);

                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);//remember id
                    while (!wifiManager.getConnectionInfo().getSSID().contains("SSB")) {
                        int netId = wifiManager.addNetwork(wifiConfig);
                        wifiManager.removeNetwork(netId);
                        int netIdd = wifiManager.addNetwork(wifiConfig);
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(netIdd, true);
                        wifiManager.reconnect();
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        counts++;
                        if (counts >= 5) {
                            return "NOT";
                        } else if (!getLocationstate() || !getWifistate() || getMobileDataState()) {
                            return "NOLOCAWIFI";
                        }
                    }
                } else {
                    return "NOLOCAWIFI";
                }
            }

            return "OK";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            switch (s) {
                case "OK":
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
                    if (wifiManager.getConnectionInfo().getSSID().contains("SSB")) {
                        Toast.makeText(context, "Connected!", Toast.LENGTH_SHORT).show();
                        Display_available_wifi();
                    } else {
                        Toast.makeText(context, "Not Detected! Try Again!", Toast.LENGTH_SHORT).show();
                    }
                    p.dismiss();
                    break;
                case "NOT":
                    Toast.makeText(context, "Not Detected! Try Again!", Toast.LENGTH_SHORT).show();
                    p.dismiss();
                    break;
                case "NOLOCAWIFI":
                    if (!getLocationstate()) {
                        Show_location_alert_dialog();
                    } else if (!getWifistate()) {
                        Show_wifi_alert_dialog();
                    } else if (getMobileDataState()) {
                        Show_mobile_data_alert_dialog();
                    }
                    p.dismiss();
                    break;
                default:
                    break;
            }
        }
    }

    public class Sending_wifi_ssid_pass_to_ssb extends AsyncTask<String, String, ArrayList<String>> {
        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(context);
            p.setMessage("Please Wait....");
            p.setIndeterminate(false);
            p.setCancelable(false);
            p.show();
        }

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            ArrayList<String> listt = new ArrayList<String>();

            if (getLocationstate() && getWifistate() && !getMobileDataState()) {
                OkHttpClient client = new OkHttpClient();
                String relay_find = "";
                String our_unique_code = "";
                String unid_code = String.valueOf(UUID.randomUUID());


                try {
                    ///////Getting Version of Smart Switch board
//                String url = "http://192.168.4.1/setting?ssid=Yashhh&pass=87654321";
                    String url = "http://192.168.4.1";
                    Log.d("JSON", "URL: " + url);
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Response response = client.newCall(request).execute();
                    relay_find = String.valueOf(response.body().source().readUtf8());
                    response.close();

                    //////////Sending SSID, pass and unique code to hardware
                    String url2 = "http://192.168.4.1/setting?ssid=" + strings[0] + "&pass=" + strings[1] + "&uuid=" + unid_code;
//                String url = "http://192.168.4.1:80";
                    Log.d("JSON", "URL: " + url2);
                    Request request2 = new Request.Builder()
                            .url(url2)
                            .build();
                    Response response2 = client.newCall(request2).execute();
                    response2.close();
                    our_unique_code = strings[0] + ":" + strings[1] + ":" + unid_code;

                    listt.add(relay_find);
                    listt.add(our_unique_code);
                    listt.add(strings[0]);
                    listt.add(strings[1]);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return listt;
            } else {
                return listt;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<String> list) {
            super.onPostExecute(list);
            p.dismiss();
            if (!list.isEmpty()) {
                String relay_find = list.get(0);
                String our_unique_code = list.get(1);

                int AA = relay_find.indexOf("[");
                int BB = relay_find.indexOf("]");
                String relay_found = relay_find.substring(AA + 1, BB);
                int relay_count = Integer.parseInt(relay_found);

                Log.d("hsay relay", String.valueOf(relay_count));
                Log.d("hsay unique id", our_unique_code);
                String our_fire_code = our_unique_code + ":" + relay_count; /// Real Time Firebase title
                Log.d("hsay Realtime fire id", our_fire_code);

                ArrayList<ArrayList<String>> db_list_final = new ArrayList<>();
                ArrayList<String> child_db_list_final = new ArrayList<>();
                child_db_list_final.add(our_fire_code);
                for (int i = 0; i < relay_count; i++) {
                    child_db_list_final.add("Switch " + (i + 1));
                }
                db_list_final.add(child_db_list_final);

                ArrayList<ArrayList<String>> arrayLists = getArrayList("fire_db_temp");

                if (arrayLists == null || arrayLists.isEmpty()) {
                    saveArrayList(db_list_final, "fire_db_temp");
                } else {
                    arrayLists.add(child_db_list_final);
                    saveArrayList(arrayLists, "fire_db_temp");
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                            .setSsid("SSB")
                            .setWpa2Passphrase("87654321")
                            .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                            .build();

                    List<WifiNetworkSuggestion> suggestionsList = new ArrayList<WifiNetworkSuggestion>();
                    suggestionsList.add(suggestion);
                    WifiManager wifiManager_our = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifiManager_our.removeNetworkSuggestions(suggestionsList);
                } else {
                    WifiConfiguration wifiConfig_temp = new WifiConfiguration();
                    String ssid_temp = "SSB";
                    String key_temp = "87654321";

                    wifiConfig_temp.SSID = String.format("\"%s\"", ssid_temp);
                    wifiConfig_temp.preSharedKey = String.format("\"%s\"", key_temp);

                    WifiManager wifiManager_temp = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);//remember id
                    int netId_temp = wifiManager_temp.addNetwork(wifiConfig_temp);
                    wifiManager_temp.removeNetwork(netId_temp);
                    int netIdd_temp = wifiManager_temp.addNetwork(wifiConfig_temp);
                    wifiManager_temp.disconnect();
                    //////////////Connecting to user WiFi router
                    WifiConfiguration wifiConfig = new WifiConfiguration();
                    String ssid = list.get(2);
                    String pass = list.get(3);
                    wifiConfig.SSID = String.format("\"%s\"", ssid);
                    wifiConfig.preSharedKey = String.format("\"%s\"", pass);
                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);//remember id
                    int netIdd = wifiManager.addNetwork(wifiConfig);
                    wifiManager.disconnect();
                    wifiManager.enableNetwork(netIdd, true);
                    wifiManager.reconnect();
                }

                /////////////Declaring firecode to firebase
                FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                for (int i = 1; i < child_db_list_final.size(); i++) {
                    DatabaseReference fire_refer = firebaseDatabase.getReference(child_db_list_final.get(0) + "/" + "Switch : " + i);
                    fire_refer.setValue(false);
                }

                adapter.Updating_list_adapter();
            } else {
                Toast.makeText(context, "This Smart Switch Board is already configured.", Toast.LENGTH_LONG).show();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    WifiNetworkSuggestion suggestion = new WifiNetworkSuggestion.Builder()
                            .setSsid("SSB")
                            .setWpa2Passphrase("87654321")
                            .setIsAppInteractionRequired(true) // Optional (Needs location permission)
                            .build();

                    List<WifiNetworkSuggestion> suggestionsList = new ArrayList<WifiNetworkSuggestion>();
                    suggestionsList.add(suggestion);
                    WifiManager wifiManager_our = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifiManager_our.removeNetworkSuggestions(suggestionsList);
                } else {
                    WifiConfiguration wifiConfig_temp = new WifiConfiguration();
                    String ssid_temp = "SSB";
                    String key_temp = "87654321";

                    wifiConfig_temp.SSID = String.format("\"%s\"", ssid_temp);
                    wifiConfig_temp.preSharedKey = String.format("\"%s\"", key_temp);

                    WifiManager wifiManager_temp = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);//remember id
                    int netId_temp = wifiManager_temp.addNetwork(wifiConfig_temp);
                    wifiManager_temp.removeNetwork(netId_temp);
                    int netIdd_temp = wifiManager_temp.addNetwork(wifiConfig_temp);
                    wifiManager_temp.disconnect();
                }
                if (!getLocationstate()) {
                    Show_location_alert_dialog();
                } else if (!getWifistate()) {
                    Show_wifi_alert_dialog();
                } else if (getMobileDataState()) {
                    Show_mobile_data_alert_dialog();
                }
            }
        }
    }
/////////////////
}


