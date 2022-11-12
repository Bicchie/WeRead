package it.unipi.dii.lsdb.weread;


import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.persistence.Neo4jDriver;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/welcomePage.fxml"));

        primaryStage.setTitle("WeRead");

        Boolean connectionDoneNeo4j = Neo4jDriver.getInstance().initConnection();
        Boolean connectionDoneMongoDB = MongoDBDriver.getInstance().startConnection();
        if(!connectionDoneNeo4j || !connectionDoneMongoDB)
        {
            System.out.println("Connection with DB failed!!");
        }


        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.centerOnScreen();
        primaryStage.show();
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("/img/icon.png"));

        // close the connection to Neo4J and MongoDB when the app closes
        primaryStage.setOnCloseRequest(actionEvent -> {
            Neo4jDriver.getInstance().closeConnection();
            MongoDBDriver.getInstance().closeConnection();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

}