package it.unipi.dii.lsdb.weread.persistence;

import com.google.gson.reflect.TypeToken;
import it.unipi.dii.lsdb.weread.model.*;

import com.google.gson.Gson;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import it.unipi.dii.lsdb.weread.utils.ConfigurationParameters;
import it.unipi.dii.lsdb.weread.utils.Utils;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.descending;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoDBDriver {
    //singleton pattern
    private static MongoDBDriver instance;

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection bookCollection;
    private MongoCollection userCollection;
    private CodecRegistry pojoCodecRegistry;
    private String firstIp;
    private int firstPort;
    private String secondIp;
    private int secondPort;
    private String thirdIp;
    private int thirdPort;
    private String username;
    private String password;
    private String dbName;

    private MongoDBDriver(ConfigurationParameters configurationParameters) {
        this.firstIp = configurationParameters.getMongoFirstIp();
        this.firstPort = configurationParameters.getMongoFirstPort();
        this.secondIp = configurationParameters.getMongoSecondIp();
        this.secondPort = configurationParameters.getMongoSecondPort();
        this.thirdIp = configurationParameters.getMongoThirdIp();
        this.thirdPort = configurationParameters.getMongoThirdPort();
        this.username = configurationParameters.getMongoUsername();
        this.password = configurationParameters.getMongoPassword();
        this.dbName = configurationParameters.getMongoDbName();
    }

    public static MongoDBDriver getInstance() {
        if(instance == null){
            instance = new MongoDBDriver(Utils.readConfigurationParameters());
        }
        return instance;
    }

    public boolean startConnection(){
        try{
            String string = "mongodb://";
            if(!username.equals("")){
                string += username + ":" + password + "@";
            }
            string += firstIp + ":" + firstPort + ", " + secondIp + ":" + secondPort + ", " + thirdIp + ":" + thirdPort;
            ConnectionString connectionString = new ConnectionString(string);
            MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .readPreference(ReadPreference.secondaryPreferred())
                    .retryWrites(true)
                    .writeConcern(WriteConcern.W3)
                    .build();
            mongoClient = MongoClients.create(mongoClientSettings);

            database = mongoClient.getDatabase(dbName);

            pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));

            userCollection = database.getCollection("user");
            bookCollection = database.getCollection("book");
        }
        catch (Exception ex) {
            System.out.println("MongoDB is not available");
            return false;
        }
        return true;
    }

    public void closeCollection(){
        if(mongoClient != null)
            mongoClient.close();
    }

    public boolean addUser(User u){
        Document doc = new Document("username", u.getUsername())
                .append("name", u.getName())
                .append("surname", u.getSurname())
                .append("email", u.getEmail())
                .append("password", u.getPassword())
                .append("numReviews", 0)
                .append("isAdministrator", u.getIsAdministrator())
                .append("favorite", Arrays.asList())
                .append("reviews", Arrays.asList())
                .append("readingList", Arrays.asList());
        try {
            userCollection.insertOne(doc);
        } catch (Exception e){
            System.err.println("Error in adding a new user");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean addBook(Book b){
        Document doc = new Document("isbn", b.getIsbn())
                .append("title", b.getTitle())
                .append("language", b.getLanguage())
                .append("category", b.getCategory())
                .append("publisher", b.getPublisher())
                .append("description", b.getDescription())
                .append("numPages", b.getNumPages())
                .append("imageURL", b.getImageURL())
                .append("author", b.getAuthor())
                .append("publicationYear", b.getPublicationYear())
                .append("reviews", Arrays.asList());
        try{
            bookCollection.insertOne(doc);
        } catch(Exception e){
            System.err.println("Error in adding a new user");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public User getUserInfo(String username){
        try {
            User user = null;
            Gson gson = new Gson();
            Document res = (Document) userCollection.find(eq("username", username)).first();
            user = gson.fromJson(gson.toJson(res), User.class);
            return user;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean userExists(String username){
        if(userCollection.countDocuments(eq("username", username)) > 0)
            return true;
        else
            return false;
    }

    public boolean checkLogin(String username, String password){
        if(userCollection.countDocuments(and(eq("username", username), eq("password", password))) > 0)
            return true;
        else
            return false;
    }

    public List<Book> getFavoriteOfUser(String username){
        List<Book> favorites = new ArrayList<>();
        Gson gson = new Gson();
        List<Document> res = (List<Document>) bookCollection.find(eq("username", username)).projection(fields(excludeId(), include("favorite"))).into(new ArrayList<>());
        Type bookListType = new TypeToken<ArrayList<Book>>(){}.getType();
        favorites = gson.fromJson(gson.toJson(res), bookListType);
        return favorites;
    }

    public Book getBookInformation(String isbn){
        Gson gson = new Gson();
        Document res = (Document) bookCollection.find(eq("isbn", isbn)).projection(fields(excludeId()));
        Book b = gson.fromJson(gson.toJson(res), Book.class);
        return b;
    }

    public List<Review> getBookReviews(String isbn){
        Gson gson = new Gson();
        Bson match = match(eq("isbn", isbn));
        Bson unwind = unwind("$reviews");
        Bson project = project(fields(excludeId(), include("reviews")));
        Bson sort = sort(descending("reviews.rating"));
        List<Document> res = (List<Document>)
                bookCollection.aggregate(Arrays.asList(match, unwind, project, sort)).into(new ArrayList());
        Type reviewListType = new TypeToken<ArrayList<Review>>(){}.getType();
        List<Review> reviews = gson.fromJson(gson.toJson(res), reviewListType);
        return reviews;
    }

    public
}
