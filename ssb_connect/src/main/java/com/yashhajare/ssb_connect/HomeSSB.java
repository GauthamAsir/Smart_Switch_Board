package com.yashhajare.ssb_connect;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class HomeSSB extends AppCompatActivity {

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_ssb);

        ListView listView = findViewById(R.id.list_view);


        ArrayList<String> fire_list = getArrayList("Firecode_DB_1");
        if (fire_list == null) {
            Toast.makeText(this, "Not connected to any SSB", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, String.valueOf(fire_list), Toast.LENGTH_SHORT).show();
            ArrayList<String> list = new ArrayList<>();
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.fire_button_layout, R.id.create_txt, list);
            listView.setAdapter(arrayAdapter);

            for (int i = 0; i < fire_list.size(); i++) {
                String[] temp = fire_list.get(i).split(":"); //Finding Relay numbers
                int relay_counts = Integer.parseInt(temp[temp.length - 1]);
//                Toast.makeText( this,relay_counts, Toast.LENGTH_SHORT).show();

                for (int j = 0; j < relay_counts; j++) {
                    list.add(fire_list.get(i) + "Switch :" + j);
                }
//                toggles(list);
                arrayAdapter.notifyDataSetChanged();
            }
        }

    }

    public void toggles(ArrayList<String> s) {
        ListView listView = findViewById(R.id.list_view);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.fire_button_layout, R.id.create_txt, s);
        listView.setAdapter(arrayAdapter);

//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                String AA = adapterView.getItemAtPosition(i).toString();
//
//            }
//        });
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

}