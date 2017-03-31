package bjchau.github.remote;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.ConsumerIrManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

public class Brands extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "IR Blaster";
    final Brands s = this;
    ConsumerIrManager mCIR;
    List<String> list = new ArrayList<>();
    ArrayAdapter<String> listAdapter;
    DataAdapter dbAdapter;
    private GoogleApiClient client;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setTitle("Remote Control");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mCIR = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        ListView lv = (ListView) findViewById(R.id.list);
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        lv.setAdapter(listAdapter);

        try{
            dbAdapter = new DataAdapter(getApplicationContext());
            dbAdapter.createDatabase();

            dbAdapter.open();
            Cursor cursor = dbAdapter.getBrands();
            dbAdapter.close();

            list.clear();
            if (cursor != null) {
                do {
                    list.add(cursor.getString(0));
                } while (cursor.moveToNext());
                cursor.close();
            }
        }catch(Exception e){
            Log.e(TAG, e.getMessage());
        }

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(100);

                Intent intent = new Intent(s, Models.class);
                intent.putExtra("id", position+1);
                intent.putExtra("name", list.get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(getApplicationContext(), "Press BACK again to exit.", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run(){
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_all_brands) {

        } else if (id == R.id.nav_favorites) {
            Intent intent = new Intent(s, Favorites.class);
            startActivity(intent);
        } else if (id == R.id.nav_rooms) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_source_code) {

        } else if (id == R.id.nav_share) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Brands")
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
