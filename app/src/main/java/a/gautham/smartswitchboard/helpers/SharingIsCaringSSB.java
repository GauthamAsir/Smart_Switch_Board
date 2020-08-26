package a.gautham.smartswitchboard.helpers;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.lang.reflect.Type;
import java.util.ArrayList;

import a.gautham.smartswitchboard.MainActivity;
import a.gautham.smartswitchboard.R;

public class SharingIsCaringSSB extends Activity {
    public Context context;
    public Activity activity;
    public ListAdapterSSB adapter;
    public int Cam_request_code = 321;
    AlertDialog alertDialog_getting_secret_code;
    AlertDialog alertDialog_sharing_secret_code;

    public SharingIsCaringSSB(Context foreign_context, Activity foreign_activity, ListAdapterSSB adapter) {
        this.context = foreign_context;
        this.activity = foreign_activity;
        this.adapter = adapter;
    }

    public SharingIsCaringSSB(Context foreign_context, Activity foreign_activity) {
        this.context = foreign_context;
        this.activity = foreign_activity;
    }

    ///////////////////////////////////////////////////////GETTING SECRET KEYS METHOD SECTION
    public void getting_secret_code() {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View input = layoutInflater.inflate(R.layout.getting_secret_code_layout, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(input);
        Button scan_qr_btn = input.findViewById(R.id.scan_qr);
        Button copy_paste_qr_btn = input.findViewById(R.id.copy_paste);

        scan_qr_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog_getting_secret_code.dismiss();
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
//                    Toast.makeText(context,"Permission Denied! Please Allow Permission.",Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, Cam_request_code);
                } else {
                    openScanner();
                }
            }
        });

        copy_paste_qr_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog_getting_secret_code.dismiss();
                LayoutInflater layoutfor_copypaste = LayoutInflater.from(context);
                View copypaste_input = layoutfor_copypaste.inflate(R.layout.copy_paste_sharing_layout, null);
                AlertDialog.Builder copy_paste = new AlertDialog.Builder(context);
                copy_paste.setView(copypaste_input);
                final EditText copy_paste_edit_text = copypaste_input.findViewById(R.id.copy_paste_edit_text);

                ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                try {
                    final String clip = clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();
                    if (clip.contains("https://smart.switch.board/")) {
                        Toast.makeText(context, "Auto Copied!", Toast.LENGTH_LONG).show();
                        copy_paste_edit_text.setText(clip);
                    }
                } catch (NullPointerException n) {
                    Log.d("NULL", String.valueOf(n));
                }


                copy_paste.setPositiveButton("Next", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (copy_paste_edit_text.getText().toString().contains("https://smart.switch.board/")) {
//                            String[] temp_list = copy_paste_edit_text.getText().toString().split("https://smart.switch.board/");
//                            String ciper_text = temp_list[temp_list.length - 1];
                            adding_secret_key_from_scaned_qr_copy_paste_deep_link(copy_paste_edit_text.getText().toString());
                        } else {
                            Toast.makeText(context, "Please enter valid sharecode", Toast.LENGTH_LONG).show();
                        }
                        dialogInterface.dismiss();
                    }
                });
                copy_paste.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alertDialog = copy_paste.create();
                alertDialog.show();
            }
        });
        alertDialog_getting_secret_code = builder.create();
        alertDialog_getting_secret_code.show();
    }

    public void adding_secret_key_from_scaned_qr_copy_paste_deep_link(String ciper_text) {

        String[] temp_list = ciper_text.split("https://smart.switch.board/");
        String pure_ciper_text = temp_list[temp_list.length - 1];
        String secret_key = Hexa_to_string(pure_ciper_text);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        final ArrayList<String> yet_to_add_array = gson.fromJson(secret_key, type);

        final ArrayList<ArrayList<String>> arrayLists = getArrayList("fire_db_temp");
        final ArrayList<ArrayList<String>> if_array_is_null = new ArrayList<>();
        if_array_is_null.add(yet_to_add_array);

        if (arrayLists == null || arrayLists.isEmpty()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("New Connection Received!");
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    saveArrayList(if_array_is_null, "fire_db_temp");
//                    Toast.makeText(context,"Added Successfully!",Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("SUCCESS", "SUCCESS");
                    context.startActivity(intent);
                    Activity activity = (Activity) context;
                    activity.finish();

//                    adapter.Updating_list_adapter();

                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            boolean temp_state = false;
            for (int i = 0; i < arrayLists.size(); i++) {
                if (arrayLists.get(i).get(0).equals(yet_to_add_array.get(0))) {
                    Toast.makeText(context, "Already added!", Toast.LENGTH_SHORT).show();
                    temp_state = true;
                }
            }

            if (!temp_state) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("New Connection Received!");
                builder.setCancelable(false);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        arrayLists.add(yet_to_add_array);
                        saveArrayList(arrayLists, "fire_db_temp");
//                        Toast.makeText(context,"Added Successfully!",Toast.LENGTH_SHORT).show();

//                        ori_relay_name.clear();
//                        data.clear();
//                        for (int z = 0; z < arrayLists.size(); z++){
//                            int sizeee = arrayLists.get(z).size();
//                            for (int y = 1; y < sizeee; y++){
//                                if (!arrayLists.get(z).get(y).equals("BLANK")) {
//                                    data.add(arrayLists.get(z).get(0) + "/" + "Switch : " + String.valueOf(y));
//                                    ori_relay_name.add(arrayLists.get(z).get(y));
//                                }
//                            }
//                        }
//                        notifyDataSetChanged();

                        adapter.Updating_list_adapter();

                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
    }

    public void openScanner() {
//        new IntentIntegrator(this).initiateScan();
        IntentIntegrator intentIntegrator = new IntentIntegrator((Activity) context);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setBeepEnabled(false);
        intentIntegrator.setPrompt("Scan QR");
        intentIntegrator.setTimeout(30000);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        intentIntegrator.initiateScan();
    }


    ////////////////////////////////////////////////////////Sharing Secrets keys to others
    protected void sharing_secret_code(String index) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View input = layoutInflater.inflate(R.layout.sharing_secret_code_layout, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(input);
        final Button qr_button = input.findViewById(R.id.generate_qr);
        final Button share__via_mssg_button = input.findViewById(R.id.share_via_mssg);

        final String[] temp = index.split("/");
        String getting_secret_key = temp[0];
        final ArrayList<ArrayList<String>> arrayLists = getArrayList("fire_db_temp");

        ArrayList<String> temp_array = new ArrayList<>();
        for (int i = 0; i < arrayLists.size(); i++) {
            if (arrayLists.get(i).get(0).equals(getting_secret_key)) {
//                Toast.makeText(context,String.valueOf(saved_fire_db.get(i)),Toast.LENGTH_LONG).show();
//                share_string = String.valueOf(saved_fire_db.get(i));
                temp_array = arrayLists.get(i);
            }
        }
        ////////////////
        int size;
//        String[] AA = new String[0];
//        boolean[] BB = new boolean[0];
        ArrayList<String> AA_array = new ArrayList<>();
        ArrayList<Boolean> BB_array = new ArrayList<>();

        for (int i = 0; i < arrayLists.size(); i++) {
            if (arrayLists.get(i).get(0).equals(getting_secret_key)) {
                size = arrayLists.get(i).size();
//                secret_key_which_is_deleted.addAll(arrayLists.get(i));
//                temp_list_for_deleing = arrayLists.get(i);
//                AA = new String[size - 1];
//                BB = new boolean[size - 1];
                for (int j = 1; j < size; j++) {
                    if (!arrayLists.get(i).get(j).equals("BLANK")) {
//                        AA[j - 1] = arrayLists.get(i).get(j);
//                        BB[j - 1] = true;
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
        ////
        boolean[] BB = new boolean[BB_array.size()];
        for (int boi = 0; boi < BB_array.size(); boi++) {
            BB[boi] = BB_array.get(boi);
        }

        AlertDialog.Builder builder_for_sharing = new AlertDialog.Builder(context);
        builder_for_sharing.setTitle("Select Switches:");

        final String[] finalAA = AA;
        final boolean[] finalBB = BB;
        builder_for_sharing.setMultiChoiceItems(AA, BB, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                finalBB[i] = b;
            }
        });


        final ArrayList<String> finalTemp_array = temp_array;
        builder_for_sharing.setPositiveButton("Share", new DialogInterface.OnClickListener() {
            int true_counts;
            int false_counts;

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                for (int ii = 0; ii < finalBB.length; ii++) {
                    if (!finalBB[ii]) {
                        finalTemp_array.set(ii + 1, "BLANK");
                        false_counts++;
                    } else {
                        true_counts++;
                    }
                }
//                Toast.makeText(context, "Boolean states:" + Arrays.toString(finalBB),Toast.LENGTH_SHORT).show();
//                Toast.makeText(context, "Blanks :" + finalTemp_array.toString(),Toast.LENGTH_SHORT).show();

                if (false_counts == finalBB.length) {
                    Toast.makeText(context, "Please select switches!", Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                } else {
                    /////////////////////////
                    final ArrayList<String> share_string = new ArrayList<>();
                    for (int i_temp = 0; i_temp < finalTemp_array.size(); i_temp++) {
                        share_string.add('"' + finalTemp_array.get(i_temp) + '"');
                    }

                    final String result = String_to_hexa(share_string.toString());


                    qr_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog_sharing_secret_code.dismiss();
                            try {
                                display_qr("https://smart.switch.board/" + result);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    //////////////
                    share__via_mssg_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog_sharing_secret_code.dismiss();
                            try {
                                shareText(result, context);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    alertDialog_sharing_secret_code = builder.create();
                    alertDialog_sharing_secret_code.show();
                    /////////////////////////
                }
            }
        });
        builder_for_sharing.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder_for_sharing.create();
        alertDialog.show();

    }

    private void display_qr(String text) throws WriterException {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View input = layoutInflater.inflate(R.layout.qr_generate_display, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(input);
        ImageView qr_display = input.findViewById(R.id.qr_code);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 500, 500);
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
        qr_display.setImageBitmap(bitmap);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void shareText(String text, Context context) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "I'm inviting you to join Smart Swtich Board.\nClick the link https://smart.switch.board/" + text);
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, null);
        context.startActivity(shareIntent, Bundle.EMPTY);
    }

    /////////////////////////////////////
    private String String_to_hexa(String name) {
        StringBuilder sb = new StringBuilder();

        char[] ch = name.toCharArray();
        for (char c : ch) {
            String hexString = Integer.toHexString(c);
            sb.append(hexString);
        }
//        final String result = sb.toString();
        return sb.toString();
    }

    private String Hexa_to_string(String name) {
        String result = "";
        char[] charArray = name.toCharArray();
        for (int i = 0; i < charArray.length; i = i + 2) {
            String st = "" + charArray[i] + "" + charArray[i + 1];
            char ch = (char) Integer.parseInt(st, 16);
            result = result + ch;
        }
        return result;
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
/////////////////
}
