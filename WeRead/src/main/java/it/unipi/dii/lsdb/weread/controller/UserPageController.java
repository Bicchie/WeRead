package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.persistence.Neo4jDriver;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.model.User;
import it.unipi.dii.lsdb.weread.model.Session;
import it.unipi.dii.lsdb.weread.utils.Utils;


import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;


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
    @FXML private ImageView followIcon;
    @FXML private ImageView unfollowIcon;
    @FXML private Button changePasswordButton;
    @FXML private TextField newPasswordText;


    private MongoDBDriver mongoDBDriver;
    private Neo4jDriver neo4jDriver;
    private User user; // user showed in this page
    private Session session;
    /**
     * Method called when the controller is initialized
     */
    public void initialize()
    {
        mongoDBDriver = MongoDBDriver.getInstance();
        neo4jDriver = Neo4jDriver.getInstance();
        session = Session.getInstance();

        bookSearchIcon.setOnMouseClicked(mouseEvent -> clickOnSearchIcon(mouseEvent));
        homeIcon.setOnMouseClicked(mouseEvent -> clickOnHomeIcon(mouseEvent));
        userIcon.setOnMouseClicked(mouseEvent -> clickOnUserIcon(mouseEvent));

    }

    /**
     * Function that prepare the page in these 3 cases:
     * - the loggedUser wants to see his/her profile
     * - the loggedUser wants to see another user profile that doesn't follow
     * - the loggedUser wants to see another user profile that follows
     * @param u    parameter containing the object class User with all his/her information
     */
    public void setUser(User u){
        this.user = u;
        // show the information about the user
        username.setText(user.getUsername());
        following.setText("following: " + neo4jDriver.numberFollowed(user.getUsername()));
        follower.setText("follower: " + neo4jDriver.numberFollowers(user.getUsername()));

        Utils.showFavoriteBooks(favoriteBooksHbox,user.getFavourite());
        Utils.showReadingLists(readingListsHbox,user.getReadingLists());
        Utils.showReviews(reviewsHbox,user.getReviewList());

        // if is the profile of the loggedUser to be shown
        if (u.getUsername().equals(session.getLoggedUser().getUsername())){
            followIcon.setVisible(false);
            unfollowIcon.setVisible(false);
            changePasswordButton.setOnMouseClicked(mouseEvent -> clickOnChangePassword(mouseEvent));;
        } else { // is another user profile to be shown
            changePasswordButton.setVisible(false);
            newPasswordText.setVisible(false);
            // if the loggedUser follows the user of the profile
            if(neo4jDriver.checkUserFollowsUser(session.getLoggedUser().getUsername(),user.getUsername())){
                unfollowIcon.setOnMouseClicked(mouseEvent -> clickOnUnfollow(mouseEvent));
                unfollowIcon.setVisible(true);
                followIcon.setVisible(false);
            } else { // the loggedUser not follows the user of the profile
                unfollowIcon.setVisible(false);
                followIcon.setVisible(true);
                followIcon.setOnMouseClicked(mouseEvent -> clickOnFollow(mouseEvent));
            }


        }
    }

    private void clickOnUserIcon(MouseEvent mouseEvent){
        UserPageController userPageController = (UserPageController) Utils.changeScene("/userPage.fxml", mouseEvent);
        userPageController.setUser(session.getLoggedUser());
    }

    private void clickOnSearchIcon(MouseEvent mouseEvent){
        Utils.changeScene("/searchPage.fxml", mouseEvent);
    }

    private void clickOnHomeIcon(MouseEvent mouseEvent){
        Utils.changeScene("/homePage.fxml", mouseEvent);
    }

    private void clickOnUnfollow(MouseEvent mouseEvent){
        if(neo4jDriver.unfollow(session.getLoggedUser().getUsername(),user.getUsername())){
            Utils.showInformationAlert("User Unfollowed!");
            int numFollower = Integer.parseInt(follower.getText());
            numFollower = numFollower - 1;
            follower.setText(Integer.toString(numFollower));
            unfollowIcon.setVisible(false);
            followIcon.setVisible(true);
            unfollowIcon.removeEventHandler(MouseEvent.MOUSE_CLICKED,this::clickOnUnfollow);
            followIcon.setOnMouseClicked(event -> clickOnFollow(event));
        } else {
            Utils.showErrorAlert("User not unfollowed, please retry!");
        }
    }

    private void clickOnFollow(MouseEvent mouseEvent){
        if(neo4jDriver.follow(session.getLoggedUser().getUsername(),user.getUsername())){
            Utils.showInformationAlert("User Followed!");
            int numFollower = Integer.parseInt(follower.getText());
            numFollower = numFollower + 1;
            follower.setText(Integer.toString(numFollower));
            followIcon.setVisible(false);
            unfollowIcon.setVisible(true);
            followIcon.removeEventHandler(MouseEvent.MOUSE_CLICKED,this::clickOnFollow);
            unfollowIcon.setOnMouseClicked(event -> clickOnUnfollow(event));
        } else {
            Utils.showErrorAlert("User not followed, please retry!");
        }
    }

    private void clickOnChangePassword(MouseEvent mouseEvent){
        if(newPasswordText.getText() != ""){
           if(mongoDBDriver.changeUserPassword(user.getUsername(),newPasswordText.getText())){
               Utils.showInformationAlert("PasswordChanged");
           } else {
               Utils.showErrorAlert("Error in changing password, please retry!");
           }
        } else {
            Utils.showErrorAlert("Please insert the new password");
        }
    }



}
