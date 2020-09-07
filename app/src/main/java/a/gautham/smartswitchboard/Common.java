package a.gautham.smartswitchboard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Common {

    public static String PHONE_NUMBER = "default";
    public static String EMAIL = "default";
    public static String uid = "default";
    public static String NAME = "default";
    public static boolean SETTINGS_ENABLED = false;
    public static String DEVICE_ID = "";

    public static boolean checkInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static void toastShort(Context context, String text, int bgColor, int textColor){
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);

        if (bgColor==0)
            bgColor = Color.WHITE;

        if (textColor==0)
            textColor = Color.BLACK;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.toast_layout, null);
        toast.setView(view);

        TextView textView = view.findViewById(R.id.textView);
        textView.setText(text);
        textView.setTextColor(textColor);

        CardView bg = view.findViewById(R.id.bg);
        bg.setCardBackgroundColor(bgColor);

        toast.show();
    }

    public static void toastLong(Context context, String text, int bgColor, int textColor){
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);

        if (bgColor==0)
            bgColor = Color.WHITE;

        if (textColor==0)
            textColor = Color.BLACK;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.toast_layout, null);
        toast.setView(view);

        TextView textView = view.findViewById(R.id.textView);
        textView.setText(text);
        textView.setTextColor(textColor);

        CardView bg = view.findViewById(R.id.bg);
        bg.setCardBackgroundColor(bgColor);

        toast.show();
    }

    public static boolean getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else return activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            return false;
        }
    }

    public void Successful_alertdialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Connected Successfully!");
        builder.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public Map<String, Object> getDeviceDetails(Context context, boolean loggedIn) {

        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        Map<String, Object> devices = new HashMap<>();
        devices.put("device_id", Common.DEVICE_ID);
        devices.put("logged_in", loggedIn);
        devices.put("device_name", getDeviceName());
        devices.put("sdk_version", String.valueOf(Build.VERSION.SDK_INT));
        devices.put("last_login", System.currentTimeMillis());
        devices.put("ip", ip);
        return devices;
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

}
