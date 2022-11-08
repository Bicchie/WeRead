package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.model.ReadingList;
import it.unipi.dii.lsdb.weread.model.Review;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ReviewSnapshotController {
    @FXML private Label titleReview;
    @FXML private Label textReview;
    @FXML private Label ratingReview;

    private Review review;

    public void setReview(Review r){
        this.review = r;
        titleReview.setText(r.getBookTitle());
        textReview.setText(r.getText());
        ratingReview.setText(Integer.toString(r.getRating()));
    }

}
