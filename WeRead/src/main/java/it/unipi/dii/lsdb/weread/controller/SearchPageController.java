package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.model.Session;
import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.List;

public class SearchPageController {
    @FXML private TextField searchText;
    @FXML private RadioButton usersRadio;
    @FXML private RadioButton booksRadio;
    @FXML private Button searchButton;
    @FXML private VBox resultBox;
    @FXML private ComboBox searchByBox;
    @FXML private Label searchByLabel;
    @FXML private Button nextButton;
    @FXML private Button previousButton;
    @FXML private Label pageLabel;
    @FXML private ImageView homeIcon;
    @FXML private ImageView bookSearchIcon;
    @FXML private ImageView userIcon;

    private MongoDBDriver mongoDBDriver;
    private List resultList; //list of the search results
    private final String[] searchByOptions = {"title", "author", "category", "publisher", "publication year"};
    private long currentPage; //index of the current page of the results list
    private long numPages; //total number of pages requested for showing the last search results
    private int numResultsToShow = 3; //maximum number of results to be showed in each page
    private String searchBy; //by what the last search has been requested
    private String searchKey; //string inserted by the user to make the research

    public void initialize(){
        mongoDBDriver = MongoDBDriver.getInstance();
        searchButton.setOnMouseClicked(mouseEvent -> search(mouseEvent));

        //only one radio button at a time can be selected
        usersRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {
            usersRadioListener(newValue);
        });
        booksRadio.selectedProperty().addListener((observable, oldValue, newValue) -> {
            booksRadioListener(newValue);
        });

        currentPage = 1;
        numPages = 1;
        searchBy = "";
        setPageBox();

        previousButton.setOnMouseClicked(mouseEvent -> previousPage(mouseEvent));
        nextButton.setOnMouseClicked(mouseEvent -> nextPage(mouseEvent));

        bookSearchIcon.setOnMouseClicked(mouseEvent -> clickOnSearchIcon(mouseEvent));
        userIcon.setOnMouseClicked(mouseEvent -> clickOnUserIcon(mouseEvent));
        homeIcon.setOnMouseClicked(mouseEvent -> clickOnHomeIcon(mouseEvent));
    }

    private void clickOnHomeIcon(MouseEvent mouseEvent){
        Utils.changeScene("/homePage.fxml", mouseEvent);
    }

    private void clickOnSearchIcon(MouseEvent mouseEvent){
        Utils.changeScene("/searchPage.fxml", mouseEvent);
    }

    private void clickOnUserIcon(MouseEvent mouseEvent){
        UserPageController userPageController = (UserPageController) Utils.changeScene("/userPage.fxml", mouseEvent);
        userPageController.setUser(Session.getInstance().getLoggedUser());
    }

    private void usersRadioListener(boolean selected){
        if(selected){
            //users has been selected
            if(booksRadio.isSelected())
                booksRadio.setSelected(false);
            //have to clear the content of the box because the users can be searched only by usernames
            searchByBox.getItems().clear();
            searchByBox.setOpacity(0);
            searchByLabel.setOpacity(0);
        }
    }

    private void booksRadioListener(boolean selected){
        if(selected){
            //books has been selected
            if(usersRadio.isSelected())
                usersRadio.setSelected(false);
            searchByBox.getItems().addAll(searchByOptions);
            searchByBox.setOpacity(1);
            searchByLabel.setOpacity(1);
        }
    }

    private void search(MouseEvent mouseEvent){
        searchKey = searchText.getText();
        if(searchKey.equals("")){
            Utils.showErrorAlert("You have to write something in the text area!");
            return;
        }
        if(!usersRadio.isSelected() && !booksRadio.isSelected()){
            Utils.showErrorAlert("You must select one thing between users and books");
            return;
        }
        //must empty the previous results
        makeBoxEmpty();
        numPages = 1;
        currentPage = 1;
        if(booksRadio.isSelected()){
            searchBy = (String) searchByBox.getValue();
            if(searchBy == null){
                Utils.showErrorAlert("Please select something to search by");
                return;
            }
            numResultsToShow = 3; //number of books to show for each page
            switch(searchBy){
                case "title":
                    System.out.println();
                    numPages = (long) Math.ceil(mongoDBDriver.countBookByTitle(searchKey)/ numResultsToShow);
                    resultList = mongoDBDriver.searchBookByTitle(searchKey, (int) (currentPage-1) * numResultsToShow, numResultsToShow);
                    break;
                case "author":
                    numPages = (long) Math.ceil(mongoDBDriver.countBookByAuthor(searchKey)/ numResultsToShow);
                    resultList = mongoDBDriver.searchBookByAuthor(searchKey, (int) (currentPage-1) * numResultsToShow, numResultsToShow);
                    break;
                case "category":
                    numPages = (long) Math.ceil(mongoDBDriver.countBookByCategory(searchKey)/ numResultsToShow);
                    resultList = mongoDBDriver.searchBookByCategory(searchKey, (int) (currentPage-1) * numResultsToShow, numResultsToShow);
                    break;
                case "publisher":
                    numPages = (long) Math.ceil(mongoDBDriver.countBookByPublisher(searchKey)/ numResultsToShow);
                    resultList = mongoDBDriver.searchBookByPublisher(searchKey, (int) (currentPage-1) * numResultsToShow, numResultsToShow);
                    break;
                case "publication year":
                    numPages = (long) Math.ceil(mongoDBDriver.countBookByYear(searchKey)/ numResultsToShow);
                    resultList = mongoDBDriver.searchBookByYear(searchKey, (int) (currentPage-1) * numResultsToShow, numResultsToShow);
                    break;
            }
            setPageBox();
            Utils.addBookPreviewsBig(resultBox, resultList);
        }
        if(usersRadio.isSelected()){
            numResultsToShow = 12;
            numPages = (long) Math.ceil(mongoDBDriver.countUsersByString(searchKey)/ numResultsToShow);
            resultList = mongoDBDriver.searchUsersByString(searchKey, (int) (currentPage-1) * numResultsToShow, numResultsToShow);
            setPageBox();
            Utils.addUserPreviews(resultBox, resultList);
        }
    }

    private void setPageBox(){
        if(numPages <= 1){
            nextButton.setDisable(true);
            previousButton.setDisable(true);
        }
        else {
            if (currentPage < numPages)
                nextButton.setDisable(false);
            else
                nextButton.setDisable(true);
            if (currentPage > 1)
                previousButton.setDisable(false);
            else
                previousButton.setDisable(true);
        }
        pageLabel.setText(currentPage + "/" + numPages);
    }

    private void previousPage(MouseEvent mouseEvent){
        makeBoxEmpty();
        currentPage--;
        setPageBox();
        searchNewResults();
    }

    private void nextPage(MouseEvent mouseEvent){
        makeBoxEmpty();
        currentPage++;
        setPageBox();
        searchNewResults();
    }

    private void searchNewResults(){
        switch(searchBy){
            case "title":
                resultList = mongoDBDriver.searchBookByTitle(searchKey, (int) (currentPage-1) * numResultsToShow, numResultsToShow);
                break;
            case "author":
                resultList = mongoDBDriver.searchBookByAuthor(searchKey, (int) (currentPage-1) * numResultsToShow, numResultsToShow);
                break;
            case "category":
                resultList = mongoDBDriver.searchBookByCategory(searchKey, (int) (currentPage-1) * numResultsToShow, numResultsToShow);
                break;
            case "publisher":
                resultList = mongoDBDriver.searchBookByPublisher(searchKey, (int) (currentPage-1) * numResultsToShow, numResultsToShow);
                break;
            case "publication year":
                resultList = mongoDBDriver.searchBookByYear(searchKey, (int) (currentPage-1) * numResultsToShow, numResultsToShow);
                break;
        }
        Utils.addBookPreviewsBig(resultBox, resultList);
    }

    private void makeBoxEmpty(){
        resultBox.getChildren().clear();
    }
}
