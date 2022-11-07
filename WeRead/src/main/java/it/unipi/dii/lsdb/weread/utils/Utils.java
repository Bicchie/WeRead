package it.unipi.dii.lsdb.weread.utils;

import it.unipi.dii.lsdb.weread.controller.BookPreviewController;
import it.unipi.dii.lsdb.weread.controller.ReviewController;
import it.unipi.dii.lsdb.weread.model.Book;
import it.unipi.dii.lsdb.weread.model.Review;
import it.unipi.dii.lsdb.weread.utils.ConfigurationParameters;

import com.thoughtworks.xstream.XStream;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
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
    private static Pane createReviewBig(Review review)
    {
        Pane pane = null;
        try {
            FXMLLoader loader = new FXMLLoader(Utils.class.getResource("/review.fxml"));
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
    public static void addReviewsBig(VBox vBox, List<Review> reviews) {
        for (Review rev : reviews) {
            /*HBox row = new HBox();
            row.setStyle("-fx-padding: 10px");
            row.setSpacing(20);*/
            Pane revPane = createReviewBig(rev);
            vBox.getChildren().add(revPane);
            ;
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
    public static void addRBookPreviewsBig(VBox vBox, List<Book> bookList) {
        for (Book rev : bookList) {
            /*HBox row = new HBox();
            row.setStyle("-fx-padding: 10px");
            row.setSpacing(20);*/
            Pane revPane = createBookPreviewBig(rev);
            vBox.getChildren().add(revPane);
            ;
        }
    }
}
