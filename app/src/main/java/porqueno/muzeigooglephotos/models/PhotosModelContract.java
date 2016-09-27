package porqueno.muzeigooglephotos.models;

import android.provider.BaseColumns;

import static porqueno.muzeigooglephotos.models.PhotosModelContract.PhotoEntry.COLUMN_NAME_LAT;
import static porqueno.muzeigooglephotos.models.PhotosModelContract.PhotoEntry.COLUMN_NAME_LNG;

/**
 * Created by jacob on 7/10/16.
 */
public class PhotosModelContract {
		// To prevent someone from accidentally instantiating the contract class,
		// give it an empty constructor.
		public PhotosModelContract() {}

		/* Inner class that defines the table contents */
		public static abstract class PhotoEntry implements BaseColumns {
			public static final String TABLE_NAME = "photos";
			public static final String COLUMN_NAME_PHOTO_ID = "photo_id";
			public static final String COLUMN_NAME_PHOTO_USED = "viewed";
			public static final String COLUMN_NAME_CREATED = "photo_created_at";
			public static final String COLUMN_NAME_LAT = "photo_latutide";
			public static final String COLUMN_NAME_LNG = "photo_longitude";
		}

	public static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + PhotoEntry.TABLE_NAME + " (" +
					PhotoEntry.COLUMN_NAME_PHOTO_ID + " TEXT PRIMARY KEY," +
					PhotosModelContract.PhotoEntry.COLUMN_NAME_PHOTO_USED + " INTEGER DEFAULT 0," +
					PhotoEntry.COLUMN_NAME_CREATED + " LONG" +
					COLUMN_NAME_LAT + " DOUBLE" +
					COLUMN_NAME_LNG + " DOUBLE" +
			" )";

	public static final String SQL_DELETE_ENTRIES =
			"DROP TABLE IF EXISTS " + PhotoEntry.TABLE_NAME;

	public static final String SQL_ADD_LAT =
			"ALTER TABLE  " + PhotoEntry.TABLE_NAME + " ADD COLUMN " + COLUMN_NAME_LAT + " DOUBLE";

	public static final String SQL_ADD_LNG =
			"ALTER TABLE  " + PhotoEntry.TABLE_NAME + " ADD COLUMN " + COLUMN_NAME_LNG + " DOUBLE";
}
