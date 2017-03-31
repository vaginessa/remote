package bjchau.github.remote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

import static android.content.ContentValues.TAG;

class DatabaseHelper extends SQLiteOpenHelper {
    private final Context context;
    private static String DB_NAME = "remote.db";
    private static String DB_PATH = "";
    private SQLiteDatabase database;

    DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);

        if(Build.VERSION.SDK_INT >= 17){
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        }else{
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases";
        }
        this.context = context;
    }

    void createDatabase() throws IOException{
        boolean databaseExists = checkDatabase();
        if(!databaseExists){
            this.getReadableDatabase();
            this.close();
            try{
                copyDatabase();
                Log.e(TAG, "createDatabase called.");
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }
    private boolean checkDatabase(){
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }
    private void copyDatabase() throws IOException{
        InputStream input = context.getAssets().open(DB_NAME);

        OutputStream output = new FileOutputStream(DB_PATH + DB_NAME);
        byte[] buffer = new byte[1024];
        int length;
        while((length = input.read(buffer)) > 0){
            output.write(buffer,0,length);
        }
        output.flush();
        output.close();
        input.close();
    }

    boolean openDatabase() throws SQLException {
        String path = DB_PATH + DB_NAME;
        database = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return database != null;
    }

    public synchronized void close(){
        if(database != null)
            database.close();
        super.close();
    }

    @Override public void onCreate(SQLiteDatabase db){}
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
