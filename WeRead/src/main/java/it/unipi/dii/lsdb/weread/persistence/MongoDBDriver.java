package it.unipi.dii.lsdb.weread.persistence;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.lsdb.weread.model.*;

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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.*;
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

    //we need to establish how the LocalDateTime must be deserialized
    private class DateTimeAdapter implements JsonDeserializer<LocalDateTime>{
        @Override
        public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString());
        }
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
        User user = null;
        //we need to establish how the LocalDateTime must be deserialized
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter()).create();

        Document res = (Document) userCollection.find(eq("username", username)).first();
        user = gson.fromJson(gson.toJson(res), User.class);
        return user;
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
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter()).create();
        Document res = (Document) userCollection.find(eq("username", username)).projection(fields(excludeId(), include("favourite"))).first();
        List<Document> bookList = (List<Document>) res.get("favourite");
        Type bookListType = new TypeToken<ArrayList<Book>>(){}.getType();
        List<Book> favourite = gson.fromJson(gson.toJson(bookList), bookListType);
        return favourite;
    }

    public Book getBookInformation(String isbn){
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter()).create();
        Document res = (Document) bookCollection.find(eq("isbn", isbn)).first();
        Book b = gson.fromJson(gson.toJson(res), Book.class);
        return b;
    }

    public List<Review> getBookReviews(String isbn){
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter()).create();
        Bson match = match(eq("isbn", isbn));
        Bson unwind = unwind("$reviews");
        Bson project = project(fields(excludeId(), include("reviews")));
        Bson sort = sort(descending("reviews.rating"));

        MongoCursor<Document> iterator = (MongoCursor<Document>) bookCollection.aggregate(Arrays.asList(match, unwind, project, sort)).iterator();
        List<Review> reviews = new ArrayList<>();
        while(iterator.hasNext()){
            Document doc = iterator.next();
            Document reviewDocument = (Document) doc.get("reviews");
            Review rev = gson.fromJson(gson.toJson(reviewDocument), Review.class);
            reviews.add(rev);
        }
        return reviews;
    }

    public List<Book> searchBookByTitle(String titleExpr){
        List<Book> bookList = null;
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter()).create();

        Pattern pattern  = Pattern.compile("^.*" + titleExpr + ".*$", Pattern.CASE_INSENSITIVE);
        List<Document> res = (List<Document>) bookCollection.find(regex("title", pattern)).projection(fields(excludeId(), include("title", "author", "category", "imageURL", "isbn"))).into(new ArrayList());
        Type bookListType = new TypeToken<ArrayList<Book>>(){}.getType();
        bookList = gson.fromJson(gson.toJson(res), bookListType);
        return  bookList;
    }

    public List<Book> searchBookByCategory(String category){
        List<Book> bookList = null;
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter()).create();

        Pattern pattern  = Pattern.compile("^.*" + category + ".*$", Pattern.CASE_INSENSITIVE);
        List<Document> res = (List<Document>) bookCollection.find(regex("category", pattern)).projection(fields(excludeId(), include("title", "author", "category", "imageURL", "isbn"))).into(new ArrayList());
        Type bookListType = new TypeToken<ArrayList<Book>>(){}.getType();
        bookList = gson.fromJson(gson.toJson(res), bookListType);
        return  bookList;
    }

    public List<Book> searchBookByAuthor(String author){
        List<Book> bookList = null;
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter()).create();

        Pattern pattern  = Pattern.compile("^.*" + author + ".*$", Pattern.CASE_INSENSITIVE);
        List<Document> res = (List<Document>) bookCollection.find(regex("author", pattern)).projection(fields(excludeId(), include("title", "author", "category", "imageURL", "isbn"))).into(new ArrayList());
        Type bookListType = new TypeToken<ArrayList<Book>>(){}.getType();
        bookList = gson.fromJson(gson.toJson(res), bookListType);
        return  bookList;
    }

    public List<Book> searchBookByPublisher(String publisher){
        List<Book> bookList = null;
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter()).create();

        Pattern pattern  = Pattern.compile("^.*" + publisher + ".*$", Pattern.CASE_INSENSITIVE);
        List<Document> res = (List<Document>) bookCollection.find(regex("publisher", publisher)).projection(fields(excludeId(), include("title", "author", "category", "imageURL", "isbn"))).into(new ArrayList());
        Type bookListType = new TypeToken<ArrayList<Book>>(){}.getType();
        bookList = gson.fromJson(gson.toJson(res), bookListType);
        return  bookList;
    }

    public boolean addBookToFavorite(String username, Book book){
        Document toAdd = new Document("isbn", book.getIsbn())
                .append("title", book.getTitle())
                .append("author", book.getAuthor())
                .append("imageURL", book.getImageURL());
        Bson match = eq("username", username);
        Bson push = push("favourite", toAdd);
        UpdateResult res = userCollection.updateOne(match, push);
        if(res.getModifiedCount() < 1)
            return false;
        else
            return true;
    }

    public boolean removeBookFromFavorite(String username, Book book){
        Document toRemove = new Document("isbn", book.getIsbn());
        Bson match = eq("username", username);
        Bson pull = pull("favourite", toRemove);
        UpdateResult res = userCollection.updateOne(match, pull);
        if(res.getModifiedCount() == 1)
            return true;
        else
            return false;
    }

    //returns true only if both the queries in both the collections are ok
    public boolean addNewReview(User user, Book book, Review review){
        Document toAdd = new Document("reviewId", review.getReviewId())
                .append("title", review.getBookTitle())
                .append("text", review.getText())
                .append("rating", review.getRating())
                .append("time", review.getTime().toString())
                .append("numLikes", review.getNumLikes())
                .append("likers", review.getNumLikes());
        //first I update users collection
        Bson match = eq("username", user.getUsername());
        Bson push = push("reviews", toAdd);
        UpdateResult res = userCollection.updateOne(match, push);
        if(res.getModifiedCount() < 1)
            return false;
        
        //update books collection
        toAdd.remove("title");
        match = eq("isbn", book.getIsbn());
        push = push("reviews", toAdd);
        res = bookCollection.updateOne(match, push);
        if(res.getModifiedCount() < 1) {
            //rimuovi review da utente??
            return false;
        }
        return true;
    }
}
