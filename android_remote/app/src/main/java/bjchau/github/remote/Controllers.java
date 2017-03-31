package bjchau.github.remote;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.ConsumerIrManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controllers extends Activity {
    private static final String TAG = "Controllers";
    ArrayList<String> names = new ArrayList<>();
    ArrayList<String> patterns = new ArrayList<>();
    ConsumerIrManager mCIR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        Intent intent = getIntent();
        int position = intent.getIntExtra("id", -1);
        String name = intent.getStringExtra("name");
        setTitle(name);

        DataAdapter dbAdapter= new DataAdapter(getApplicationContext());
        ListView lv = (ListView) findViewById(R.id.list);
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);

        lv.setAdapter(listAdapter);
        try {
            // Get models for specific brand
            dbAdapter.open();
            Cursor cursor = dbAdapter.getButtons(position);
            dbAdapter.close();

            if (cursor != null) {
                do {
                    names.add(cursor.getString(0));
                    patterns.add(cursor.getString(1));
                } while (cursor.moveToNext());
                cursor.close();
            }
        }catch(Exception e){
            Log.e(TAG, e.getMessage());
        }
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Calculate pulses
                int result[] = hex2dec(patterns.get(position));

                // Get frequency and pattern
                int frequency = result[0];
                int pattern[] = Arrays.copyOfRange(result, 1, result.length);

                Log.e(TAG, Arrays.toString(pattern));

                // Send signal
                mCIR = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);
                mCIR.transmit(frequency, pattern);
            }
        });
    }

    // This is a modified version of the function found here:
    //   https://github.com/rngtng/IrDude/blob/master/src/com/rngtng/irdude/MainActivity.java
    // This returns an array of integers with the pulses already taken into account, rather than a
    //   list of strings with values not taken into account.
    protected static int[] hex2dec(String irData) {
        List<String> list = new ArrayList<>(Arrays.asList(irData.split(" ")));
        list.remove(0);

        int pulses = (int) Math.floor(Integer.parseInt(list.remove(0),16) * 0.241246); // Pulses
        int frequency = 1000000 / pulses; // Frequency
        list.remove(0);
        list.remove(0);

        int pattern[] = new int[list.size() + 1];
        pattern[0] = frequency;
        for (int i = 1; i < pattern.length; i++)
            pattern[i] = pulses * Integer.parseInt(list.get(i - 1), 16);

        return pattern;
    }
}