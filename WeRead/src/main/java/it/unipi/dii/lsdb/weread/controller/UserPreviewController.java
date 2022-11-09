package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.model.User;
import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.persistence.Neo4jDriver;
import it.unipi.dii.lsdb.weread.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class UserPreviewController {

    @FXML private Label usernameLabel;
    @FXML private Label nameLabel;
    @FXML private Label surnameLabel;
    @FXML private Label emailLabel;
    @FXML private Pane userPane;

    private MongoDBDriver mongoDBDriver;
    private User user; //user who is showed

    public void initialize(){
        mongoDBDriver = MongoDBDriver.getInstance();
        userPane.setOnMouseClicked(mouseEvent -> showUserPage(mouseEvent));
    }

    private void showUserPage(MouseEvent mouseEvent){
        UserPageController userPageController = (UserPageController) Utils.changeScene("/userPage.fxml", mouseEvent);
        userPageController.setUser(user);
    }

    public void setUser(User u){
        user = u;
        nameLabel.setText(user.getName());
        surnameLabel.setText(user.getSurname());
        usernameLabel.setText(user.getUsername());
        //emailLabel.setText(user.getEmail());
    }
}
