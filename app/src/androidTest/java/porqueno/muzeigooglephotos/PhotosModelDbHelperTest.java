package porqueno.muzeigooglephotos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import com.google.api.services.drive.model.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class PhotosModelDbHelperTest {
	private PhotosModelDbHelper database;

	private File createPhotoOne() {
		File photoOne = new File();
		photoOne.setId("1");
		return photoOne;
	}

	private File createPhotoTwo() {
		File photoTwo = new File();
		photoTwo.setId("2");
		return photoTwo;
	}

	private void seedDb(File file) {
		SQLiteDatabase db = database.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(PhotosModelContract.PhotoEntry.COLUMN_NAME_PHOTO_ID, file.getId());
		db.insert(
				PhotosModelContract.PhotoEntry.TABLE_NAME,
				null,
				values);
		db.close();
	}

	@Before
	public void setUp() throws Exception {
		getTargetContext().deleteDatabase(PhotosModelDbHelper.DATABASE_NAME);
		RenamingDelegatingContext context = new RenamingDelegatingContext(getTargetContext(), "test_");
		database = new PhotosModelDbHelper(context);
	}

	@After
	public void tearDown() throws Exception {
		database.close();
	}

	@Test
	public void savePhotosThatDoNotAlreadyExist() throws Exception {
		ArrayList<File> photos = new ArrayList<>();
		File photoOne = createPhotoOne();
		seedDb(photoOne);
		photos.add(createPhotoOne());
		photos.add(createPhotoTwo());
		database.savePhotos(photos);
		SQLiteDatabase db = database.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM " + PhotosModelContract.PhotoEntry.TABLE_NAME, null);
		c.moveToFirst();
		assertThat(c.getCount(), is(2));
		assertTrue(database.getId(c).equals("1"));
		assertThat(database.getViewed(c), is(false));
		c.moveToNext();
		assertTrue(database.getId(c).equals("2"));
		assertThat(database.getViewed(c), is(false));
		c.close();
	}
}