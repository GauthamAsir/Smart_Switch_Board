package com.yashhajare.ssb_connect;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivitySSB extends AppCompatActivity {

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ssb);

        TextView t1 = findViewById(R.id.scan_detected_connected);

        WifiConfiguration wifiConfig = new WifiConfiguration();
        String ssid = "SSB";
        String key = "87654321";
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", key);
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);//remember id
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.removeNetwork(netId);

        Async_wifi_scan wifi_scan_ssb = new Async_wifi_scan();
        wifi_scan_ssb.execute();

//        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager.getConnectionInfo().getSSID().contains("SSB")) {
            t1.setText("Connected to SSB");
        } else {
            t1.setText("Not Connected");
        }

        t1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), HomeSSB.class);
                startActivity(i);
            }
        });


//        List<ScanResult> results = null;
//        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//        TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
//        results = wifiManager.getScanResults();
//        int len = results.size();
//
//        for (int i=0; i < len; i++) {
//            if (results.get(i).SSID.equals("SSB")) {
//                scan_detect_connect.setText("Detected");
//            }
//            else {
//                scan_detect_connect.setText("Scanning");
//            }
//        }

//        List<ScanResult> results = null;
//        TextView outputs = (TextView)findViewById(R.id.wifi_scanned);
//        TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
//
//        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//        results = wifiManager.getScanResults();
//
//        int len = results.size();
//        String res = null;
//        for (int i = 0; i < len; i++) {
//            res = outputs.getText() + "\n" + results.get(i).SSID;
//            outputs.setText(res);
//            if (results.get(i).SSID.equals("SSB")){
//                scan_detect_connect.setText("Detected");
//            }
//        }
//
//        ///////////////////////////
//        WifiInfo wifiinfo = wifiManager.getConnectionInfo(); //Getting IP Address from SSB
//        int ip = wifiinfo.getIpAddress();
//
////        ip = Integer.reverseBytes(ip); //This is other way to get IP
////
////        byte[] ipByteArray = BigInteger.valueOf(ip).toByteArray();
////
////        String ipAdd = null;
////        try {
////            ipAdd = String.valueOf(InetAddress.getByAddress(ipByteArray));
////        } catch (UnknownHostException e) {
////            e.printStackTrace();
////        }
//
//        String ipAdd = Formatter.formatIpAddress(ip); //This is another option to get IP
//        TextView t2 = findViewById(R.id.wifiinfo);
//        t2.setText(ipAdd);
//        /////////////////////////

    }

    public void display_wifi() {
        ListView list = (ListView) findViewById(R.id.wifi_list);
        List<ScanResult> results = null;

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        results = wifiManager.getScanResults();

        int len = results.size();
        ArrayList<String> AA = new ArrayList<>();

        for (int i = 0; i < len; i++) {
            AA.add(results.get(i).SSID);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.wifi_names, R.id.textwifi, AA);

        list.setAdapter(arrayAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String wifi_name = ((TextView) view).getText().toString();
                String wifi_name = (String) parent.getItemAtPosition(position).toString();
                connect_wifi(wifi_name);
            }
        });
    }

    public void connect_wifi(final String wifi_name) {
        Toast.makeText(this, wifi_name, Toast.LENGTH_LONG).show();
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.wifi_log_in);
        dialog.setTitle("Enter your WiFi Password:");
        final TextView t1 = (TextView) dialog.findViewById(R.id.ssid_login);
        t1.setText(wifi_name);
        final EditText e1 = (EditText) dialog.findViewById(R.id.pass_login);
        Button b1 = (Button) dialog.findViewById(R.id.connect_btn);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivitySSB.this, "Please wait..", Toast.LENGTH_LONG).show();

//                OkHttpClient client = new OkHttpClient();
////                String url = "http://192.168.4.1/setting?ssid=" + t1.getText() + "&pass=" + e1.getText();
//                String url = "http://192.168.4.1/scan";
//                Request request = new Request.Builder()
//                        .url(url)
//                        .build();
//                client.newCall(request).enqueue(new Callback() {
//                    @Override
//                    public void onFailure(Call call, IOException e) {
//                        e.printStackTrace();
////                        Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_LONG).show();
//                        TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
//                        scan_detect_connect.setText("NExt time");
//                    }
//
//                    @Override
//                    public void onResponse(Call call, Response response) throws IOException {
//                        if (response.isSuccessful()) {
//                            final String myResponse = response.body().string();
//                            MainActivity.this.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
////                                    t2.setText(myResponse);
//                                    Toast.makeText(MainActivity.this,myResponse,Toast.LENGTH_LONG).show();
//                                    TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
//                                    scan_detect_connect.setText(myResponse);
//                                }
//                            });
//                        }
//                    }
//                });

//                Async_wifi_ssid_pass_share ssid_pass_share = new Async_wifi_ssid_pass_share();
//                ssid_pass_share.execute();

////                String urlGET = "http://192.168.4.1:80/?pin=13";
//                String urlGET ="http://192.168.4.1/scan";
//                URL url;
//                HttpURLConnection urlConnection = null;
//                try {
//                    url = new URL(urlGET);
//                    urlConnection = (HttpURLConnection) url.openConnection();
//
//                    InputStream in = urlConnection.getInputStream();
//                    InputStreamReader isr = new InputStreamReader(in);
//
//                    StringBuffer sb = new StringBuffer();
//                    int data = isr.read();
//                    while (data != -1) {
//                        char current = (char) data;
//                        sb.append(current);
//                        data = isr.read();
//                    }
//
//                    System.out.print(sb.toString());
//                    TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
//                    scan_detect_connect.setText(sb);
//                    JSONObject jsonObject = new JSONObject(sb.toString());
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    if (urlConnection != null) {
//                        urlConnection.disconnect();
//                    }
//                }


//                String serverResponse = "";
//                HttpClient httpclient = new DefaultHttpClient();
//                try {
//                    HttpGet httpGet = new HttpGet();
//                    httpGet.setURI(new URI("http://192.168.4.1/scan"));
//                    HttpResponse httpResponse = httpclient.execute(httpGet);
//
//                    InputStream inputStream = null;
//                    inputStream = httpResponse.getEntity().getContent();
//                    BufferedReader bufferedReader =
//                            new BufferedReader(new InputStreamReader(inputStream));
//                    serverResponse = bufferedReader.readLine();
//
//                    TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
//                    scan_detect_connect.setText(serverResponse);
//
//                    inputStream.close();
//                } catch (URISyntaxException e) {
//                    e.printStackTrace();
//                    serverResponse = e.getMessage();
//                } catch (ClientProtocolException e) {
//                    e.printStackTrace();
//                    serverResponse = e.getMessage();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    serverResponse = e.getMessage();
//                }

//                try {
//                    sendGet("http://192.168.4.1:80");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
//                    scan_detect_connect.setText("LOLlll");
//                }

//                HttpGetRequest AA = new HttpGetRequest();
//                AA.execute("http://192.168.4.1/scan");


                Async_wifi_ssid_pass_share AA = new Async_wifi_ssid_pass_share();
                AA.execute(t1.getText().toString(), e1.getText().toString());

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void saveArrayList(ArrayList<String> list, String key) {
        SharedPreferences prefs = getSharedPreferences("DB", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();
    }

    public ArrayList<String> getArrayList(String key) {
        SharedPreferences prefs = getSharedPreferences("DB", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public String sendGet(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        System.out.println("Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // connection ok
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
            scan_detect_connect.setText(response);
            return response.toString();
        } else {
            TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
            scan_detect_connect.setText("LOL");
            return "";
        }
    }

    public class Async_wifi_scan extends AsyncTask<String, String, String> {
        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(MainActivitySSB.this);
            p.setMessage("Please Wait....");
            p.setIndeterminate(false);
            p.setCancelable(false);
            p.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            WifiConfiguration wifiConfig = new WifiConfiguration();
            String ssid = "SSB";
            String key = "87654321";

//        conf.SSID = "\"\"" + networkSSID + "\"\"";
//        conf.preSharedKey = "\"" + networkPass + "\"";

            wifiConfig.SSID = String.format("\"%s\"", ssid);
            wifiConfig.preSharedKey = String.format("\"%s\"", key);

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);//remember id

//            int netId = wifiManager.addNetwork(wifiConfig);
//            wifiManager.removeNetwork(netId);
//            int netIdd = wifiManager.addNetwork(wifiConfig);
//            wifiManager.disconnect();
//            wifiManager.enableNetwork(netIdd, true);
//            wifiManager.reconnect();
            int counts = 0;
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
                }
            }
            return "OK";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.contains("OK")) {
                TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
                TextView wifi_mssg = findViewById(R.id.textView2);
                WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                if (wifiManager.getConnectionInfo().getSSID().contains("SSB")) {
                    scan_detect_connect.setText("Connected!");
                    wifi_mssg.setText("Please select your WiFi :");
                    display_wifi();
                    p.dismiss();
                } else {
                    scan_detect_connect.setText("Not Connected");
                }
            } else {
                p.dismiss();
                Toast.makeText(MainActivitySSB.this, "SSB Not Detected! Try Again!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class Async_wifi_ssid_pass_share extends AsyncTask<String, String, ArrayList<String>> {
        ProgressDialog p;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(MainActivitySSB.this);
            p.setMessage("Please Wait....");
            p.setIndeterminate(false);
            p.setCancelable(false);
            p.show();
        }

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            OkHttpClient client = new OkHttpClient();
            String relay_find = "";
            String our_unique_code = "";
            String unid_code = String.valueOf(UUID.randomUUID());
//            String[] listt = null;
            ArrayList<String> listt = new ArrayList<String>();
            try {
                ///////Getting Version of Smart Switch board
//                String url = "http://192.168.4.1/setting?ssid=Yashhh&pass=87654321";
                String url = "http://192.168.4.1";
                Log.d("JSON", "URL: " + url);
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = client.newCall(request).execute();
//                results = String.valueOf(response.body().source().readByteString());
                relay_find = String.valueOf(response.body().source().readUtf8());
//                Log.d("JSON", response.body().string());
                response.close();

                //////////Sending SSID, pass and unique code to hardware
                String url2 = "http://192.168.4.1/setting?ssid=" + strings[0] + "&pass=" + strings[1] + "&uuid=" + unid_code;
//                String url = "http://192.168.4.1:80";
                Log.d("JSON", "URL: " + url2);
                Request request2 = new Request.Builder()
                        .url(url2)
                        .build();
                Response response2 = client.newCall(request2).execute();
//                results = String.valueOf(response.body().source().readByteString());
//                results = String.valueOf(response.body().source().readUtf8());
//                Log.d("JSON", response.body().string());
                response2.close();
                our_unique_code = strings[0] + ":" + strings[1] + ":" + unid_code;
            } catch (IOException e) {
                e.printStackTrace();
            }

            listt.add(relay_find);
            listt.add(our_unique_code);
            listt.add(strings[0]);
            listt.add(strings[1]);
            return listt;
        }

        @Override
        protected void onPostExecute(ArrayList<String> list) {
            super.onPostExecute(list);
            String relay_find = list.get(0);
            String our_unique_code = list.get(1);
//            Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
//            TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
//            scan_detect_connect.setText(s);
            int AA = relay_find.indexOf("[");
            int BB = relay_find.indexOf("]");
            String relay_found = relay_find.substring(AA + 1, BB);
            int relay_count = Integer.valueOf(relay_found);
            Log.d("hsay relay", String.valueOf(relay_count));
            Log.d("hsay unique id", our_unique_code);
            String our_fire_code = our_unique_code + ":" + relay_count; /// Real Time Firebase title
            Log.d("hsay Realtime fire id", our_fire_code);


            ArrayList<String> db_list = new ArrayList<>();
            db_list.add(our_fire_code);

            ArrayList<String> ARE = getArrayList("Firecode_DB_1");

            if (ARE == null) {
                saveArrayList(db_list, "Firecode_DB_1");
            } else {
                ARE.add(our_fire_code);
                saveArrayList(ARE, "Firecode_DB_1");
            }


//            sp = getSharedPreferences("DB", MODE_PRIVATE);
//            String temp = sp.getString("Firecode_DB",null);
//            if (temp == null){
//                sp = getSharedPreferences("DB", MODE_PRIVATE);
//                SharedPreferences.Editor editor = sp.edit();
//                editor.putString("Firecode_DB", our_fire_code);
//                editor.apply();
//            }
//            else{
//                sp = getSharedPreferences("DB", MODE_PRIVATE);
//                SharedPreferences.Editor editor = sp.edit();
//                editor.putString("Firecode_DB", temp + "," + our_fire_code );
//                editor.apply();
//            }

            //////////////Connecting to user WiFi router
            WifiConfiguration wifiConfig = new WifiConfiguration();
            String ssid = list.get(2);
            String pass = list.get(3);
            wifiConfig.SSID = String.format("\"%s\"", ssid);
            wifiConfig.preSharedKey = String.format("\"%s\"", pass);
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);//remember id
            int netIdd = wifiManager.addNetwork(wifiConfig);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netIdd, true);
            wifiManager.reconnect();

            p.dismiss();
            Intent i = new Intent(getApplicationContext(), HomeSSB.class);
            startActivity(i);
        }
    }

    public class HttpGetRequest extends AsyncTask<String, Void, String> {
        public static final String REQUEST_METHOD = "GET";
        public static final int READ_TIMEOUT = 15000;
        public static final int CONNECTION_TIMEOUT = 15000;

        @Override
        protected String doInBackground(String... params) {
            String stringUrl = params[0];
            String result;
            String inputLine;
            try {
                //Create a URL object holding our url
                URL myUrl = new URL(stringUrl);
                //Create a connection
                HttpURLConnection connection = (HttpURLConnection)
                        myUrl.openConnection();
                //Set methods and timeouts
                connection.setRequestMethod(REQUEST_METHOD);
                connection.setReadTimeout(READ_TIMEOUT);
                connection.setConnectTimeout(CONNECTION_TIMEOUT);

                //Connect to our url
                connection.connect();
                //Create a new InputStreamReader
                InputStreamReader streamReader = new
                        InputStreamReader(connection.getInputStream());
                //Create a new buffered reader and String Builder
                BufferedReader reader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                //Check if the line we are reading is not null
                while ((inputLine = reader.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }
                //Close our InputStream and Buffered reader
                reader.close();
                streamReader.close();
                //Set our result equal to our stringBuilder
                result = stringBuilder.toString();
                TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
                scan_detect_connect.setText(result);
            } catch (IOException e) {
                e.printStackTrace();
                TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
                scan_detect_connect.setText("Again LOL");
                result = null;
            }
            return result;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
            scan_detect_connect.setText(result);
        }
    }


//    public void ONN(View view) {
//
//        WifiConfiguration wifiConfig = new WifiConfiguration();
//        String ssid = "SSB";
//        String key = "87654321";
//
////        conf.SSID = "\"\"" + networkSSID + "\"\"";
////        conf.preSharedKey = "\"" + networkPass + "\"";
//
//        wifiConfig.SSID = String.format("\"%s\"", ssid);
//        wifiConfig.preSharedKey = String.format("\"%s\"", key);
//
//        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);//remember id
//        int netId = wifiManager.addNetwork(wifiConfig);
//        wifiManager.disconnect();
//        wifiManager.enableNetwork(netId, true);
//        wifiManager.reconnect();
//    }

//    public boolean Scann_wifi(){
//        List<ScanResult> results = null;
//        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//        TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
//        while (true){
//            results = wifiManager.getScanResults();
//            int len = results.size();
//
//            for (int i=0; i <= len; i++) {
//                if (results.get(i).SSID.equals("SSB")) {
//
//                    scan_detect_connect.setText("Detected");
//                    return true;
//                }
//                else {
//                    scan_detect_connect.setText("Scanning");
//                }
//            }
//        }
//    }

//    public void scan_now(View view) throws InterruptedException {
//        List<ScanResult> results = null;
//        TextView outputs = findViewById(R.id.wifi_scanned);
//        outputs.setText("");
//        TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
//        scan_detect_connect.setText("");
//
//        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//        results = wifiManager.getScanResults();
//
//        int len = results.size();
//        String res = null;
//        ArrayList<String> AA = new ArrayList<>();
//        for (int i = 0; i < len; i++) {
//            res = outputs.getText() + "\n" + results.get(i).SSID;
//            outputs.setText(res);
//            AA.add(results.get(i).SSID);
////            if (results.get(i).SSID.equals("SSB")){
////                scan_detect_connect.setText("Detected");
////            }
////            else{
////                scan_detect_connect.setText("Scanning");
////            }
//        }
//        if (AA.contains("SSB")){
//            AA.clear();
//            scan_detect_connect.setText("Detected");
//            detected_ssb();
//        }
//        else{
//            scan_detect_connect.setText("Scanning");
//        }
//    }

//    public void detected_ssb() throws InterruptedException {
//        TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
//        scan_detect_connect.setText("Connecting");
//        WifiConfiguration wifiConfig = new WifiConfiguration();
//        String ssid = "SSB";
//        String key = "87654321";
//
////        conf.SSID = "\"\"" + networkSSID + "\"\"";
////        conf.preSharedKey = "\"" + networkPass + "\"";
//
//        wifiConfig.SSID = String.format("\"%s\"", ssid);
//        wifiConfig.preSharedKey = String.format("\"%s\"", key);
//
//        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);//remember id
//        int netId = wifiManager.addNetwork(wifiConfig);
//        wifiManager.disconnect();
//        wifiManager.enableNetwork(netId, true);
//        wifiManager.reconnect();
//
//
//        if (wifiManager.getConnectionInfo().getSSID().equals("SSB")){
//            scan_detect_connect.setText("Connected!");
//        }
//        else{
//            scan_detect_connect.setText(wifiManager.getConnectionInfo().getSSID());
//        }
//    }

//    public void clickss(View view) throws IOException {
//        final TextView scan_detect_connect = findViewById(R.id.scan_detected_connected);
//        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
//        String AA = wifiManager.getConnectionInfo().getSSID();
//
//        if (AA.contains("SSB")){
//            scan_detect_connect.setText("Connected!");
//                        ///////////////////////////
//            WifiInfo wifiinfo = wifiManager.getConnectionInfo(); //Getting IP Address from SSB
//            int ip = wifiinfo.getIpAddress();
//
//    //        ip = Integer.reverseBytes(ip); //This is other way to get IP
//    //
//    //        byte[] ipByteArray = BigInteger.valueOf(ip).toByteArray();
//    //
//    //        String ipAdd = null;
//    //        try {
//    //            ipAdd = String.valueOf(InetAddress.getByAddress(ipByteArray));
//    //        } catch (UnknownHostException e) {
//    //            e.printStackTrace();
//    //        }
//            String ipAdd = Formatter.formatIpAddress(ip); //This is another option to get IP
//            final TextView t2 = findViewById(R.id.wifiinfo);
//            t2.setText(ipAdd);
//            /////////////////////////
//
////            URL url = new URL("http://www.google.com");
////            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
////            try {
//////                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
//////                t2.setText((CharSequence) in);
////                t2.setText(urlConnection.getResponseCode());
////            }
////            finally {
////                urlConnection.disconnect();
////            }
//
//            OkHttpClient client = new OkHttpClient();
//            String url = "https://reqres.in/";
//            Request request = new Request.Builder()
//                    .url(url)
//                    .build();
//            client.newCall(request).enqueue(new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    e.printStackTrace();
//                }
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if (response.isSuccessful()) {
//                        final String myResponse = response.body().string();
//                        MainActivity.this.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                t2.setText(myResponse);
//                            }
//                        });
//                    }
//                }
//            });
//        }
//        else{
//            scan_detect_connect.setText("NOT Connected");
//        }
//    }

}