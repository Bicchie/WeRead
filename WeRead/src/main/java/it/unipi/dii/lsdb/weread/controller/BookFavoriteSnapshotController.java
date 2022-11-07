package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.model.Book;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class BookFavoriteSnapshotController {
    @FXML private AnchorPane favBookPane;
    @FXML private Label bookTitle;
    @FXML private Label bookAuthor;
    @FXML private ImageView favBookImage;

    private Book book; //book showed in this preview

    /**
     * Initialization functions
     */
    public void initialize ()
    {

    }
    public void setFavoriteBook(Book b){
        this.book = b;
        bookTitle.setText(book.getTitle());
        bookAuthor.setText(book.getAuthor());
        favBookImage.setImage(new Image(book.getImageURL()));
    }

}
