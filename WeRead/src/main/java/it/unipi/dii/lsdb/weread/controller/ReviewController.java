package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.model.*;
import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class ReviewController {
    @FXML private AnchorPane reviewPane;
    @FXML private Label reviewAuthor;
    @FXML private Label rating;
    @FXML private Label numLikes;
    @FXML private Text text;
    @FXML private Label dateTime;
    @FXML private Button likeButton;
    @FXML private VBox likersBox;

    private MongoDBDriver mongoDBDriver;
    private Review review;
    private Session session;
    private boolean likersShowed;

    public void initialize(){
        mongoDBDriver = MongoDBDriver.getInstance();
        session = Session.getInstance();
        likersShowed = false; //true if the review box is showing the likers list
        //setta i vari click ai label
        reviewAuthor.setOnMouseClicked(mouseEvent -> showUserPage(mouseEvent));
        numLikes.setOnMouseClicked(mouseEvent -> showLikers(mouseEvent));
    }

    public void setReview(Review r){
        this.review = r;
        reviewAuthor.setText(review.getReviewer());
        rating.setText(review.getRating() + "/5");
        numLikes.setText(String.valueOf(review.getNumLikes()));
        text.setText(review.getText());
        SimpleDateFormat dateFormat = new SimpleDateFormat("H:m dd/MM/yyyy");
        dateTime.setText(dateFormat.format(review.getTime()));
        //check if the logged user is already a liker of this review
        setLikeButton(review.getLikers().contains(session.getLoggedUser().getUsername()));
        //if the logged user has written the comment, he/she can't like it
        if(review.getReviewer().equals(session.getLoggedUser().getUsername()))
            likeButton.setDisable(true);
    }

    private void showUserPage(MouseEvent mouseEvent){
        User u = mongoDBDriver.getUserInfo(review.getReviewer());
        UserPageController userPageController = (UserPageController) Utils.changeScene("/userPage.fxml", mouseEvent);
        //userPageController.setUser(u);
    }

    /*private void showBookPage(MouseEvent mouseEvent){
        Book b = mongoDBDriver.getBookInformation(review.getReviewedBookIsbn());
        BookPageController bookPageController = (BookPageController) Utils.changeScene("/bookPage.fxml", mouseEvent);
        bookPageController.setBook(b);
    }*/

    private void showLikers(MouseEvent mouseEvent){
        if(!likersShowed) {
            Utils.addLikers(likersBox, review.getLikers());
            likersShowed = true;
        }
        else{
            likersBox.getChildren().clear();
            likersShowed = false;
        }
    }

    private void setLikeButton(boolean liked){
        if(liked) {
            likeButton.setText("UNLIKE");
            likeButton.setOnMouseClicked(mouseEvent -> unlikeReview(mouseEvent));
        }
        else {
            likeButton.setText("LIKE");
            likeButton.setOnMouseClicked(mouseEvent -> likeReview(mouseEvent));
        }
    }

    private void likeReview(MouseEvent mouseEvent){
        if(mongoDBDriver.addLikeReview(review, session.getLoggedUser().getUsername())) {
            setLikeButton(true);
            int likes = Integer.parseInt(numLikes.getText());
            numLikes.setText(String.valueOf(likes+1));
            session.updateLoggedUserInfo(session.getLoggedUser());
        }
        else
            Utils.showErrorAlert("Error in liking the review");
    }

    private void unlikeReview(MouseEvent mouseEvent){
        if(mongoDBDriver.removeLikeReview(review, session.getLoggedUser().getUsername())) {
            setLikeButton(false);
            int likes = Integer.parseInt(numLikes.getText());
            numLikes.setText(String.valueOf(likes-1));
            session.updateLoggedUserInfo(session.getLoggedUser());
        }
        else
            Utils.showErrorAlert("Error in unliking the review");
    }

}
