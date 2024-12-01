package com.example.googleapi;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> restaurantList = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private Button backButton, deleteButton;

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

        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDatabase();
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
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUDE));
                restaurantList.add("Name: " + name + ", Rating: " + rating + ", Latitude: " + latitude + ", Longitude: " + longitude);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        adapter.notifyDataSetChanged();
    }

    private void deleteDatabase() {
    File dbFile = getDatabasePath(dbHelper.getDatabaseName());
        if (dbFile.exists()) {
            boolean deleted = dbFile.delete();
            if (deleted) {
                Toast.makeText(this, "Database deleted successfully", Toast.LENGTH_SHORT).show();
                restaurantList.clear();
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Failed to delete database", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Database does not exist", Toast.LENGTH_SHORT).show();
        }
    }
}