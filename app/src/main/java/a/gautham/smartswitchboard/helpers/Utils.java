package a.gautham.smartswitchboard.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class Utils {

    public void Successful_alertdialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Connected Successfully!");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
