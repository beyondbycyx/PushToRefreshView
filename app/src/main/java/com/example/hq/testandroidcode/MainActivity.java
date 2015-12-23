package com.example.hq.testandroidcode;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity{

  RefreshableView refreshableView;
  ListView listView;
  ArrayAdapter<String> adapter;
  String[] items = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L" };
  int[] datas = { R.drawable.test_1, R.drawable.test_2, R.drawable.test_3 };


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);
    refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
    listView = (ListView) findViewById(R.id.list_view);
    adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);

    /*
    //添加listview的header

    final ViewPager viewPager = new ViewPager(MainActivity.this);
    PagerAdapter viewAdapter = new PagerAdapter() {
      @Override public int getCount() {
        return 3;
      }

      @Override public boolean isViewFromObject(View view, Object object) {
        return view == object;
      }

      @Override public Object instantiateItem(ViewGroup container, int position) {
        ImageView view = new ImageView(MainActivity.this);
        view.setImageResource(datas[position]);

        container.addView(view);
        return view;
      }

      @Override public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        container.removeViewAt(position);
      }
    };

    viewPager.setAdapter(viewAdapter);

    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
    addContentView(viewPager,params);
    */

    TextView textView = new TextView(MainActivity.this);
    textView.setText("这是一个头部");

    listView.addHeaderView(textView);

    listView.setAdapter(adapter);
    refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
      @Override
      public void onRefresh() {
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        refreshableView.finishRefreshing();
      }
    }, 0);
  }

}