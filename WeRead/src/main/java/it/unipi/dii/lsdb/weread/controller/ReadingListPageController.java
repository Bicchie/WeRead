package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.model.ReadingList;
import it.unipi.dii.lsdb.weread.model.Session;
import it.unipi.dii.lsdb.weread.model.User;
import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.persistence.Neo4jDriver;
import it.unipi.dii.lsdb.weread.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.List;

public class ReadingListPageController {
    @FXML private VBox booksVBox;
    @FXML private Button likeButton;
    @FXML private Label numLikesLabel;
    @FXML private Label usernameLabel;
    @FXML private Label readingListTitle;
    @FXML private ImageView bookSearchIcon;
    @FXML private ImageView userIcon;
    @FXML private ImageView homeIcon;

    private ReadingList readingList; // the readinglist showed in this page
    private String owner;  // the user that owns the reading list
    private Neo4jDriver neo4jDriver;
    private MongoDBDriver mongoDBDriver;
    private Session session;

    public void initialize(){
        neo4jDriver = Neo4jDriver.getInstance();
        mongoDBDriver = MongoDBDriver.getInstance();
        session = Session.getInstance();

        bookSearchIcon.setOnMouseClicked(mouseEvent -> clickOnSearchIcon(mouseEvent));
        userIcon.setOnMouseClicked(mouseEvent -> clickOnUserIcon(mouseEvent));
        homeIcon.setOnMouseClicked(mouseEvent -> clickOnHomeIcon(mouseEvent));
    }

    public void setReadingList(ReadingList rl,String username){
        this.readingList = rl;
        this.owner = username;
        readingListTitle.setText(readingList.getName());
        usernameLabel.setText(username);
        usernameLabel.setOnMouseClicked(mouseEvent -> clickOnCreator(mouseEvent));
        numLikesLabel.setText(Integer.toString(readingList.getNumLikes()));
        Utils.addBookPreviewsBig(booksVBox,readingList.getBooks());
        // if you are the owner, you can delete the reading list
        if(owner.equals(session.getLoggedUser().getUsername())){
            likeButton.setText("Delete");
            likeButton.setOnMouseClicked(mouseEvent -> clickOnDeleteButton(mouseEvent));
        } else {
            // if the user already liked the reading list
            if(neo4jDriver.checkUserLikesReadingList(session.getLoggedUser().getUsername(),readingList.getName())){
                likeButton.setOnMouseClicked(mouseEvent -> clickOnUnlikeButton(mouseEvent));
                likeButton.setText("Unlike");
            } else {
                likeButton.setOnMouseClicked(mouseEvent -> clickOnLikeButton(mouseEvent));
                likeButton.setText("Like");
            }
        }
    }

    private void clickOnDeleteButton(MouseEvent mouseEvent){
        if(mongoDBDriver.removeReadingList(readingList,owner)){
            if(!neo4jDriver.deleteReadingList(readingList.getName())){
                mongoDBDriver.addNewReadingList(readingList,owner);
                Utils.showErrorAlert("Error in deleting reading list from neo4j!");
            }
        } else {
            Utils.showErrorAlert("Error in deleting reading list from mongoDB!");
        }
        User u = mongoDBDriver.getUserInfo(session.getLoggedUser().getUsername());
        UserPageController userPageController = (UserPageController) Utils.changeScene("/userPage.fxml", mouseEvent);
        userPageController.setUser(u);
    }

    private void clickOnCreator(MouseEvent mouseEvent){
        User u = mongoDBDriver.getUserInfo(usernameLabel.getText());
        UserPageController userPageController = (UserPageController) Utils.changeScene("/userPage.fxml", mouseEvent);
        userPageController.setUser(u);
    }

    private  void clickOnUnlikeButton(MouseEvent mouseEvent){
        // cross-db consistency
        if(mongoDBDriver.removeLikeReadingList(owner,readingList.getName())){
            String rlname = owner + ":" + readingList.getName();
            if(!neo4jDriver.unlikeReadingList(session.getLoggedUser().getUsername(),rlname)){
                mongoDBDriver.addLikeReadingList(owner,readingList.getName());
                Utils.showErrorAlert("Error in liking reading list from neo4j!");
            }
        } else {
            Utils.showErrorAlert("Error in liking reading list from mongoDB!");
        }
        likeButton.setText("Like");
        int numLikes = Integer.parseInt(numLikesLabel.getText());
        numLikes = numLikes - 1;
        numLikesLabel.setText(Integer.toString(numLikes));
        likeButton.setOnMouseClicked(event -> clickOnLikeButton(mouseEvent));

    }

    private void clickOnLikeButton(MouseEvent mouseEvent){
        // cross-db consistency
        if(mongoDBDriver.addLikeReadingList(owner,readingList.getName())){
            String rlname = owner + ":" + readingList.getName();
            if(!neo4jDriver.likesReadingList(session.getLoggedUser().getUsername(),rlname)){
                mongoDBDriver.removeLikeReadingList(owner,readingList.getName());
                Utils.showErrorAlert("Error in liking reading list from neo4j!");
            }
        } else {
            Utils.showErrorAlert("Error in liking reading list from mongodDB!");
        }
        likeButton.setText("Unlike");
        int numLikes = Integer.parseInt(numLikesLabel.getText());
        numLikes = numLikes + 1;
        numLikesLabel.setText(Integer.toString(numLikes));
        likeButton.setOnMouseClicked(event -> clickOnUnlikeButton(mouseEvent));
    }

    private void clickOnSearchIcon(MouseEvent mouseEvent){
        Utils.changeScene("/searchPage.fxml", mouseEvent);
    }

    private void clickOnUserIcon(MouseEvent mouseEvent){
        UserPageController userPageController = (UserPageController) Utils.changeScene("/userPage.fxml", mouseEvent);
        userPageController.setUser(session.getLoggedUser());
    }

    private void clickOnHomeIcon(MouseEvent mouseEvent){
        Utils.changeScene("/homePage.fxml", mouseEvent);
    }
}
