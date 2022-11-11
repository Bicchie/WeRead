package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.model.Book;
import it.unipi.dii.lsdb.weread.model.ReadingList;
import it.unipi.dii.lsdb.weread.model.User;
import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.persistence.Neo4jDriver;
import it.unipi.dii.lsdb.weread.utils.Utils;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsightsPageController {
    @FXML private ImageView bookSearchIcon;
    @FXML private ImageView homeIcon;
    @FXML private ImageView userIcon;
    @FXML private ComboBox statisticsChoice;
    @FXML private Button goButton;
    @FXML private VBox resultBox;
    @FXML private VBox resultBox1;
    @FXML private Label resultLabel1;
    @FXML private VBox resultBox2;
    @FXML private Label resultLabel2;
    @FXML private VBox resultBox3;
    @FXML private Label resultLabel3;
    @FXML private VBox resultBox4;
    @FXML private Label resultLabel4;

    private MongoDBDriver mongoDBDriver;
    private Neo4jDriver neo4jDriver;

    public void initialize(){
        mongoDBDriver = MongoDBDriver.getInstance();
        neo4jDriver = Neo4jDriver.getInstance();
        statisticsChoice.getItems().addAll("Books statistics", "Users statistics");
        goButton.setOnMouseClicked(mouseEvent -> getResults(mouseEvent));
    }

    public void getResults(MouseEvent mouseEvent){
        String choice = (String) statisticsChoice.getValue();
        if(choice == null){
            Utils.showErrorAlert("Please select a statistics category");
            return;
        }
        resultBox1.getChildren().remove(1, resultBox1.getChildren().size());
        resultBox2.getChildren().remove(1, resultBox2.getChildren().size());
        resultBox3.getChildren().remove(1, resultBox3.getChildren().size());
        resultBox4.getChildren().remove(1, resultBox4.getChildren().size());

        if(choice.equals("Books statistics")){
            //first result
            Utils.showBestBookSnapshots(resultBox1, mongoDBDriver.topAvgRatingBooks(3));
            resultLabel1.setText("Top 3 books by average rating");

            //second result
            //invece di far ritornare il numero di persone che hanno favorito il libro, puoi semplicemente mettere 1,2,3 come classifica
            List<Book> bookList = neo4jDriver.mostFavoritedBooks(3);
            //creating the mapList in order to use the same showBestBookSnapshots function
            List<Map<String, Object>> mapList = new ArrayList<>();
            double count = 1;
            for(Book b: bookList){
                Map<String, Object> map = new HashMap<>();
                map.put("isbn", b.getIsbn());
                map.put("author", b.getAuthor());
                map.put("title", b.getTitle());
                map.put("imageURL", b.getImageURL());
                map.put("averageRating", count);
                count++;
                mapList.add(map);
            }
            Utils.showBestBookSnapshots(resultBox2, mapList);
            resultLabel2.setText("Top 3 books by number of users who added it to their favorites");

            //third result
            resultLabel3.setText("Number of books per language");
            List<Map<String, Object>> booksPerLanguage = mongoDBDriver.howManyBooksPerLanguage();
            CategoryAxis X = new CategoryAxis();
            NumberAxis Y = new NumberAxis();
            BarChart<String, Number> chart = new BarChart<String, Number>(X, Y);
            X.setLabel("Language");
            Y.setLabel("N° of books");

            XYChart.Series series = new XYChart.Series();
            for(Map<String, Object> m: booksPerLanguage){
               series.getData().add(new XYChart.Data(m.get("language"), m.get("count")));
            }
            chart.getData().add(series);
            chart.setLegendVisible(false);
            resultBox3.getChildren().add(chart);
            resultBox3.setPrefHeight(250);

            //fourth result
            resultLabel4.setText("Top 5 authors by average ratings");
            List<Map<String, Object>> authorMapList = mongoDBDriver.authorsWithHigherAvgRating(5);
            Utils.showAuthors(resultBox4, authorMapList);
        }
        else{
            //USERS STATISTICS

            //first result
            resultLabel1.setText("Top 3 users with more reviews than others");
            List<Map<String, Object>> userListMap = mongoDBDriver.userWithMoreReviews(3);
            List<User> userList = new ArrayList<>();
            for(Map<String, Object> m: userListMap){
                //the user is built in order to be printed with the UserReview file
                User u = new User((String) m.get("username"), "reviews number", String.valueOf(m.get("numReviews")));
                userList.add(u);
            }
            Utils.addUserPreviews(resultBox1, userList);

            //second result
            resultLabel2.setText("Top 3 reading lists by number of received likes");
            List<ReadingList> list = neo4jDriver.mostLikedReadingLists(3);
            System.out.println(list.size());
            Utils.showBestReadingLists(resultBox2, list);

            //third result
            String[] categoryArray = {"Picture Book", "Easy Reader", "Comics", "Graphic Novels", "Graphic Novels", "Fantasy", "Paranormal", "History", "Biography", "Mystery",
                    "Thriller", "Crime", "Love Poems", "Anthology", "Poetry", "Erotic Romance", "Romantic Suspense", "Romance", "Adventures", "Diary", "Family and Relationship", "Young Fiction"};
            resultLabel3.setText("Number of books per category)");
            Map<String, Integer> favorites = neo4jDriver.categoriesSummaryByFavoriteBooks(categoryArray.length);
            Map<String, Integer> readingLists = neo4jDriver.categoriesSummaryByReadingList(categoryArray.length);
            CategoryAxis X = new CategoryAxis();
            NumberAxis Y = new NumberAxis();
            BarChart<String, Number> chart = new BarChart<String, Number>(X, Y);
            X.setLabel("Categories");
            Y.setLabel("N° of books");

            XYChart.Series series1 = new XYChart.Series();
            XYChart.Series series2 = new XYChart.Series();
            for(String c: categoryArray){
                series1.getData().add(new XYChart.Data(c, favorites.get(c)));
                series2.getData().add(new XYChart.Data(c, readingLists.get(c)));
            }
            chart.getData().addAll(series1, series2);
            series1.setName("Based on favourite books");
            series2.setName("Based on reading lists");
            resultBox3.getChildren().add(chart);
            resultBox3.setPrefHeight(500);

            //fourth result
            resultLabel4.setText("Top 3 most followed users");
            List<Map<String, Object>> resList = neo4jDriver.mostFollowedUsers(3);
            //the user is built in order to be printed with the UserReview file
            List<User> users = new ArrayList<>();
            for(Map<String, Object> m: resList){
                User u = new User((String) m.get("username"), "number of followers", (String) m.get("numFollowers"));
                users.add(u);
            }
            Utils.addUserPreviews(resultBox4, users);
        }
    }
}
