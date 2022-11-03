package it.unipi.dii.lsdb.weread.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Review {
    private String reviewId; //<reviewer>:<book_isbn>
    private String title;
    private String text;
    private int rating;
    private LocalDateTime time;
    private int numLikes;
    private List<String> likers;


    public Review(String reviewId, String title, String text, int rating, LocalDateTime time, int numLikes, List<String> likers) {
        this.reviewId = reviewId;
        this.title = title;
        this.text = text;
        this.rating = rating;
        this.time = time;
        this.likers = likers;
        this.numLikes = numLikes;
    }

    //constructor for a new review, with some variables that must be initialized to default values
    public Review(String username, String isbn, String text, int rating, String title){
        this.reviewId = username + ":" + isbn;
        this.text = text;
        this.rating = rating;
        this.title = title;
        time = LocalDateTime.now();
        numLikes = 0;
        likers = new ArrayList<>();
    }

    public String getReviewId() { return reviewId; }

    public String getReviewer() {
        return reviewId.split(":")[0];
    }

    public String getBookTitle() {
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

    public int getNumLikes() { return numLikes; }

    public List<String> getLikers() {
        return likers;
    }
}
