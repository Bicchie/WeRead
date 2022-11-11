package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.model.Book;
import it.unipi.dii.lsdb.weread.model.Session;
import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.persistence.Neo4jDriver;
import it.unipi.dii.lsdb.weread.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;


public class BookPreviewController {
    @FXML private AnchorPane bookPane;
    @FXML private ImageView bookImage;
    @FXML private Label bookTitle;
    @FXML private Label bookAuthor;
    @FXML private Label bookCategory;

    private MongoDBDriver mongoDBDriver;
    private Neo4jDriver neo4jDriver;
    private Book book; //book showed in this preview

    public void initialize(){
        mongoDBDriver = MongoDBDriver.getInstance();
        neo4jDriver = Neo4jDriver.getInstance();
        bookPane.setOnMouseClicked(mouseEvent -> showBookPage(mouseEvent));
    }

    private void showBookPage(MouseEvent mouseEvent) {
        if(Session.getInstance().getLoggedUser().getIsAdministrator())
            return;
        if(book.getNumPages() <= 0){
            book = mongoDBDriver.getBookInformation(book.getIsbn());
        }
        BookPageController bookPageController = (BookPageController) Utils.changeScene("/bookPage.fxml", mouseEvent);
        bookPageController.setBook(book);
    }

    public void setBook(Book b){
        this.book = b;
        bookTitle.setText(book.getTitle());
        bookAuthor.setText(book.getAuthor());
        bookImage.setImage(new Image(book.getImageURL()));
        if(book.getCategory() == null){
            String category = neo4jDriver.getCategory(book.getIsbn());
            bookCategory.setText(category);
        } else {
            bookCategory.setText(book.getCategory());
        }
    }
}
