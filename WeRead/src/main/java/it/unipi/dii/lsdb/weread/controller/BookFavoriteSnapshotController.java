package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.model.Book;
import it.unipi.dii.lsdb.weread.model.Session;
import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class BookFavoriteSnapshotController {
    @FXML private AnchorPane favBookPane;
    @FXML private Label bookTitle;
    @FXML private Label bookAuthor;
    @FXML private ImageView favBookImage;
    @FXML private Label avgRatingLabel;

    private Book book; //book showed in this preview
    private MongoDBDriver mongoDBDriver;

    /**
     * Initialization functions
     */
    public void initialize ()
    {
        mongoDBDriver = MongoDBDriver.getInstance();
        favBookPane.setOnMouseClicked(mouseEvent -> showBookPage(mouseEvent));
    }

    private void showBookPage(MouseEvent mouseEvent){
        if(Session.getInstance().getLoggedUser().getIsAdministrator())
            return;
        this.book = mongoDBDriver.getBookInformation(book.getIsbn());
        BookPageController bookPageController = (BookPageController) Utils.changeScene("/bookPage.fxml", mouseEvent);
        bookPageController.setBook(book);
    }


    public void setFavoriteBook(Book b, double avgRating){
        this.book = b;
        if(avgRating != 0){
            if(avgRating == (int) avgRating)
                avgRatingLabel.setText(String.valueOf((int) avgRating));
            else
                avgRatingLabel.setText(String.format("%.2f",avgRating));
        } else {
            avgRatingLabel.setVisible(false);
        }
        bookTitle.setText(book.getTitle());
        bookAuthor.setText(book.getAuthor());
        favBookImage.setImage(new Image(book.getImageURL()));
    }

}
