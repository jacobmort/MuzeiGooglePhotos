package porqueno.muzeigooglephotos;

import android.provider.BaseColumns;

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
		}

	public static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + PhotoEntry.TABLE_NAME + " (" +
					PhotoEntry.COLUMN_NAME_PHOTO_ID + " TEXT PRIMARY KEY," +
					PhotosModelContract.PhotoEntry.COLUMN_NAME_PHOTO_USED + " INTEGER DEFAULT 0" +
			" )";

	public static final String SQL_DELETE_ENTRIES =
			"DROP TABLE IF EXISTS " + PhotoEntry.TABLE_NAME;
}
