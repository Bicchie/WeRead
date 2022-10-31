package it.unipi.dii.lsdb.weread.persistence;

import com.google.gson.Gson;
import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import it.unipi.dii.lsdb.weread.utils.ConfigurationParameters;
import it.unipi.dii.lsdb.weread.utils.Utils;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
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
                .append("isAdministrator", u.isAdmin())
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
                .append("language", b.getTitle())
                .append("category", b.getCategory())
                .append("publisher", b.getPublisher())
                .append("description", b.getDescription())
                .append("numPages", b.getNumPages())
                .append("imageURL", b.getImageURL())
                .append("author", b.getAuthor())
                .append("publicationYear", b.getPubYear())
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
        try{
            if(userCollection.countDocuments(eq("username", username)) > 0)
                return true;
            else
                return false;
        } catch (Exception e){
            e.printStackTrace();
            return true; //in case of error returning true is better so that the db will not be modified by successive inserting query
        }
    }

    public boolean checkLogin(String username, String password){
        try{
            if(userCollection.countDocuments(and(eq("username", username), eq("password", password))) > 0)
                return true;
            else
                return false;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
