package it.unipi.dii.lsdb.weread.utils;

import it.unipi.dii.lsdb.weread.controller.BookPreviewController;
import it.unipi.dii.lsdb.weread.controller.ReviewController;
import it.unipi.dii.lsdb.weread.model.Book;
import it.unipi.dii.lsdb.weread.model.ReadingList;
import it.unipi.dii.lsdb.weread.model.Review;
import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.persistence.Neo4jDriver;
import it.unipi.dii.lsdb.weread.utils.ConfigurationParameters;

import com.thoughtworks.xstream.XStream;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

public class Utils {
    /**
     * This function is used to read the config.xml file
     * @return  ConfigurationParameters instance
     */
    public static ConfigurationParameters readConfigurationParameters () {
        XStream xs = new XStream();

        String text = null;
        try {
            text = new String(Files.readAllBytes(Paths.get("./config.xml")));
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return (ConfigurationParameters) xs.fromXML(text);

    }

    private static void sleep(int i) {
    }

    /**
     * This function is used to validate the config.xml with the config.xsd
     * @return  true if config.xml is well formatted, otherwise false
     */
    private static boolean validConfigurationParameters() {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Document document = documentBuilder.parse("./config.xml");
            Schema schema = schemaFactory.newSchema(new StreamSource("./config.xsd"));
            schema.newValidator().validate(new DOMSource(document));
        }
        catch (Exception e) {
            if (e instanceof SAXException)
                System.out.println("Validation Error: " + e.getMessage());
            else
                System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Function that shows an error alert
     * @param text  Text to be shown
     */
    public static void showErrorAlert (String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(text);
        alert.setHeaderText("Ops.. Something went wrong..");
        alert.setTitle("Error");
        ImageView imageView = new ImageView(new Image("/img/error.png"));
        alert.setGraphic(imageView);
        alert.show();
    }

    /**
     * Snippet of code for jumping in the next scene
     * Every scene has associated its specific controller
     * @param fileName      The name of the file in which i can obtain the GUI (.fxml)
     * @param event         The event that leads to change the scene
     * @return The new controller, because I need to pass some parameters
     */
    public static Object changeScene (String fileName, Event event)
    {
        Scene scene = null;
        FXMLLoader loader = null;
        try {
            loader=new FXMLLoader(Utils.class.getResource(fileName));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.show();
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This function create a pane that contains a review snapshot
     * @param review    recipe to display in the snapshot
     * @return
     */
    private static Pane createReviewBig(Review review, boolean bookPage)
    {
        Pane pane = null;
        String fxml;
        if(bookPage)
            fxml = "/review_bookPage.fxml";
        else
            fxml = "/review.fxml";
        try {
            FXMLLoader loader = new FXMLLoader(Utils.class.getResource(fxml));
            pane = (Pane) loader.load();
            ReviewController reviewController =
                    (ReviewController) loader.getController();
            reviewController.setReview(review);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pane;
    }

    /**
     * Function that adds the snapshots of the reviews, 1 for each row
     * @param vBox      VBox in which I have to show the snapshots
     * @param reviews   Recipes to show
     */
    public static void addReviewsBig(VBox vBox, List<Review> reviews, boolean bookPage) {
        for (Review rev : reviews) {
            Pane revPane = createReviewBig(rev, bookPage);
            vBox.getChildren().add(revPane);
        }
    }

    /**
     * This function create a pane that contains a book snapshot
     * @param book    recipe to display in the snapshot
     * @return
     */
    private static Pane createBookPreviewBig(Book book)
    {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(Utils.class.getResource("/bookPreview.fxml"));
            pane = (Pane) loader.load();
            BookPreviewController bookPreviewController =
                    (BookPreviewController) loader.getController();
            bookPreviewController.setBook(book);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pane;
    }

    /**
     * Function that adds the snapshots of the books, 1 for each row
     * @param vBox      VBox in which I have to show the snapshots
     * @param bookList   Recipes to show
     */
    public static void addBookPreviewsBig(VBox vBox, List<Book> bookList) {
        for (Book rev : bookList) {
            /*HBox row = new HBox();
            row.setStyle("-fx-padding: 10px");
            row.setSpacing(20);*/
            Pane revPane = createBookPreviewBig(rev);
            vBox.getChildren().add(revPane);
            ;
        }
    }

    public static String buildReviewId(String username, String isbn){return username + ":" + isbn;}

    //check the consistency of the queries in both the databases
    public static boolean addBookToFavorite(String username, Book book){
        MongoDBDriver mongoDBDriver = MongoDBDriver.getInstance();
        Neo4jDriver neo4jDriver = Neo4jDriver.getInstance();

        if(mongoDBDriver.addBookToFavorite(username, book)){
            if(!neo4jDriver.favoritesBook(username, book.getIsbn())) {
                //must remove the copy in mongodb
                mongoDBDriver.removeBookFromFavorite(username, book);
                showErrorAlert("Error in adding to favorite books in neo4j");
                return false;
            }
            else
                return true;
        }
        else{
            showErrorAlert("Error in adding to favorite books in mongodb");
            return false;
        }
    }

    //check the consistency of the queries in both the databases
    public static boolean removeBookFromFavorite(String username, Book book){
        MongoDBDriver mongoDBDriver = MongoDBDriver.getInstance();
        Neo4jDriver neo4jDriver = Neo4jDriver.getInstance();

        if(mongoDBDriver.removeBookFromFavorite(username, book)){
            if(!neo4jDriver.removeFavoriteBook(username, book.getIsbn())) {
                //must remove the copy in mongodb
                mongoDBDriver.addBookToFavorite(username, book);
                showErrorAlert("Error in removing from favorite books in neo4j");
                return false;
            }
            else
                return true;
        }
        else{
            showErrorAlert("Error in removing from favorite books in mongodb");
            return false;
        }
    }

    //check the consistency of the queries in both the databases
    public static boolean addBookToReadingList(String username, String rlName, Book book){
        MongoDBDriver mongoDBDriver = MongoDBDriver.getInstance();
        Neo4jDriver neo4jDriver = Neo4jDriver.getInstance();

        if(mongoDBDriver.addBookToReadingList(username, rlName, book)){
            if(!neo4jDriver.readingListHasBook(username+":"+rlName, book.getIsbn())){
                mongoDBDriver.removeBookFromReadingList(username, rlName, book);
                showErrorAlert("Error in adding a book to a reading list in neo4j");
                return false;
            }
            else
                return true;
        }
        else{
            showErrorAlert("Error in adding a book to a reading list in mongodb");
            return false;
        }
    }

    //check the consistency of the queries in both the databases
    public static boolean removeBookFromReadingList(String username, String rlName, Book book){
        MongoDBDriver mongoDBDriver = MongoDBDriver.getInstance();
        Neo4jDriver neo4jDriver = Neo4jDriver.getInstance();

        if(mongoDBDriver.removeBookFromReadingList(username, rlName, book)){
            if(!neo4jDriver.removeBookFromReadingList(book.getIsbn(), username+":"+rlName)){
                mongoDBDriver.addBookToReadingList(username, rlName, book);
                showErrorAlert("Error in removing a book from a reading list in neo4j");
                return false;
            }
            else
                return true;
        }
        else{
            showErrorAlert("Error in removing a book from a reading list in mongodb");
            return false;
        }
    }

    //check the consistency of the queries in both the databases
    public static boolean createNewReadingList(String username, ReadingList rl){
        MongoDBDriver mongoDBDriver = MongoDBDriver.getInstance();
        Neo4jDriver neo4jDriver = Neo4jDriver.getInstance();

        if(mongoDBDriver.addNewReadingList(rl, username)){
            if(!neo4jDriver.newReadingList(new ReadingList(username + ":" + rl.getName()))){
                mongoDBDriver.removeReadingList(rl, username);
                showErrorAlert("Error in creating a new reading list in neo4j");
                return false;
            }
            else
                return true;
        }
        else{
            showErrorAlert("Error in creating a new reading list in mongodb");
            return false;
        }
    }
}
