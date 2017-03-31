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

public class Favorites extends AppCompatActivity {
    private static final String TAG = "FAVORITES";
    ArrayList<String> list = new ArrayList<>();
    ArrayList<Integer> modelIds = new ArrayList<>();
    private Favorites d = this;
    DataAdapter dbAdapter;
    ArrayAdapter<String> listAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        setTitle("Favorites");
        ListView lv = (ListView) findViewById(R.id.list);
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);

        dbAdapter = new DataAdapter(getApplicationContext());

        lv.setAdapter(listAdapter);
        try {
            dbAdapter.open();
            Cursor cursor = dbAdapter.getFavorites();
            dbAdapter.close();

            if (cursor != null) {
                do {
                    modelIds.add(cursor.getInt(0));
                    list.add(cursor.getString(1));
                } while (cursor.moveToNext());
                cursor.close();
            }
        }catch(Exception e){
            Log.e(TAG, "Error getting favorites.");
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
        menu.add(0,acmi.position, 0, "Remove From Favorites");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        try {
            if (item.getTitle().equals("Remove From Favorites")) {
                dbAdapter.open();
                dbAdapter.removeFromFavorites(modelIds.get(item.getItemId()));
                dbAdapter.close();

                list.remove(item.getItemId());
                listAdapter.notifyDataSetChanged();
                return true;
            } else if (item.getTitle().equals("Clear Favorites")) {
                dbAdapter.open();
                dbAdapter.clearFavorites();
                dbAdapter.close();

                list.remove(item.getItemId());
                listAdapter.notifyDataSetChanged();
                return true;
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }
}
