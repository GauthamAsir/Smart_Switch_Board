package a.gautham.smartswitchboard.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import a.gautham.smartswitchboard.Common;
import a.gautham.smartswitchboard.R;
import a.gautham.smartswitchboard.activity.MainActivity;

import static android.content.Context.MODE_PRIVATE;

public class ListAdapterSSB extends ArrayAdapter<String> {
    public Context context;
    customButtonListener customListner;
    FirebaseDatabase firebase_realtime_db = FirebaseDatabase.getInstance();
    private ArrayList<String> data;
    private ArrayList<String> ori_relay_name;

    public ListAdapterSSB(@NonNull Context context, ArrayList<String> resource, ArrayList<String> names) {
        super(context, R.layout.item_layout, resource);
        this.data = resource;
        this.context = context;
        this.ori_relay_name = names;
    }

    public void setCustomButtonListner(customButtonListener listener) {
        this.customListner = listener;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.text = convertView.findViewById(R.id.create_txt);
            viewHolder.toggleButton = convertView.findViewById(R.id.create_toggle);
            viewHolder.imageView = convertView.findViewById(R.id.create_img);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.toggleButton.setText(R.string.please_wait);
        viewHolder.toggleButton.setEnabled(false);
        final String fire_rt_db_secret_key = data.get(position);

        final DatabaseReference fire_db_reference = firebase_realtime_db.getReference(fire_rt_db_secret_key);
        fire_db_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    Boolean value = snapshot.getValue(Boolean.class);
                    viewHolder.toggleButton.setEnabled(true);
                    if (value != null) {
                        viewHolder.toggleButton.setChecked(value);
                    } else {
                        fire_db_reference.setValue(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    String value = snapshot.getValue(String.class);
                    viewHolder.toggleButton.setEnabled(false);
                    if (value != null) {
                        if (value.contains("Deleted")) {
                            viewHolder.toggleButton.setText(R.string.deleted);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        viewHolder.text.setText(ori_relay_name.get(position));

        viewHolder.toggleButton.setOnCheckedChangeListener((compoundButton, b) -> {
            if (customListner != null) {
                DatabaseReference fire_db_reference1 = firebase_realtime_db.getReference(fire_rt_db_secret_key);
                fire_db_reference1.setValue(b);
            }
        });

        viewHolder.toggleButton.setOnClickListener(view -> {
            fire_rt_value_cross_check AA = new fire_rt_value_cross_check();
            AA.execute();
        });

        viewHolder.imageView.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.getMenuInflater().inflate(R.menu.popup_setting, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (String.valueOf(menuItem.getTitle())) {
                    case "Edit":
                        change_name_of_relay(fire_rt_db_secret_key, String.valueOf(viewHolder.text.getText()), viewHolder.text);
                        break;
                    case "Share":
                        SharingIsCaringSSB shaing_is_caring = new SharingIsCaringSSB(context, null);
                        shaing_is_caring.sharing_secret_code(fire_rt_db_secret_key);
                        break;
                    case "Delete":
                        delete_name_of_raley(fire_rt_db_secret_key);
                        break;
                    default:
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public Boolean check_check() {
        boolean isOnline = false;
        try {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkCapabilities capabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());  // need ACCESS_NETWORK_STATE permission
            isOnline = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isOnline;
    }

    private void change_name_of_relay(final String index, final String original_name, final TextView textView) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View input = layoutInflater.inflate(R.layout.change_name_of_relay_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(input);
        final EditText editText = input.findViewById(R.id.edit_text);
        builder.setPositiveButton(R.string.ok, (dialogInterface, num) -> {
            if (editText.length() > 1) {
                textView.setText(editText.getText());
                String[] temp = index.split("/");
                String getting_secret_key = temp[0];

                ArrayList<ArrayList<String>> saved_fire_db = getArrayList("fire_db_temp");

                for (int i = 0; i < saved_fire_db.size(); i++) {
                    if (saved_fire_db.get(i).get(0).equals(getting_secret_key)) {

                        int AA = saved_fire_db.get(i).indexOf(original_name);
                        saved_fire_db.get(i).set(AA, String.valueOf(editText.getText()));
                    }
                }
                saveArrayList(saved_fire_db, "fire_db_temp");
                Toast.makeText(context, R.string.title_changed, Toast.LENGTH_LONG).show();

                ori_relay_name.clear();
                for (int i = 0; i < saved_fire_db.size(); i++) {
                    int sizeee = saved_fire_db.get(i).size();
                    for (int j = 1; j < sizeee; j++) {
                        if (!saved_fire_db.get(i).get(j).equals("BLANK")) {
                            ori_relay_name.add(saved_fire_db.get(i).get(j));
                        }
                    }
                }
                notifyDataSetChanged();
            } else {
                Toast.makeText(context, R.string.enter_valid_text, Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(
                context, R.color.colorAccent));
        alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(
                context, R.color.colorAccent));
    }

    public void delete_name_of_raley(final String fire_rt_db_secret_key) {
        int size;

        ArrayList<String> AA_array = new ArrayList<>();
        ArrayList<Boolean> BB_array = new ArrayList<>();

        final ArrayList<String> secret_key_which_is_deleted = new ArrayList<>();

        String[] temp = fire_rt_db_secret_key.split("/");
        final String getting_secret_key = temp[0];

        final ArrayList<ArrayList<String>> arrayLists = getArrayList("fire_db_temp");

        for (int i = 0; i < arrayLists.size(); i++) {
            if (arrayLists.get(i).get(0).equals(getting_secret_key)) {
                size = arrayLists.get(i).size();
                secret_key_which_is_deleted.addAll(arrayLists.get(i));

                for (int j = 1; j < size; j++) {
                    if (!arrayLists.get(i).get(j).equals("BLANK")) {
                        AA_array.add(arrayLists.get(i).get(j));
                        BB_array.add(true);
                    }
                }
            }
        }

        String[] AA = new String[AA_array.size()];
        for (int boi1 = 0; boi1 < BB_array.size(); boi1++) {
            AA[boi1] = AA_array.get(boi1);
        }

        boolean[] BB = new boolean[BB_array.size()];
        for (int boi = 0; boi < BB_array.size(); boi++) {
            BB[boi] = BB_array.get(boi);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.select_switches);

        final String[] finalAA = AA;
        final boolean[] finalBB = BB;
        builder.setMultiChoiceItems(AA, BB, (dialogInterface, i, b) -> finalBB[i] = b);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            int true_counts;
            int false_counts;

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (int k = 0; k < finalAA.length; k++) {
                    if (finalBB[k]) {
                        DatabaseReference deleting_reference = firebase_realtime_db.getReference(getting_secret_key + "/" + "Switch : " + secret_key_which_is_deleted.indexOf(finalAA[k]));
                        deleting_reference.setValue("Deleted");
                        true_counts++;
                    } else {
                        false_counts++;
                    }
                }
                if (true_counts == finalBB.length) {

                    arrayLists.remove(secret_key_which_is_deleted);
                    saveArrayList(arrayLists, "fire_db_temp");

                    ArrayList<ArrayList<String>> deleted_ssbs_arraylist = getArrayList("deleted_ssbs");
                    ArrayList<ArrayList<String>> if_array_is_null = new ArrayList<>();
                    if_array_is_null.add(secret_key_which_is_deleted);

                    if (deleted_ssbs_arraylist == null || deleted_ssbs_arraylist.isEmpty()) {
                        saveArrayList(if_array_is_null, "deleted_ssbs");
                    } else {
                        boolean state_deleted = true;
                        String initial_name = secret_key_which_is_deleted.get(0);
                        for (int temp_i = 0; temp_i < deleted_ssbs_arraylist.size(); temp_i++) {
                            if (deleted_ssbs_arraylist.get(temp_i).get(0).equals(initial_name)) {
                                deleted_ssbs_arraylist.set(deleted_ssbs_arraylist.indexOf(deleted_ssbs_arraylist.get(temp_i)), secret_key_which_is_deleted);
                                saveArrayList(deleted_ssbs_arraylist, "deleted_ssbs");
                                state_deleted = false;
                            }
                        }

                        if (state_deleted) {
                            deleted_ssbs_arraylist.add(secret_key_which_is_deleted);
                            saveArrayList(deleted_ssbs_arraylist, "deleted_ssbs");
                        }
                    }

                    ori_relay_name.clear();
                    data.clear();
                    for (int z = 0; z < arrayLists.size(); z++) {
                        int sizeee = arrayLists.get(z).size();
                        for (int y = 1; y < sizeee; y++) {
                            if (!arrayLists.get(z).get(y).equals("BLANK")) {
                                data.add(arrayLists.get(z).get(0) + "/" + "Switch : " + y);
                                ori_relay_name.add(arrayLists.get(z).get(y));
                            }
                        }
                    }
                    notifyDataSetChanged();
                }
                if (false_counts == finalBB.length) {
                    Toast.makeText(context, R.string.please_select_switches, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(
                context, R.color.colorAccent));
        alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(
                context, R.color.colorAccent));

    }

    public void saveArrayList(ArrayList<ArrayList<String>> list, String key) {
        SharedPreferences prefs = context.getSharedPreferences("DB_temp", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(list);
        prefs.edit().putString(key, json).apply();
        Type type = new TypeToken<ArrayList<ArrayList<String>>>() {
        }.getType();
        ArrayList<ArrayList<String>> ssbList = gson.fromJson(json, type);
        ArrayList<String> singleList = new ArrayList<>();
        for (ArrayList<String> s : ssbList) {
            String joined = TextUtils.join(",", s);
            singleList.add(joined);
        }
        if (key.equals("deleted_ssbs"))
            FirebaseFirestore.getInstance().collection("Users")
                    .document(Common.uid).update("deleted_ssb", singleList);
        else
            FirebaseFirestore.getInstance().collection("Users")
                    .document(Common.uid).update("ssb_list", singleList);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public void Updating_list_adapter() {
        ArrayList<ArrayList<String>> arrayLists = getArrayList("fire_db_temp");

        ori_relay_name.clear();
        data.clear();
        for (int z = 0; z < arrayLists.size(); z++) {
            int sizeee = arrayLists.get(z).size();
            for (int y = 1; y < sizeee; y++) {
                if (!arrayLists.get(z).get(y).equals("BLANK")) {
                    data.add(arrayLists.get(z).get(0) + "/" + "Switch : " + y);
                    ori_relay_name.add(arrayLists.get(z).get(y));
                }
            }
        }
        notifyDataSetChanged();

        Activity activity = (Activity) context;

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("SUCCESS", "SUCCESS");
        context.startActivity(intent);

        activity.finish();
    }

    public ArrayList<ArrayList<String>> getArrayList(String key) {
        SharedPreferences prefs = context.getSharedPreferences("DB_temp", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<ArrayList<String>>>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public interface customButtonListener {
        void onButtonClickListener(int position, String value);

        void onToggleClickListener(int position, String value, Boolean ans);
    }

    public static class ViewHolder {
        TextView text;
        ToggleButton toggleButton;
        ImageView imageView;
    }

    @SuppressLint("StaticFieldLeak")
    public class fire_rt_value_cross_check extends AsyncTask<String, String, String> {
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
        protected String doInBackground(final String... strings) {
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "OK";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (check_check()) {
                p.dismiss();
            } else {
                p.dismiss();
                Common.toastShort(context, context.getString(R.string.no_interent), 0, 0);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle("No Internet");
                alertDialogBuilder.setMessage(R.string.response_will_be_sent_once_interenet);
                alertDialogBuilder.setPositiveButton(R.string.ok,
                        (arg0, arg1) -> arg0.cancel());
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(
                        context, R.color.colorAccent));
            }
        }
    }
}
