package porqueno.muzeigooglephotos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.test.runner.AndroidJUnit4;
import android.test.RenamingDelegatingContext;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.File.ImageMediaMetadata.Location;

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
	private PhotosModelDbHelper mDatabaseHelper;
	RenamingDelegatingContext mRenamingDelegatingContext;

	private File.ImageMediaMetadata getMetaForGeo(Double lat, Double lng){
		File.ImageMediaMetadata meta = new File.ImageMediaMetadata();
		Location location = new Location();
		location.setLatitude(lat);
		location.setLongitude(lng);
		meta.setLocation(location);
		return meta;
	}

	private File createPhotoOne() {
		File photoOne = new File();
		photoOne.setId("1");
		photoOne.setImageMediaMetadata(getMetaForGeo(1.0, -1.0));
		photoOne.setCreatedTime(new DateTime(Calendar.getInstance().getTime()));
		return photoOne;
	}

	private File createPhotoTwo() {
		File photoTwo = new File();
		photoTwo.setId("2");
		photoTwo.setImageMediaMetadata(getMetaForGeo(2.0, -2.0));
		photoTwo.setCreatedTime(new DateTime(Calendar.getInstance().getTime()));
		return photoTwo;
	}

	private void seedDb(SQLiteOpenHelper databaseHelper, File file) {
		SQLiteDatabase db = databaseHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(PhotosModelContract.PhotoEntry.COLUMN_NAME_PHOTO_ID, file.getId());
		if (file.getImageMediaMetadata() != null){
			values.put(PhotosModelContract.PhotoEntry.COLUMN_NAME_LAT, file.getImageMediaMetadata().getLocation().getLatitude());
			values.put(PhotosModelContract.PhotoEntry.COLUMN_NAME_LNG, file.getImageMediaMetadata().getLocation().getLongitude());
		}
		db.insert(
				PhotosModelContract.PhotoEntry.TABLE_NAME,
				null,
				values);
		db.close();
	}

	private RenamingDelegatingContext getTestContext(){
		if (mRenamingDelegatingContext == null){
			mRenamingDelegatingContext = new RenamingDelegatingContext(getTargetContext(), "test_");
		}
		return mRenamingDelegatingContext;
	}

	@Before
	public void setUp() throws Exception {
		getTargetContext().deleteDatabase("test_" + PhotosModelDbHelper.DATABASE_NAME);
	}

	@After
	public void tearDown() throws Exception {
		if (mDatabaseHelper != null){
			mDatabaseHelper.close();
		}
	}

	@Test
	public void savePhotos() throws Exception {
		mDatabaseHelper = new PhotosModelDbHelper(getTestContext());
		ArrayList<File> photos = new ArrayList<>();
		File photoOne = createPhotoOne();
		seedDb(mDatabaseHelper, photoOne);
		photos.add(createPhotoOne());
		photos.add(createPhotoTwo());
		mDatabaseHelper.savePhotos(photos);

		SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM " + PhotosModelContract.PhotoEntry.TABLE_NAME, null);
		c.moveToFirst();
		assertThat(c.getCount(), is(2));
		assertTrue(mDatabaseHelper.getId(c).equals("1"));
		assertThat(mDatabaseHelper.getViewed(c), is(false));
		assertThat(mDatabaseHelper.getLatitude(c), is(1.0));
		assertThat(mDatabaseHelper.getLongitude(c), is(-1.0));
		c.moveToNext();
		assertTrue(mDatabaseHelper.getId(c).equals("2"));
		assertThat(mDatabaseHelper.getViewed(c), is(false));
		assertThat(mDatabaseHelper.getLatitude(c), is(2.0));
		assertThat(mDatabaseHelper.getLongitude(c), is(-2.0));
		c.close();
		db.close();
	}

	@Test
	public void getNextPhotoEmptyDB() throws Exception {
		mDatabaseHelper = new PhotosModelDbHelper(getTestContext());
		PhotoModel emptyPhoto = mDatabaseHelper.getNextPhoto();
		assertNull(emptyPhoto);
	}

	@Test
	public void getNextPhoto() throws Exception {
		mDatabaseHelper = new PhotosModelDbHelper(getTestContext());
		seedDb(mDatabaseHelper, createPhotoOne());
		seedDb(mDatabaseHelper, createPhotoTwo());

		// Get photo and set viewed
		PhotoModel firstPhoto = mDatabaseHelper.getNextPhoto();
		SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
		String[] args = { firstPhoto.getId() };
		Cursor c = db.rawQuery("SELECT * FROM " + PhotosModelContract.PhotoEntry.TABLE_NAME + " WHERE photo_id=?", args);
		assertThat(c.getCount(), is(1));
		c.moveToFirst();
		assertTrue(firstPhoto.getViewed());

		// Get other photo
		PhotoModel secondPhoto = mDatabaseHelper.getNextPhoto();
		assertThat(firstPhoto.getId(), not(secondPhoto.getId()));
		assertTrue(secondPhoto.getViewed());
		c.close();
		db.close();
	}

	@Test
	public void getNextPhotoReset() throws Exception {
		mDatabaseHelper = new PhotosModelDbHelper(getTestContext());
		seedDb(mDatabaseHelper, createPhotoOne());
		seedDb(mDatabaseHelper, createPhotoTwo());
		mDatabaseHelper.getNextPhoto();
		mDatabaseHelper.getNextPhoto();

		SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM " + PhotosModelContract.PhotoEntry.TABLE_NAME + " WHERE "+ PhotosModelContract.PhotoEntry.COLUMN_NAME_PHOTO_USED + "= 1", null);
		assertThat(c.getCount(), is(2));

		PhotoModel loopAroundPhoto = mDatabaseHelper.getNextPhoto();
		assertThat(loopAroundPhoto.getId(), anyOf(is("1"), is("2")));
		c.close();
		db.close();
	}

	@Test
	public void migrationV1ToV2() throws Exception {
		PhotosModelDbHelperV1 oldDatabaseHelper = new PhotosModelDbHelperV1(getTestContext());
		File photoOne = new File();
		photoOne.setId("1");
		photoOne.setCreatedTime(new DateTime(Calendar.getInstance().getTime()));
		seedDb(oldDatabaseHelper, photoOne);

		SQLiteDatabase oldDb = oldDatabaseHelper.getReadableDatabase();
		Cursor c = oldDb.rawQuery("SELECT * FROM " + PhotosModelContract.PhotoEntry.TABLE_NAME, null);
		assertThat(c.getCount(), is(1));
		c.moveToFirst();

		assertThat(c.getColumnIndex(PhotosModelContract.PhotoEntry.COLUMN_NAME_LAT), is(-1));
		assertThat(oldDb.getVersion(), is(1));

		c.close();
		oldDb.close();

		mDatabaseHelper = new PhotosModelDbHelper(getTestContext());
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		assertThat(db.getVersion(), is(2));
		c = db.rawQuery("SELECT * FROM " + PhotosModelContract.PhotoEntry.TABLE_NAME, null);
		assertThat(c.getCount(), is(1));
		c.moveToFirst();
		assertThat(c.getColumnIndex(PhotosModelContract.PhotoEntry.COLUMN_NAME_LAT), not(-1));
		mDatabaseHelper.close();
		db.close();
	}
}