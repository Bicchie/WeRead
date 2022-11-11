package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.model.Book;
import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.persistence.Neo4jDriver;
import it.unipi.dii.lsdb.weread.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;

public class NewBookPageController {
    @FXML private Button insightsPageButton;
    @FXML private TextField isbnField;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField languageField;
    @FXML private TextField categoryField;
    @FXML private TextField publisherField;
    @FXML private TextField pagesField;
    @FXML private TextField yearField;
    @FXML private TextField imageField;
    @FXML private TextArea descriptionField;
    @FXML private Button addButton;

    private MongoDBDriver mongoDBDriver;
    private Neo4jDriver neo4jDriver;

    public void initialize(){
        mongoDBDriver = MongoDBDriver.getInstance();
        neo4jDriver = Neo4jDriver.getInstance();
        addButton.setOnMouseClicked(mouseEvent -> addBook(mouseEvent));
        insightsPageButton.setOnMouseClicked(mouseEvent -> showInsightsPage(mouseEvent));
    }

    private void showInsightsPage(MouseEvent mouseEvent){
        Utils.changeScene("/insightsPage.fxml", mouseEvent);
    }

    private void addBook(MouseEvent mouseEvent){
        if(isbnField.getText().equals("")){
            Utils.showErrorAlert("You must write an ISBN");
            return;
        }
        if(titleField.getText().equals("")){
            Utils.showErrorAlert("You must write a title");
            return;
        }
        if(authorField.getText().equals("")){
            Utils.showErrorAlert("You must write an author");
            return;
        }
        if(languageField.getText().equals("")){
            Utils.showErrorAlert("You must write a language");
            return;
        }
        if(categoryField.getText().equals("")){
            Utils.showErrorAlert("You must write a category");
            return;
        }
        if(publisherField.getText().equals("")){
            Utils.showErrorAlert("You must write a publisher");
            return;
        }
        int numPages = 0;
        if(pagesField.getText().equals("")){
            Utils.showErrorAlert("You must write the number of pages");
            return;
        }
        else {
            try{
                numPages = Integer.parseInt(pagesField.getText());
            } catch(NumberFormatException e){
                e.printStackTrace();
                Utils.showErrorAlert("You must write a number of pages");
            }
        }
        int pubYear = 0;
        if(yearField.getText().equals("")){
            Utils.showErrorAlert("You must write the publication year");
            return;
        }
        else {
            try{
                pubYear = Integer.parseInt(yearField.getText());
            } catch(NumberFormatException e){
                e.printStackTrace();
                Utils.showErrorAlert("You must write the publication year");
            }
        }
        if(imageField.getText().equals("")){
            Utils.showErrorAlert("You must write an image URL for the book cover");
            return;
        }
        if(descriptionField.getText().equals("")){
            Utils.showErrorAlert("You must write a description");
            return;
        }

        //I can do the queries
        Utils.addNewBook(new Book(isbnField.getText(),
                titleField.getText(),
                languageField.getText(),
                categoryField.getText(),
                publisherField.getText(),
                descriptionField.getText(),
                authorField.getText(),
                numPages,
                pubYear,
                imageField.getText(),
                new ArrayList<>()));
    }
}
