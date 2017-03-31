package bjchau.github.remote;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;
import java.sql.SQLException;

class DataAdapter {
    private static final String TAG = "DataAdapter";
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    DataAdapter(Context context){
        dbHelper = new DatabaseHelper(context);
    }

    DataAdapter createDatabase() throws IOException {
        try{
            dbHelper.createDatabase();
        }catch(IOException e){
            Log.e(TAG, e.toString() + "Unable to create database.");
            throw new Error("UnableToCreateDatabase");
        }
        return this;
    }

    public DataAdapter open() throws SQLException {
        try{
            dbHelper.openDatabase();
            dbHelper.close();
            db = dbHelper.getReadableDatabase();
        }catch(SQLException e){
            Log.e(TAG, "open >>" + e.toString());
            throw e;
        }
        return this;
    }

    void close(){
        dbHelper.close();
    }

    Cursor getBrands(){
        String sql = "SELECT name FROM brands";
        Cursor cursor = db.rawQuery(sql,null);
        if(cursor != null){
            cursor.moveToNext();
        }
        return cursor;
    }

    Cursor getModels(int brandid){
        String sql = "SELECT modelid, name FROM models WHERE brandid=" + brandid;
        Cursor cursor = db.rawQuery(sql,null);
        if(cursor != null){
            cursor.moveToNext();
        }
        return cursor;
    }
    Cursor getButtons(int modelid){
        String sql = "SELECT name, pattern FROM buttons WHERE modelid=" + modelid;
        Cursor cursor = db.rawQuery(sql,null);
        if(cursor != null){
            cursor.moveToNext();
        }
        return cursor;
    }

    Cursor getFavorites(){
        String sql = "SELECT modelid, brandname FROM favorites";
        Cursor cursor = db.rawQuery(sql,null);
        if(cursor != null){
            cursor.moveToNext();
        }
        return cursor;
    }
    void addToFavorites(int modelid, int brandid){
        String createTable = "CREATE TABLE IF NOT EXISTS favorites (id INTEGER PRIMARY KEY AUTOINCREMENT, brandname TEXT, modelid INTEGER, FOREIGN KEY(modelid) REFERENCES models(modelid) )";
        db.execSQL(createTable);

        String sql = "INSERT INTO favorites (brandname, modelid) SELECT brands.name, models.modelid FROM models INNER JOIN brands ON models.modelid=" + modelid + " AND brands.brandid=" + brandid;
        db.execSQL(sql);
    }
    void removeFromFavorites(int modelid){
        String sql = "DELETE FROM favorites WHERE modelid="+modelid;
        db.execSQL(sql);
    }
    void clearFavorites() {
        String sql = "DROP TABLE favorites";
        db.execSQL(sql);
    }
}
