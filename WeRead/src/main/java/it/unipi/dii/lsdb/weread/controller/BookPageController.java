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
    @FXML private Label numFavorite;
    @FXML private Label publicationYear;
    @FXML private Label numPages;
    @FXML private Label bookPublisher;
    @FXML private Label bookLanguage;
    @FXML private Text bookTitle;
    @FXML private ImageView bookImage;
    @FXML private Label bookAuthor;
    @FXML private Label bookCategory;

    private Book book; //book showed in this page
    private MongoDBDriver mongoDBDriver;
    private Neo4jDriver neo4jDriver;
    private Session session;

    public void initialize(){
        mongoDBDriver = MongoDBDriver.getInstance();
        neo4jDriver = Neo4jDriver.getInstance();
        session = Session.getInstance();
        //setta vari listener
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
        publicationYear.setText(String.valueOf(book.getPublicationYear()));
        numFavorite.setText(neo4jDriver.numberFavoritesOfBook(book.getIsbn()) + " users add this book to favorites");

        if(neo4jDriver.checkUserFavoritesBook(session.getLoggedUser().getUsername(), book.getIsbn())){
            //if the user has already add this book to its favorites
            favoriteButton.setText("Remove from favorites");
            favoriteButton.setOnMouseClicked(mouseEvent -> remFavorite(mouseEvent));
        }
        else{
            //if the user has not already add this book to its favorites
            favoriteButton.setText("Add to favorites");
            favoriteButton.setOnMouseClicked(mouseEvent -> addFavorite(mouseEvent));
        }

        List<ReadingList> readingLists = session.getLoggedUser().getReadingLists();
        for(ReadingList rl: readingLists){
            rlBox.getItems().add(rl.getName());
        }

        bookDescription.setText(book.getDescription());
        List<Review> reviewList = session.getLoggedUser().getReviewList();
        //check if the user has or has not reviewed this book yet
        String id = session.getLoggedUser().getUsername() + ":" + book.getIsbn();
        boolean reviewed = false;
        for(Review r: reviewList){
            if(r.getReviewId().equals(id)) {
                reviewed = true;
                break;
            }
        }
        if(reviewed){
            reviewLabel.setText("You already reviewed this book");
            reviewText.setDisable(true);
            reviewButton.setDisable(true);
            ratingBox.setDisable(true);
        }
        else{
            ratingBox.getItems().addAll("1", "2", "3", "4", "5");
        }
        //aggiungi le review
        Utils.addReviewsBig(reviewsVbox, book.getReviews());
    }

    private void addFavorite(MouseEvent mouseEvent) {
    }

    private void remFavorite(MouseEvent mouseEvent) {
    }
}
