package com.loroclip.record.recorder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.loroclip.R;
import com.loroclip.model.Record;

import java.util.ArrayList;
import java.util.List;

public class RecordListActivity extends Activity {

  ListView recordListView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_record_list);

    List<Record> records = Record.listAll(Record.class);
    ArrayList<String> titles = new ArrayList<String>();
    for(int i=0; i < records.size(); i++) {
      titles.add(records.get(i).getTitle());
    }


    String[] titleArray = new String[titles.size()];
    titles.toArray(titleArray);


    recordListView = (ListView) findViewById(R.id.recordListView);
    recordListView.setAdapter(new ArrayAdapter<String>(
        this, android.R.layout.simple_list_item_single_choice, android.R.id.text1, titleArray));
    recordListView.setOnItemClickListener(testListener);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_record_list, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private AdapterView.OnItemClickListener testListener = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
      String  fileName    = (String) recordListView.getItemAtPosition(position);

      String filename = "/storage/emulated/0/Loroclip/" + fileName + ".wav";
        try {
          Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(filename));
          intent.putExtra("was_get_content_intent", true);
          intent.setClassName( "com.loroclip", "com.loroclip.LoroClipEditActivity");
          startActivityForResult(intent, 1);
        } catch (Exception e) {
        Log.e("LoroClip", "Couldn't start editor");
        }
    }
  };
}