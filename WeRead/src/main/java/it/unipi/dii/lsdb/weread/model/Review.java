package it.unipi.dii.lsdb.weread.model;

import java.time.LocalDateTime;
import java.util.List;

public class Review {
    private String reviewId; //<reviewer>:<book_isbn>
    private String title;
    private String text;
    private int rating;
    private LocalDateTime time;
    private int numLikes;
    private List<User> likers;


    public Review(String reviewId, String title, String text, int rating, LocalDateTime time, int numLikes, List<User> likers) {
        this.reviewId = reviewId;
        this.title = title;
        this.text = text;
        this.rating = rating;
        this.time = time;
        this.likers = likers;
        this.numLikes = numLikes;
    }

    public String getReviewer() {
        return reviewId.split(":")[0];
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

    public List<User> getLikers() {
        return likers;
    }
}
