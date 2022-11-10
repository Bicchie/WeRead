package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.model.Book;
import it.unipi.dii.lsdb.weread.model.ReadingList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

public class ReadingListSnapshotController {
    @FXML private Label titleRL;
    @FXML private Label numberLikesRL;

    private ReadingList readinglist;

    public void setReadinglist(ReadingList rl){
        this.readinglist = rl;
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
