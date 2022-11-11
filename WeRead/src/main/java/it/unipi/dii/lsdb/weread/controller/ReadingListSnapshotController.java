package it.unipi.dii.lsdb.weread.controller;


import it.unipi.dii.lsdb.weread.model.ReadingList;
import it.unipi.dii.lsdb.weread.model.Session;
import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class ReadingListSnapshotController {
    @FXML private AnchorPane readingListPane;
    @FXML private Label titleRL;
    @FXML private Label numberLikesRL;
    private String owner;
    private MongoDBDriver mongoDBDriver;
    private ReadingList readinglist;

    public void initialize(){
        mongoDBDriver = MongoDBDriver.getInstance();
        readingListPane.setOnMouseClicked(mouseEvent -> showReadingListPage(mouseEvent));
    }

    private void showReadingListPage(MouseEvent mouseEvent){
        if(Session.getInstance().getLoggedUser().getIsAdministrator())
            return;
        // means that you are arriving from suggestion of reading list, so you can go to the user that creates the rl
        if(readinglist.getBooks().size() == 0){
            String name = readinglist.getName();
            String parts[] = name.split(":");
            owner = parts[0];
            String rlName = parts[1];
            readinglist = mongoDBDriver.getReadingList(owner,rlName);
            ReadingListPageController readingListPageController = (ReadingListPageController) Utils.changeScene("/readingListPage.fxml", mouseEvent);
            readingListPageController.setReadingList(readinglist, owner);
        } else { // you are arriving from the userpage so you can go directly to the reading list page
            ReadingListPageController readingListPageController = (ReadingListPageController) Utils.changeScene("/readingListPage.fxml", mouseEvent);
            readingListPageController.setReadingList(readinglist, owner);
        }
    }

    public void setReadinglist(ReadingList rl, String username){
        this.readinglist = rl;
        this.owner = username;
        String name = rl.getName();
        if(name.contains(":")){
            String[] parts = name.split(":");
            titleRL.setText(parts[1] + " by \n " + parts[0]);
        } else {
            titleRL.setText(rl.getName());
        }
        if(rl.getNumLikes() != 0){
            String numLikes = Integer.toString(rl.getNumLikes());
            numberLikesRL.setText("Number of Likes: " + numLikes);
        }
    }

}
