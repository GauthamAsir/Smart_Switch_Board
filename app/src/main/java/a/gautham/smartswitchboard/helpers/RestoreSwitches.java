package a.gautham.smartswitchboard.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import a.gautham.smartswitchboard.R;
import a.gautham.smartswitchboard.activity.MainActivity;

import static android.content.Context.MODE_PRIVATE;

public class RestoreSwitches {
    public Context context;
    public ListAdapterSSB adapter;
    FirebaseDatabase firebase_realtime_db = FirebaseDatabase.getInstance();

    public RestoreSwitches(Context our_context, ListAdapterSSB our_adapter) {
        this.context = our_context;
        this.adapter = our_adapter;
    }

    public void Show_deleted_switches() {
        final ArrayList<ArrayList<String>> deleted_ssbs_arraylist = getArrayList("deleted_ssbs");
        final ArrayList<ArrayList<String>> arrayLists = getArrayList("fire_db_temp");

        if (deleted_ssbs_arraylist == null || deleted_ssbs_arraylist.isEmpty()) {
            Toast.makeText(context, R.string.nothing_deleted_yet, Toast.LENGTH_SHORT).show();
        } else {

            final ArrayList<String> deleted_fire_list = new ArrayList<String>();
            final ArrayList<String> deleted_name_list = new ArrayList<String>();

            final ArrayList<String> AA_array = new ArrayList<>();
            final ArrayList<Boolean> BB_array = new ArrayList<>();

            final ArrayList<String> secret_key_which_is_deleted = new ArrayList<>();

            for (int i = 0; i < deleted_ssbs_arraylist.size(); i++) {
                int sizeee = deleted_ssbs_arraylist.get(i).size();
                for (int j = 1; j < sizeee; j++) {
                    if (!deleted_ssbs_arraylist.get(i).get(j).equals("BLANK")) {
                        deleted_fire_list.add(deleted_ssbs_arraylist.get(i).get(0) + "/" + "Switch : " + j);
                        deleted_name_list.add(deleted_ssbs_arraylist.get(i).get(j));
                    }
                }
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.select_switches);

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, deleted_name_list);
            builder.setAdapter(dataAdapter, (dialog, which) -> {

                String[] temp = deleted_fire_list.get(which).split("/");
                final String getting_secret_key = temp[0];

                for (int i = 0; i < deleted_ssbs_arraylist.size(); i++) {
                    if (deleted_ssbs_arraylist.get(i).get(0).equals(getting_secret_key)) {
                        int size = deleted_ssbs_arraylist.get(i).size();
                        secret_key_which_is_deleted.addAll(deleted_ssbs_arraylist.get(i));

                        for (int j = 1; j < size; j++) {
                            if (!deleted_ssbs_arraylist.get(i).get(j).equals("BLANK")) {

                                AA_array.add(deleted_ssbs_arraylist.get(i).get(j));
                                BB_array.add(true);
                            }
                        }
                    }
                }

                boolean temp_state_for_main_array = false;
                for (int ii = 0; ii < arrayLists.size(); ii++) {
                    if (arrayLists.get(ii).get(0).equals(secret_key_which_is_deleted.get(0))) {
                        Toast.makeText(context, R.string.already_added, Toast.LENGTH_SHORT).show();
                        temp_state_for_main_array = true;
                    }
                }

                if (!temp_state_for_main_array) {

                    String[] AA = new String[AA_array.size()];
                    for (int boi1 = 0; boi1 < BB_array.size(); boi1++) {
                        AA[boi1] = AA_array.get(boi1);
                    }
                    boolean[] BB = new boolean[BB_array.size()];
                    for (int boi = 0; boi < BB_array.size(); boi++) {
                        BB[boi] = BB_array.get(boi);
                    }

                    final String[] finalAA = AA;
                    final boolean[] finalBB = BB;

                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                    builder1.setTitle(R.string.confirmation);

                    builder1.setMultiChoiceItems(AA, BB, (dialogInterface, i, b) -> finalBB[i] = b);

                    builder1.setPositiveButton(R.string.restore_now, new DialogInterface.OnClickListener() {
                        int true_counts;
                        int false_counts;

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            for (int k = 0; k < finalAA.length; k++) {
                                if (finalBB[k]) true_counts++;
                                else false_counts++;
                            }
                            if (false_counts == finalBB.length) {
                                Toast.makeText(context, R.string.please_select_switches, Toast.LENGTH_SHORT).show();
                            } else {
                                final ArrayList<ArrayList<String>> arrayLists1 = getArrayList("fire_db_temp");
                                ArrayList<ArrayList<String>> if_array_is_null = new ArrayList<>();
                                if_array_is_null.add(secret_key_which_is_deleted);

                                if (arrayLists1 == null || arrayLists1.isEmpty()) {

                                    for (int k = 0; k < finalAA.length; k++) {
                                        if (finalBB[k]) {
                                            DatabaseReference deleting_reference = firebase_realtime_db.getReference(getting_secret_key + "/" + "Switch : " + secret_key_which_is_deleted.indexOf(finalAA[k]));
                                            deleting_reference.setValue(false);
                                        }
                                    }

                                    saveArrayList(if_array_is_null, "fire_db_temp");

                                    deleted_ssbs_arraylist.remove(secret_key_which_is_deleted);
                                    saveArrayList(deleted_ssbs_arraylist, "deleted_ssbs");

                                    Intent intent = new Intent(context, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("SUCCESS", "SUCCESS");
                                    context.startActivity(intent);
                                    Activity activity = (Activity) context;
                                    activity.finish();

                                } else {
                                    boolean temp_state = false;
                                    for (int ii = 0; ii < arrayLists1.size(); ii++) {
                                        if (arrayLists1.get(ii).get(0).equals(secret_key_which_is_deleted.get(0))) {
                                            Toast.makeText(context, R.string.already_added, Toast.LENGTH_SHORT).show();
                                            temp_state = true;
                                        }
                                    }

                                    if (!temp_state) {
                                        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                                        builder1.setTitle(R.string.new_connection_received);
                                        builder1.setCancelable(false);
                                        builder1.setPositiveButton(R.string.ok, (dialogInterface1, i1) -> {

                                            for (int k = 0; k < finalAA.length; k++) {
                                                if (finalBB[k]) {
                                                    DatabaseReference deleting_reference = firebase_realtime_db.getReference(getting_secret_key + "/" + "Switch : " + secret_key_which_is_deleted.indexOf(finalAA[k]));
                                                    deleting_reference.setValue(false);
                                                }
                                            }

                                            arrayLists1.add(secret_key_which_is_deleted);
                                            saveArrayList(arrayLists1, "fire_db_temp");

                                            deleted_ssbs_arraylist.remove(secret_key_which_is_deleted);
                                            saveArrayList(deleted_ssbs_arraylist, "deleted_ssbs");

                                            adapter.Updating_list_adapter();

                                            dialogInterface1.dismiss();
                                        });
                                        builder1.setNegativeButton(R.string.cancel, (dialogInterface12, i12) -> dialogInterface12.dismiss());
                                        AlertDialog alertDialog = builder1.create();
                                        alertDialog.show();
                                        alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(
                                                context, R.color.colorAccent));
                                        alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(
                                                context, R.color.colorAccent));
                                    }
                                }
                            }
                            dialogInterface.dismiss();
                        }
                    });

                    builder1.setNegativeButton(R.string.delete_forever, new DialogInterface.OnClickListener() {
                        int true_counts;
                        int false_counts;

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            for (int k = 0; k < finalAA.length; k++) {
                                if (finalBB[k]) {
                                    true_counts++;
                                } else {
                                    false_counts++;
                                }
                            }

                            if (false_counts == finalBB.length) {
                                Toast.makeText(context, R.string.please_select_switches, Toast.LENGTH_SHORT).show();
                            } else {
                                deleted_ssbs_arraylist.remove(secret_key_which_is_deleted);
                                saveArrayList(deleted_ssbs_arraylist, "deleted_ssbs");
                            }
                            dialogInterface.dismiss();
                        }
                    });

                    builder1.setNeutralButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());

                    AlertDialog alertDialog = builder1.create();
                    alertDialog.show();
                    alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(
                            context, R.color.colorAccent));
                    alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(
                            context, R.color.colorAccent));
                    alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(
                            context, R.color.colorAccent));
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

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
}
