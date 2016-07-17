package porqueno.muzeigooglephotos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Calendar;

import porqueno.muzeigooglephotos.models.PhotoModel;
import porqueno.muzeigooglephotos.models.PhotosModelContract;
import porqueno.muzeigooglephotos.models.PhotosModelDbHelper;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PhotosModelDbHelperInstrumentationTest {
	private PhotosModelDbHelper database;

	private File createPhotoOne() {
		File photoOne = new File();
		photoOne.setId("1");
		photoOne.setCreatedTime(new DateTime(Calendar.getInstance().getTime()));
		return photoOne;
	}

	private File createPhotoTwo() {
		File photoTwo = new File();
		photoTwo.setId("2");
		photoTwo.setCreatedTime(new DateTime(Calendar.getInstance().getTime()));
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
	public void savePhotos() throws Exception {
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
		db.close();
	}

	@Test
	public void getNextPhotoEmtpyDB() throws Exception {
		PhotoModel emptyPhoto = database.getNextPhoto();
		assertNull(emptyPhoto);
	}

	@Test
	public void getNextPhoto() throws Exception {
		seedDb(createPhotoOne());
		seedDb(createPhotoTwo());

		// Get photo and set viewed
		PhotoModel firstPhoto = database.getNextPhoto();
		SQLiteDatabase db = database.getReadableDatabase();
		String[] args = { firstPhoto.getId() };
		Cursor c = db.rawQuery("SELECT * FROM " + PhotosModelContract.PhotoEntry.TABLE_NAME + " WHERE photo_id=?", args);
		assertThat(c.getCount(), is(1));
		c.moveToFirst();
		assertTrue(firstPhoto.getViewed());

		// Get other photo
		PhotoModel secondPhoto = database.getNextPhoto();
		assertThat(firstPhoto.getId(), not(secondPhoto.getId()));
		assertTrue(secondPhoto.getViewed());
		c.close();
		db.close();
	}

	@Test
	public void getNextPhotoReset() throws Exception {
		seedDb(createPhotoOne());
		seedDb(createPhotoTwo());
		database.getNextPhoto();
		database.getNextPhoto();

		SQLiteDatabase db = database.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM " + PhotosModelContract.PhotoEntry.TABLE_NAME + " WHERE "+ PhotosModelContract.PhotoEntry.COLUMN_NAME_PHOTO_USED + "= 1", null);
		assertThat(c.getCount(), is(2));

		PhotoModel loopAroundPhoto = database.getNextPhoto();
		assertThat(loopAroundPhoto.getId(), anyOf(is("1"), is("2")));
		c.close();
		db.close();
	}
}