package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.persistence.Neo4jDriver;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.model.User;
import it.unipi.dii.lsdb.weread.model.Session;
import it.unipi.dii.lsdb.weread.model.Book;
import it.unipi.dii.lsdb.weread.model.ReadingList;
import it.unipi.dii.lsdb.weread.model.Review;
import it.unipi.dii.lsdb.weread.utils.Utils;
import it.unipi.dii.lsdb.weread.model.Book;


import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Controller for the User Page
 */
public class UserPageController {
    @FXML private Label username;
    @FXML private Label following;
    @FXML private Label follower;
    @FXML private HBox favoriteBooksHbox;
    @FXML private HBox reviewsHbox;
    @FXML private HBox readingListsHbox;
    @FXML private ImageView bookSearchIcon;
    @FXML private ImageView homeIcon;
    @FXML private ImageView userIcon;


    private MongoDBDriver mongoDBDriver;
    private Neo4jDriver neo4jDriver;
    private User loggedUser;
    /**
     * Method called when the controller is initialized
     */
    public void initialize()
    {
        mongoDBDriver = MongoDBDriver.getInstance();
        neo4jDriver = Neo4jDriver.getInstance();
        loggedUser = Session.getInstance().getLoggedUser();
        username.setText(loggedUser.getUsername());
        following.setText("following: " + neo4jDriver.numberFollowed(loggedUser.getUsername()));
        follower.setText("follower: " + neo4jDriver.numberFollowers(loggedUser.getUsername()));

        bookSearchIcon.setOnMouseClicked(mouseEvent -> clickOnSearchIcon(mouseEvent));
        homeIcon.setOnMouseClicked(mouseEvent -> clickOnHomeIcon(mouseEvent));
        //userIcon.setOnMouseClicked(mouseEvent -> clickOnUserIcon(mouseEvent));

        List<Book> favoriteBooksList = mongoDBDriver.getFavoriteOfUser(loggedUser.getUsername());
        Utils.showFavoriteBooks(favoriteBooksHbox,favoriteBooksList);

    }

    private void clickOnSearchIcon(MouseEvent mouseEvent){
        //Utils.changeScene("/homepage.fxml", mouseEvent);
    }

    private void clickOnHomeIcon(MouseEvent mouseEvent){
        //Utils.changeScene("/homepage.fxml", mouseEvent);
    }

    private void clickOnUserIcon(MouseEvent mouseEvent){
        //Utils.changeScene("/userPage.fxml", mouseEvent);
    }

}
