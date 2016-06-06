package vandy.mooc.model.mediator.webdata;

import java.util.Collection;


import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Streaming;
import retrofit.mime.TypedFile;

/**
 * This interface defines an API for a VideoSvc. The
 * interface is used to provide a contract for client/server
 * interactions. The interface is annotated with Retrofit
 * annotations so that clients can automatically convert the
 * 
 * 
 * @author jules
 *
 */
public interface VideoSvcApi {

	public static final String TITLE_PARAMETER = "title";

	public static final String DURATION_PARAMETER = "duration";

	public static final String TOKEN_PATH = "/oauth/token";

	// The path where we expect the VideoSvc to live
	public static final String VIDEO_SVC_PATH = "/video";

	// The path to search videos by title
	public static final String VIDEO_TITLE_SEARCH_PATH = VIDEO_SVC_PATH + "/search/findByName";

	// The path to search videos by title
	public static final String VIDEO_DURATION_SEARCH_PATH = VIDEO_SVC_PATH + "/search/findByDurationLessThan";

	@GET(VIDEO_SVC_PATH)
	public Collection<Video> getVideoList();

	@GET(VIDEO_SVC_PATH + "/{id}")
	public Video getVideoById(@Path("id") long id);

	@POST(VIDEO_SVC_PATH)
	public Video addVideo(@Body Video v);

	//adding empty string to get around retrofit failing when POST with empty body
	@POST(VIDEO_SVC_PATH + "/{id}/like")
	public Void likeVideo(@Path("id") long id, @Body String emptyString);

	@POST(VIDEO_SVC_PATH + "/{id}/unlike")
	public Void unlikeVideo(@Path("id") long id, @Body String emptyString);

	@GET(VIDEO_SVC_PATH + "/{id}/likedby")
	public Collection<String> getUsersWhoLikedVideo(@Path("id") long id);
}