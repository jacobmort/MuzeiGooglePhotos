package porqueno.muzeigooglephotos.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;

import java.util.List;

import porqueno.muzeigooglephotos.util.TimeHelpers;

/**
 * Created by jacob on 7/10/16.
 */
public class PhotosModelDbHelper extends SQLiteOpenHelper {
	private static final String TAG = "PhotosModelDbHelper";
	private static final int DATABASE_VERSION = 2;
	public static final String DATABASE_NAME = "Photos.db";

	public PhotosModelDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	private static PhotosModelDbHelper instance;

	public static synchronized PhotosModelDbHelper getHelper(Context context)
	{
		if (instance == null)
			instance = new PhotosModelDbHelper(context);

		return instance;
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(PhotosModelContract.SQL_CREATE_ENTRIES);
	}
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (newVersion == 2){
			db.execSQL(PhotosModelContract.SQL_ADD_LAT);
			db.execSQL(PhotosModelContract.SQL_ADD_LNG);
		}
	}
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(PhotosModelContract.SQL_DELETE_ENTRIES);
		onCreate(db);
	}

	public void savePhotos(List<File> photos){
		SQLiteDatabase db = this.getWritableDatabase();
		String sql = "INSERT OR IGNORE INTO "+ PhotosModelContract.PhotoEntry.TABLE_NAME +" VALUES (?,?,?,?,?);";
		SQLiteStatement statement = db.compileStatement(sql);
		try {
			db.beginTransaction();
			for (File photo: photos) {
				DateTime createdTime = getMetaTimeOrCreatedTime(photo);
				statement.clearBindings();
				statement.bindString(1, photo.getId());
				statement.bindLong(2, 0);
				statement.bindLong(3, createdTime.getValue());
				if (photo.getImageMediaMetadata() == null || photo.getImageMediaMetadata().getLocation() == null){
					statement.bindNull(4);
					statement.bindNull(5);
				} else {
					statement.bindDouble(4, photo.getImageMediaMetadata().getLocation().getLatitude());
					statement.bindDouble(5, photo.getImageMediaMetadata().getLocation().getLongitude());
				}
				statement.execute();
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	public PhotoModel getNextPhoto() {
		return this.getNextPhoto(false);
	}

	@SuppressWarnings("WeakerAccess")
	public PhotoModel getNextPhoto(boolean reset) {
		SQLiteDatabase db = this.getReadableDatabase();
		PhotoModel photo = null;
		Cursor c = db.rawQuery("SELECT * FROM " + PhotosModelContract.PhotoEntry.TABLE_NAME + " WHERE " + PhotosModelContract.PhotoEntry.COLUMN_NAME_PHOTO_USED + " = 0 ORDER BY RANDOM() LIMIT 1", null);
		try{
			if (c.moveToFirst()) {
				photo = new PhotoModel(
						this.getId(c),
						this.getCreatedTime(c),
						this.getLatitude(c),
						this.getLongitude(c),
						true
				);
				ContentValues values = new ContentValues();
				values.put(PhotosModelContract.PhotoEntry.COLUMN_NAME_PHOTO_USED, 1);

				String selection = PhotosModelContract.PhotoEntry.COLUMN_NAME_PHOTO_ID + " = ?";
				String[] selectionArgs = { photo.getId() };

				db.update(
						PhotosModelContract.PhotoEntry.TABLE_NAME,
						values,
						selection,
						selectionArgs
				);
			} else if (!reset){
				PhotosModelDbHelper.resetViewedStatus(db);
				return this.getNextPhoto(true);
			}
		} finally {
			if (c != null) {
				c.close();
			}
			db.close();
		}
		return photo;
	}

	private static void resetViewedStatus(SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(PhotosModelContract.PhotoEntry.COLUMN_NAME_PHOTO_USED, false);

		db.update(
				PhotosModelContract.PhotoEntry.TABLE_NAME,
				values,
				null,
				null);
	}

	public String getId(Cursor c) {
		return c.getString(
				c.getColumnIndexOrThrow(
						PhotosModelContract.PhotoEntry.COLUMN_NAME_PHOTO_ID
				)
		);
	}

	public boolean getViewed(Cursor c) {
		int viewed = (
				c.getInt(
						c.getColumnIndexOrThrow(
								PhotosModelContract.PhotoEntry.COLUMN_NAME_PHOTO_USED
						)
				)
		);
		return viewed  == 1;
	}

	private long getCreatedTime(Cursor c) {
		return c.getLong(
				c.getColumnIndexOrThrow(
						PhotosModelContract.PhotoEntry.COLUMN_NAME_CREATED
				)
		);
	}

	public double getLatitude(Cursor c){
		return c.getDouble(
				c.getColumnIndexOrThrow(
						PhotosModelContract.PhotoEntry.COLUMN_NAME_LAT
				)
		);
	}

	public double getLongitude(Cursor c){
		return c.getDouble(
				c.getColumnIndexOrThrow(
						PhotosModelContract.PhotoEntry.COLUMN_NAME_LNG
				)
		);
	}

	public static DateTime getMetaTimeOrCreatedTime(File file) {
		if (file.getImageMediaMetadata() != null && file.getImageMediaMetadata().getTime() != null) {
			return new DateTime(
					TimeHelpers.getDateFromTimeMeta(file.getImageMediaMetadata().getTime())
			);
		} else {
			return file.getCreatedTime();
		}
	}

	public int getPhotoCount(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT count(*) FROM " + PhotosModelContract.PhotoEntry.TABLE_NAME, null);
		c.moveToFirst();
		int count =  c.getInt(0);
		c.close();
		db.close();
		return count;
	}

	public int getPhotoSeenCount(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT count(*) FROM " + PhotosModelContract.PhotoEntry.TABLE_NAME + " WHERE " + PhotosModelContract.PhotoEntry.COLUMN_NAME_PHOTO_USED +"=1", null);
		c.moveToFirst();
		int count =  c.getInt(0);
		c.close();
		db.close();
		return count;
	}
}
