package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.model.Session;
import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.persistence.Neo4jDriver;
import it.unipi.dii.lsdb.weread.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.List;


public class HomePageController {
    @FXML private ComboBox whatSuggestComboBox;
    @FXML private VBox suggestionsVbox;
    @FXML private Button nextButton;
    @FXML private Button previousButton;
    @FXML private Label pagesLabel;

    private List suggestList; //list of the suggestion results
    private int currentPage; //index of the current page of the results list
    private final int numResultsToShow = 3; //maximum number of results to be showed in each page
    private int numPages; // num pages of the current suggestion list
    private final String[] suggestionOptions = {"Friends", "Books", "Reading Lists"};
    private Neo4jDriver neo4jDriver;
    private Session session;
    private String whatSuggest;

    public void initialize(){
        neo4jDriver = Neo4jDriver.getInstance();
        session = Session.getInstance();

        whatSuggestComboBox.getItems().addAll(suggestionOptions);
        whatSuggestComboBox.setOnAction(event -> changeSuggestion());
        pagesLabel.setVisible(false);
        previousButton.setVisible(false);
        nextButton.setVisible(false);

        // DA METTERE NON QUI MA QUANDO E' SELEZIONATO LA COMBOBOX
        previousButton.setOnMouseClicked(mouseEvent -> previousPage(mouseEvent));
        nextButton.setOnMouseClicked(mouseEvent -> nextPage(mouseEvent));
    }

    private void changeSuggestion(){
        currentPage = 1;
        makeBoxEmpty();
        whatSuggest = whatSuggestComboBox.getValue().toString();
        switch(whatSuggest){
            case "Friends":
                suggestList = neo4jDriver.suggestUsersByCommonInterest(session.getLoggedUser().getUsername(),12);
                //Utils.addRBookPreviewsBig(suggestionsVbox, resultList);
                System.out.println("AVREI MOSTRATO FRIENDS");
                break;
            case "Books":
                suggestList = neo4jDriver.suggestBooks(session.getLoggedUser().getUsername(),12);
                //Utils.addRBookPreviewsBig(suggestionsVbox, suggestList);
                break;
            case "Reading Lists":
                suggestList = neo4jDriver.suggestReadingLists(session.getLoggedUser().getUsername(),12);
                //Utils.addRBookPreviewsBig(suggestionsVbox, resultList);
                System.out.println("AVREI MOSTRATO READING LISTS");
                break;
        }
        int numElement = suggestList.size();
        numPages = (int) Math.ceil((double)numElement / numResultsToShow);
        System.out.println("num element " + numElement);
        System.out.println("num pages " + numPages);
        showSuggestions();

    }
    private void showSuggestions(){
        int start = currentPage - 1;
        int end = start + numResultsToShow;
        System.out.println(suggestList.subList(start,end));
        Utils.addRBookPreviewsBig(suggestionsVbox, suggestList.subList(start,end));
        setPageBox();
    }


    private void setPageBox(){
        if(numPages <= 1){
            nextButton.setVisible(false);
            previousButton.setVisible(false);
        }
        else {
            if (currentPage < numPages)
                nextButton.setVisible(true);
            else
                nextButton.setVisible(false);
            if (currentPage > 1)
                previousButton.setVisible(true);
            else
                previousButton.setVisible(false);
        }
        pagesLabel.setText(currentPage + "/" + numPages);
        pagesLabel.setVisible(true);
    }

    private void previousPage(MouseEvent mouseEvent){
        makeBoxEmpty();
        currentPage--;
        setPageBox();
        showSuggestions();
    }

    private void nextPage(MouseEvent mouseEvent){
        makeBoxEmpty();
        currentPage++;
        setPageBox();
        showSuggestions();
    }


    private void makeBoxEmpty(){
        suggestionsVbox.getChildren().clear();
    }

}
