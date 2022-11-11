package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.model.Book;
import it.unipi.dii.lsdb.weread.model.ReadingList;
import it.unipi.dii.lsdb.weread.model.Review;
import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class ReviewSnapshotController {
    @FXML private Label titleReview;
    @FXML private Label textReview;
    @FXML private Label ratingReview;
    @FXML private AnchorPane reviewSnapshotPane;

    private Review review;
    private MongoDBDriver mongoDBDriver;

    public void initialize ()
    {
        mongoDBDriver = MongoDBDriver.getInstance();
        reviewSnapshotPane.setOnMouseClicked(mouseEvent -> showBookPage(mouseEvent));
    }

    private void showBookPage(MouseEvent mouseEvent){
        Book book = mongoDBDriver.getBookInformation(review.getReviewedBookIsbn());
        BookPageController bookPageController = (BookPageController) Utils.changeScene("/bookPage.fxml", mouseEvent);
        bookPageController.setBook(book);
    }


    public void setReview(Review r){
        this.review = r;
        titleReview.setText(r.getBookTitle());
        textReview.setText(r.getText());
        ratingReview.setText(Integer.toString(r.getRating()));
    }

}
