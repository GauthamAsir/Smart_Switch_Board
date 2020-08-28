package a.gautham.smartswitchboard.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import a.gautham.smartswitchboard.Common;

import static android.content.Context.MODE_PRIVATE;

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

    public void saveArrayList(Context context, ArrayList<ArrayList<String>> list, String key) {
        SharedPreferences prefs = context.getSharedPreferences("DB_temp", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(list);
        prefs.edit().putString(key, json).apply();
    }

    public void syncSSBList(Context context, String uid) {
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("Users")
                .document(uid);

        docRef.get().addOnCompleteListener(task -> {

            DocumentSnapshot document = task.getResult();

            System.out.println("TEST");

            if (task.isSuccessful() && document != null) {
                if (document.get("ssb_list") != null) {
                    System.out.println("TEST2");
                    ArrayList<String> arrayList = (ArrayList<String>) Objects.requireNonNull(document.get("ssb_list"));
                    ArrayList<ArrayList<String>> ssbList = new ArrayList<>();
                    if (arrayList.size() > 0) {
                        for (String s : arrayList) {
                            ArrayList<String> myList = new ArrayList<>(Arrays.asList(s.split(",")));
                            ssbList.add(myList);
                        }
                        new Utils().saveArrayList(context, ssbList, "fire_db_temp");
                    }
                }
            } else {
                Common.toastLong(context, "Unable to Sync Your Old Data", 0, 0);
            }
        });
    }

}
