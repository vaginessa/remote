package bjchau.github.remote;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.sql.SQLException;
import java.util.ArrayList;

public class Models extends AppCompatActivity {
    private static final String TAG = "Models";
    ArrayList<String> list = new ArrayList<>();
    ArrayList<Integer> modelIds = new ArrayList<>();
    private Models d = this;
    DataAdapter dbAdapter;
    int brand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_models);

        Intent intent = getIntent();
        brand = intent.getIntExtra("id", -1);
        String name = intent.getStringExtra("name");
        setTitle(name);
        ListView lv = (ListView) findViewById(R.id.list);
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);

        dbAdapter = new DataAdapter(getApplicationContext());

        lv.setAdapter(listAdapter);
        try {
            // Get models for specific brand
            dbAdapter.open();
            Cursor cursor = dbAdapter.getModels(brand);
            dbAdapter.close();

            if (cursor != null) {
                do {
                    modelIds.add(cursor.getInt(0));
                    list.add(cursor.getString(1));
                } while (cursor.moveToNext());
                cursor.close();
            }
        }catch(Exception e){
            Log.e(TAG, "Error getting models.");
        }
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(100);
                Intent intent = new Intent(d, Controllers.class);
                intent.putExtra("id", modelIds.get(position));
                intent.putExtra("name", list.get(position));
                startActivity(intent);
            }
        });
        registerForContextMenu(lv);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Log.e(TAG, "" + modelIds.get(acmi.position));
        menu.add(0,modelIds.get(acmi.position), 0, "Add To Favorites");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        try {
            if (item.getTitle().equals("Add To Favorites")) {
                dbAdapter.open();
                dbAdapter.addToFavorites(item.getItemId(), brand);
                dbAdapter.close();
                return true;
            } else if (item.getTitle().equals("Monkey")) {
                return true;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }
}
