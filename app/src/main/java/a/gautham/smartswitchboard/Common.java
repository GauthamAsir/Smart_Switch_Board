package a.gautham.smartswitchboard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

public class Common {

    public static String PHONE_NUMBER = "default";
    public static String EMAIL = "default";
    public static String uid = "default";
    public static String NAME = "default";
    public static boolean SETTINGS_ENABLED = false;

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

}
