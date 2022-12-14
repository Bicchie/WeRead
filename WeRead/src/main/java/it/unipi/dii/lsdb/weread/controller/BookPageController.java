package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.model.Book;
import it.unipi.dii.lsdb.weread.model.ReadingList;
import it.unipi.dii.lsdb.weread.model.Review;
import it.unipi.dii.lsdb.weread.model.Session;
import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.persistence.Neo4jDriver;
import it.unipi.dii.lsdb.weread.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.text.DecimalFormat;
import java.util.List;


public class BookPageController {
    @FXML private Text bookDescription;
    @FXML private ComboBox rlBox;
    @FXML private VBox reviewsVbox;
    @FXML private Button reviewButton;
    @FXML private ComboBox ratingBox;
    @FXML private TextArea reviewText;
    @FXML private Label reviewLabel;
    @FXML private Button favoriteButton;
    @FXML private Button rlButton;
    @FXML private Label numFavorite;
    @FXML private Label publicationYear;
    @FXML private Label numPages;
    @FXML private Label bookPublisher;
    @FXML private Label bookLanguage;
    @FXML private Text bookTitle;
    @FXML private ImageView bookImage;
    @FXML private Label bookAuthor;
    @FXML private Label bookCategory;
    @FXML private Label rateLabel;
    @FXML private ImageView homeIcon;
    @FXML private ImageView bookSearchIcon;
    @FXML private ImageView userIcon;

    private Book book; //book showed in this page
    private MongoDBDriver mongoDBDriver;
    private Neo4jDriver neo4jDriver;
    private Session session;

    public void initialize(){
        mongoDBDriver = MongoDBDriver.getInstance();
        neo4jDriver = Neo4jDriver.getInstance();
        session = Session.getInstance();
        //setta vari listener
        rlButton.setOnMouseClicked(mouseEvent -> addToReadingList(mouseEvent));
        //rlBox.setOnHidden(event -> checkRlButton(event));
        rlBox.valueProperty().addListener((observable, oldValue, newValue) -> checkRlButton());
        reviewButton.setOnMouseClicked(mouseEvent -> newReview(mouseEvent));

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
        userPageController.setUser(session.getLoggedUser());
    }

    public void setBook(Book b){
        this.book = b;
        bookTitle.setText(book.getTitle());
        bookImage.setImage(new Image(book.getImageURL()));
        bookAuthor.setText(book.getAuthor());
        bookCategory.setText(book.getCategory());
        bookLanguage.setText(book.getLanguage());
        bookPublisher.setText(book.getPublisher());
        numPages.setText(book.getNumPages() + " pages");
        if(book.getPublicationYear() != 0)
            publicationYear.setText(String.valueOf(book.getPublicationYear()));
        else
            publicationYear.setText("Not available");
        numFavorite.setText(neo4jDriver.numberFavoritesOfBook(book.getIsbn()) + " users add it to their favorite books");
        double avg = mongoDBDriver.getAvgRating(book.getIsbn());
        if(avg == -1){
            rateLabel.setVisible(false);
        } else {
            rateLabel.setText(String.format("%.2f",avg));
        }


        //List<Book> favorites = mongoDBDriver.getFavoriteOfUser(session.getLoggedUser().getUsername());
        List<Book> favorites = session.getLoggedUser().getFavourite();
        boolean isFavorite = false; //true if the showed book is already in the favorite books of the logged user
        if(favorites == null)
            System.out.println("NULL");
        for(Book favBook: favorites){
            if(favBook.getIsbn().equals(book.getIsbn())){
                isFavorite = true;
                break;
            }
        }
        setFavoriteButton(isFavorite);

        List<ReadingList> readingLists = session.getLoggedUser().getReadingLists();
        for(ReadingList rl: readingLists){
            rlBox.getItems().add(rl.getName());
        }

        bookDescription.setText(book.getDescription());
        List<Review> reviewList = session.getLoggedUser().getReviewList();
        //check if the user has or has not reviewed this book yet
        String id = Utils.buildReviewId(session.getLoggedUser().getUsername(), book.getIsbn());
        boolean reviewed = false;
        for(Review r: reviewList){
            if(r.getReviewId().equals(id)) {
                reviewed = true;
                break;
            }
        }
        setReviewZone(reviewed);

        //aggiungi le review
        Utils.addReviewsBig(reviewsVbox, book.getReviews(), true);
    }

    private void checkRlButton() {
        String selectedReadingList = (String) rlBox.getValue();
        boolean contained = false; //true if the selected reading list already contains the showed book
        for(ReadingList rl: session.getLoggedUser().getReadingLists()){
            if(rl.getName().equals(selectedReadingList)) {
                List<Book> bookList = rl.getBooks();
                for (Book b : bookList) {
                    if (b.getIsbn().equals(book.getIsbn())) {
                        contained = true;
                        break;
                    }
                }
                if (contained)
                    break;
            }
        }
        setRlButton(contained);
    }

    private void setFavoriteButton(boolean isFavorite){
        if(isFavorite) {
            //if the user has already add this book to its favorites
            favoriteButton.setText("Remove from favorite books list");
            favoriteButton.setOnMouseClicked(mouseEvent -> remFavorite(mouseEvent));
        }
        else{
            //if the user has not already add this book to its favorites
            favoriteButton.setText("Add to favorite books list");
            favoriteButton.setOnMouseClicked(mouseEvent -> addFavorite(mouseEvent));
        }
    }

    private void setRlButton(boolean contained){
        if(contained){
            rlButton.setText("REMOVE");
            rlButton.setOnMouseClicked(mouseEvent -> removeFromReadingList(mouseEvent));
        }
        else{
            rlButton.setText("ADD");
            rlButton.setOnMouseClicked(mouseEvent -> addToReadingList(mouseEvent));
        }
    }

    private void addFavorite(MouseEvent mouseEvent) {
        if(!Utils.addBookToFavorite(session.getLoggedUser().getUsername(), book))
            return;
        int nFav = Integer.parseInt(numFavorite.getText().split(" ")[0]);
        nFav++;
        numFavorite.setText(nFav + " users add it to their favorite books");
        session.getLoggedUser().getFavourite().add(book);
        setFavoriteButton(true);
    }

    private void remFavorite(MouseEvent mouseEvent) {
        if(!Utils.removeBookFromFavorite(session.getLoggedUser().getUsername(), book))
            return;
        int nFav = Integer.parseInt(numFavorite.getText().split(" ")[0]);
        nFav--;
        numFavorite.setText(nFav + " users add it to their favorite books");
        session.getLoggedUser().getFavourite().remove(book);
        setFavoriteButton(false);
    }

    private void addToReadingList(MouseEvent mouseEvent) {
        //check if the user is trying to create a new reading list or not
        String selectedReadingList = (String) rlBox.getValue();
        boolean exists = false; //true if such a reading list already exists
        for(ReadingList rl: session.getLoggedUser().getReadingLists()){
            if(rl.getName().equals(selectedReadingList)){
                exists = true;
                break;
            }
        }
        if(!exists){
            ReadingList readingList = new ReadingList(selectedReadingList);
            if(!Utils.createNewReadingList(session.getLoggedUser().getUsername(), readingList))
                return; //if the reading list could not be created, the book will obviousbly be not added to it
            rlBox.getItems().add(selectedReadingList);
            session.getLoggedUser().getReadingLists().add(readingList);
        }
        if(!Utils.addBookToReadingList(session.getLoggedUser().getUsername(), selectedReadingList, book))
            return;
        for(ReadingList rl: session.getLoggedUser().getReadingLists()){
            if(rl.getName().equals(selectedReadingList)){
                rl.getBooks().add(book);
            }
        }
        setRlButton(true);
    }

    private void removeFromReadingList(MouseEvent mouseEvent) {
        String selectedReadingList = (String) rlBox.getValue();
        if(!Utils.removeBookFromReadingList(session.getLoggedUser().getUsername(), selectedReadingList, book))
            return;
        for(ReadingList rl: session.getLoggedUser().getReadingLists()){
            if(rl.getName().equals(selectedReadingList)){
                rl.getBooks().remove(book);
            }
        }
        setRlButton(false);
    }

    private void newReview(MouseEvent mouseEvent){
        String rat = (String) ratingBox.getValue();
        if(rat == null){
            Utils.showErrorAlert("You have to give a rating to complete the review!");
            return;
        }
        int rating = Integer.parseInt(rat);
        String text = reviewText.getText();
        if(text.equals("")){
            Utils.showErrorAlert("You have to write a text to complete the review!");
            return;
        }
        Review newReview = new Review(session.getLoggedUser().getUsername(), book.getIsbn(), text, rating, book.getTitle());
        if(mongoDBDriver.addNewReview(newReview, false)) {
            setReviewZone(true);
            reviewsVbox.getChildren().clear();
            book.getReviews().add(newReview);
            session.getLoggedUser().getReviewList().add(newReview);
            Utils.addReviewsBig(reviewsVbox, book.getReviews(), true);
        }
        else
            Utils.showErrorAlert("Error during the creation of the new review");
    }

    private void setReviewZone(boolean reviewed){
        if(reviewed){
            reviewLabel.setText("You already reviewed this book");
            reviewText.setDisable(true);
            reviewButton.setDisable(true);
            ratingBox.setDisable(true);
        }
        else{
            if(ratingBox.getItems().size() == 0)
                ratingBox.getItems().addAll("0", "1", "2", "3", "4", "5");
        }
    }
}
