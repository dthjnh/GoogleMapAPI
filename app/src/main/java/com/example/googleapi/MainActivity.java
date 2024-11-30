package com.example.googleapi;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> restaurantList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, restaurantList);
        listView.setAdapter(adapter);

        dbHelper = new DatabaseHelper(this);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        fetchRestaurants();
    }

    private void fetchRestaurants() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_RESTAURANTS, null, null, null, null, null, null);

        restaurantList.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
                double rating = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RATING));
                restaurantList.add("Name: " + name + ", Rating: " + rating);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        adapter.notifyDataSetChanged();
    }
}