package org.magnum.mobilecloud.video;

import org.magnum.mobilecloud.video.model.UserVideoRating;
import org.magnum.mobilecloud.video.model.Video;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;

/**
 * Created by Marco on 2016-05-29.
 */
public interface UserVideoRatingRepository extends CrudRepository<UserVideoRating, Long> {

    UserVideoRating findByVideoAndUser(Video v, String u);
    Collection<UserVideoRating> findByVideo(Video v);

}
