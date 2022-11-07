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
import javafx.scene.text.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class ReviewController {
    @FXML private AnchorPane reviewPane;
    @FXML private Label reviewAuthor;
    //@FXML private Label reviewedBook;
    @FXML private Label rating;
    @FXML private Label numLikes;
    @FXML private Text text;
    @FXML private Label dateTime;
    @FXML private Button likeButton;

    private MongoDBDriver mongoDBDriver;
    private Review review;
    private Session session;

    public void initialize(){
        mongoDBDriver = MongoDBDriver.getInstance();
        session = Session.getInstance();
        //setta i vari click ai label
        //reviewAuthor.setOnMouseClicked(mouseEvent -> showUserPage(mouseEvent));
        //reviewedBook.setOnMouseClicked(mouseEvent -> showBookPage(mouseEvent));
        numLikes.setOnMouseClicked(mouseEvent -> showLikers(mouseEvent));
    }

    public void setReview(Review r){
        this.review = r;
        reviewAuthor.setText(review.getReviewer());
        //reviewedBook.setText(review.getBookTitle());
        rating.setText(review.getRating() + "/5");
        numLikes.setText(String.valueOf(review.getNumLikes()));
        text.setText(review.getText());
        SimpleDateFormat dateFormat = new SimpleDateFormat("H:m dd/MM/yyyy");
        dateTime.setText(dateFormat.format(review.getTime()));
        //check if the logged user is already a liker of this review
        if(review.getLikers().contains(session.getLoggedUser().getUsername()))
            likeButton.setText("UNLIKE");
        else
            likeButton.setText("LIKE");
        //if the logged user has written the comment, he/she can't like it
        if(review.getReviewer().equals(session.getLoggedUser().getUsername()))
            likeButton.setDisable(true);
    }

    /*private void showUserPage(MouseEvent mouseEvent){
        User u = mongoDBDriver.getUserInfo(review.getReviewer());
        UserPageController userPageController = (UserPageController) Utils.changeScene("/userPage.fxml", mouseEvent);
        userPageController.setUser(u);
    }*/

    private void showBookPage(MouseEvent mouseEvent){
        Book b = mongoDBDriver.getBookInformation(review.getReviewedBookIsbn());
        BookPageController bookPageController = (BookPageController) Utils.changeScene("/bookPage.fxml", mouseEvent);
        bookPageController.setBook(b);
    }

    private void showLikers(MouseEvent mouseEvent){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        StringBuilder toPrint = new StringBuilder();
        List<String> likers = review.getLikers();
        for(String user: likers){
            toPrint.append(user);
            toPrint.append("\n");
        }
        alert.setContentText(toPrint.toString());
        alert.setTitle("Review's Likers");
        alert.show();
    }

}
