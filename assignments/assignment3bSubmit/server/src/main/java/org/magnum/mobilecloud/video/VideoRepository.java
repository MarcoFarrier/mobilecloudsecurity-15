package org.magnum.mobilecloud.video;

import org.magnum.mobilecloud.video.repository.Video;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Marco on 2016-05-28.
 */

@Repository
public interface VideoRepository extends CrudRepository<Video, Long> {
}
