package it.unipi.dii.lsdb.weread.model;

import java.time.LocalDateTime;
import java.util.List;

public class Review {
    private String reviewer;
    private String title;
    private String text;
    private int rating;
    private LocalDateTime time;
    private int numLikes;
    private List likers;


    public Review(String reviewer, String title, String text, int rating, LocalDateTime time, int numLikes, List likers) {
        this.reviewer = reviewer;
        this.title = title;
        this.text = text;
        this.rating = rating;
        this.time = time;
        this.likers = likers;
    }

    public String getReviewer() {
        return reviewer;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public int getRating() {
        return rating;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public List getLikers() {
        return likers;
    }
}
