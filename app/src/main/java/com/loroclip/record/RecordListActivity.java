package com.loroclip.record;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.loroclip.LoroClipEditActivity;
import com.loroclip.R;
import com.loroclip.model.Record;

import java.util.ArrayList;
import java.util.List;

public class RecordListActivity extends Activity {

  private final String LOROCLIP_PATH = Environment.getExternalStorageDirectory().toString() + "/Loroclip/";;
  private final String AUDIO_OGG_EXTENSION = ".ogg";
  private List<Record> mRecords;

  private ListView recordListView;
  private AdapterView.OnItemClickListener selectItemListener = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
      Record clickedRecord = mRecords.get(position);

      try {
        Intent intent = new Intent(getApplicationContext(), LoroClipEditActivity.class);
        intent.putExtra("record_id", clickedRecord.getId());
        intent.putExtra("was_get_content_intent", true);
        startActivityForResult(intent, 1);
      } catch (Exception e) {
        Log.e("LoroClip", "Couldn't start editor");
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_record_list);

    recordListView = (ListView) findViewById(R.id.recordListView);
    mRecords = Record.listAll(Record.class);

    setRecordListItem();
    addEvnetListener();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_record_list, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public void addEvnetListener() {

    recordListView.setOnItemClickListener(selectItemListener);
  }

  public void setRecordListItem() {
    ArrayList<String> titles = new ArrayList<>();

    for (Record record : mRecords) {
      titles.add(record.getTitle());
    }

    recordListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, android.R.id.text1, titles));
  }

}