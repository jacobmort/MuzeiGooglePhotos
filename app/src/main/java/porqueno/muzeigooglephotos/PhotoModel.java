package porqueno.muzeigooglephotos;

/**
 * Created by jacob on 7/10/16.
 */
public class PhotoModel {
	private static String GOOGLE_PHOTO_URL = "https://www.googleapis.com/drive/v3/files/";
	private String id;
	private boolean viewed;

	public PhotoModel(String id, boolean viewed) {
		this.id = id;
		this.viewed = viewed;
	}
	public String getId() { return id; }
	public boolean getViewed() { return viewed; }
	public String getUrl(String token) {
		return GOOGLE_PHOTO_URL + this.id + "?alt=media&access_token=" + token;
	}
}
