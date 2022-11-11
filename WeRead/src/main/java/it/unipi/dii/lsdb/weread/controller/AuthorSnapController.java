package it.unipi.dii.lsdb.weread.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AuthorSnapController {
    @FXML private Label authorName;
    @FXML private Label rating;

    public void setAuthor(String author, double r){
        authorName.setText(author);
        rating.setText(String.format("%.2f", r));
    }
}
