package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.model.User;
import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class LikerController {
    @FXML private Label liker;

    MongoDBDriver mongoDBDriver;
    String username;

    public void initialize(){
        mongoDBDriver = MongoDBDriver.getInstance();
        liker.setOnMouseClicked(mouseEvent -> showUserPage(mouseEvent));
    }

    private void showUserPage(MouseEvent mouseEvent){
        User u = mongoDBDriver.getUserInfo(username);
        UserPageController userPageController = (UserPageController) Utils.changeScene("/userPage.fxml", mouseEvent);
        //userPageController.setUser(u);
    }

    public void setLiker(String l){
        username = l;
        liker.setText(username);
    }
}
