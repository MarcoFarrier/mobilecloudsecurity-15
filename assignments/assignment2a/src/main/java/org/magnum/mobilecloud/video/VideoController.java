package org.magnum.mobilecloud.video;

import org.magnum.mobilecloud.video.VideoRepository;
import org.magnum.mobilecloud.video.controller.VideoFileManager;
import org.magnum.mobilecloud.video.model.AverageVideoRating;
import org.magnum.mobilecloud.video.model.UserVideoRating;
import org.magnum.mobilecloud.video.model.Video;
import org.magnum.mobilecloud.video.model.VideoStatus;
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
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Marco on 2016-05-18.
 */

@Controller
public class VideoController {

    private static final AtomicLong currentId = new AtomicLong(0L);
    private Map<Long,Video> videos = new HashMap<Long, Video>();

    @Autowired
    VideoRepository videoRepository;

    @Autowired
    UserVideoRatingRepository userVideoRatingRepository;

    @Autowired
    private VideoFileManager myManager;


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

    //POST /video/{id}/data
    @RequestMapping(value = "/video/{id}/data", method=RequestMethod.POST)
    public @ResponseBody VideoStatus postVideoID(@PathVariable("id") Long id, @RequestParam("data") MultipartFile dataFile, HttpServletResponse response, Principal p)
    {

        if(!videoRepository.exists(id))
        {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        if(p.getName() != videoRepository.findOne(id).getOwner())
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        try {
            saveSomeVideo(videoRepository.findOne(id), dataFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        VideoStatus toReturn = new VideoStatus(VideoStatus.VideoState.READY);
        return toReturn;

    }

    //GET /video/{id}/data
    @RequestMapping(value = "/video/{id}/data", method = RequestMethod.GET)
    public void getVideoData(@PathVariable("id") Long videoID, HttpServletResponse response)
    {
        if(!videoRepository.exists(videoID))
        {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if(!myManager.hasVideoData(videoRepository.findOne(videoID)))
        {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        serveSomeVideo(videoRepository.findOne(videoID), response);

    }

    //POST /video/{id}/rating/{rating}
    @RequestMapping(value = "/video/{id}/rating/{rating}", method = RequestMethod.POST)
    public void rateVideo(@PathVariable("id") Long videoID, @PathVariable("rating") double rating, Principal p, HttpServletResponse response)
    {
        if(!videoRepository.exists(videoID))
        {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Video currentVideo = videoRepository.findOne(videoID);

        UserVideoRating videoRating = new UserVideoRating(currentVideo, rating, p.getName());

        UserVideoRating currentRating = userVideoRatingRepository.findByVideoAndUser(currentVideo, p.getName());

        if(currentRating != null)
        {
            userVideoRatingRepository.delete(currentRating);
        }

        userVideoRatingRepository.save(videoRating);

        response.setStatus(HttpServletResponse.SC_OK);
        return;

    }

    //GET /video/{id}/rating
    @RequestMapping(value = "/video/{id}/rating", method = RequestMethod.GET)
    public @ResponseBody AverageVideoRating getAverageVideoRating(@PathVariable("id") Long videoID, HttpServletResponse response)
    {
        Collection<UserVideoRating> videoRatings = userVideoRatingRepository.findByVideo(videoRepository.findOne(videoID));

        int numberOfRatings = videoRatings.size();
        int totalRating = 0;

        for(UserVideoRating uvr : videoRatings)
        {
            totalRating += uvr.getRating();
        }

        double averageRating = totalRating / numberOfRatings;

        AverageVideoRating toReturn = new AverageVideoRating(averageRating, videoID, numberOfRatings);
        return toReturn;
    }


    public void serveSomeVideo(Video v, HttpServletResponse response)
    {
        try {
            myManager.copyVideoData(v, response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void saveSomeVideo(Video v, MultipartFile videoData) throws IOException {
        myManager.saveVideoData(v, videoData.getInputStream());
    }
}
