package org.magnum.mobilecloud.video;

import org.magnum.mobilecloud.video.VideoRepository;
import org.magnum.mobilecloud.video.repository.Video;
import org.omg.CORBA.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ShellProperties;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import retrofit.http.Path;

/**
 * Created by Marco on 2016-05-18.
 */

@Controller
public class VideoController {

    private static final AtomicLong currentId = new AtomicLong(0L);
    private Map<Long,Video> videos = new HashMap<Long, Video>();

    @Autowired
    VideoRepository videoRepository;

    //GET /video
    @RequestMapping(value = "/video", method = RequestMethod.GET)
    public @ResponseBody
    Collection<Video> getVideo()
    {
        return (Collection<Video>) videoRepository.findAll();
    }

    //POST /video
    @RequestMapping(value ="/video", method=RequestMethod.POST)
    public @ResponseBody Video postVideo(@RequestBody Video v, Principal p, HttpServletResponse responseServlet)
    {
        v.setOwner(p.getName());

        if(videoRepository.exists(v.getId()))
        {
            if(v.getOwner() != p.getName())
            {
                responseServlet.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return null;
            }
        }

        return videoRepository.save(v);
    }


    //GET /video/{id}
    @RequestMapping(value = "/video/{id}", method=RequestMethod.GET)
    public @ResponseBody Video getVideoID(@PathVariable("id") Long VideoID, HttpServletResponse servletResponse)
    {
        if(!videoRepository.exists(VideoID))
        {
            servletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        return videoRepository.findOne(VideoID);
    }

    //POST /video/{id}/like
    @RequestMapping(value = "video/{id}/like", method=RequestMethod.POST)
    public void likeVideo(@PathVariable("id") Long VideoID, Principal p, HttpServletResponse servletResponse)
    {
        if(!videoRepository.exists(VideoID))
        {
            servletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Video currentVideo = videoRepository.findOne(VideoID);

        if(currentVideo.getLikedBy().add(p.getName()))
        {
            currentVideo.setLikes(currentVideo.getLikedBy().size());
            videoRepository.save(currentVideo);

            servletResponse.setStatus(HttpServletResponse.SC_OK);
        }
        else {
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    //POST /video/{id}/unlike
    @RequestMapping(value = "video/{id}/unlike", method = RequestMethod.POST)
    public void unlikeVideo(@PathVariable("id") Long VideoID, Principal p, HttpServletResponse servletResponse)
    {
        if(!videoRepository.exists(VideoID))
        {
            servletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        Video currentVideo = videoRepository.findOne(VideoID);

        if(currentVideo.getLikedBy().contains(p.getName()))
        {
            currentVideo.getLikedBy().remove(p.getName());
            currentVideo.setLikes(currentVideo.getLikedBy().size());
            videoRepository.save(currentVideo);

            servletResponse.setStatus(HttpServletResponse.SC_OK);
        }
        else {
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    //GET /video/{id}/likedby
    @RequestMapping(value = "/video/{id}/likedby", method = RequestMethod.GET)
    public @ResponseBody Set<String> getLikedUser(@PathVariable("id") Long VideoID, HttpServletResponse servletResponse)
    {
        if(!videoRepository.exists(VideoID))
        {
            servletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        return videoRepository.findOne(VideoID).getLikedBy();
    }


    private String getDataUrl(long videoId){
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }

    private String getUrlBaseForLocalServer() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String base =
                "http://"+request.getServerName()
                        + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
        return base;
    }

}
