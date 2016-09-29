package porqueno.muzeigooglephotos;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import porqueno.muzeigooglephotos.models.PhotosModelContract;

/**
 * Created by jacob on 9/28/16.
 */

public class PhotosModelDbHelperV1 extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Photos.db";

	public PhotosModelDbHelperV1(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + PhotosModelContract.PhotoEntry.TABLE_NAME + " (" +
				PhotosModelContract.PhotoEntry.COLUMN_NAME_PHOTO_ID + " TEXT PRIMARY KEY," +
				PhotosModelContract.PhotoEntry.COLUMN_NAME_PHOTO_USED + " INTEGER DEFAULT 0," +
				PhotosModelContract.PhotoEntry.COLUMN_NAME_CREATED + " LONG" +
				" )");
	}
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(PhotosModelContract.SQL_DELETE_ENTRIES);
		onCreate(db);
	}
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}

