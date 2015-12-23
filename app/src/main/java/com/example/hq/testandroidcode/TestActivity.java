package com.example.hq.testandroidcode;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TestActivity extends Activity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);
    ListView listView = (ListView) findViewById(R.id.list_view);

    ArrayAdapter<String> adapter;
    String[] items = { "A", "B", "C", "D", "E", "F", "G" };

    adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
    listView.setAdapter(adapter);
  }
}
