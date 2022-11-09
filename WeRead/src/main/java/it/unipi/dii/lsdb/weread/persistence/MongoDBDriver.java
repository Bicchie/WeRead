package it.unipi.dii.lsdb.weread.persistence;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mongodb.client.result.DeleteResult;
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
import java.text.DateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Sorts.ascending;
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

    /*
    private class DateTimeAdapter implements JsonDeserializer<LocalDateTime>{
        @Override
        public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if(json.getAsJsonPrimitive().getAsString().equals(""))
                return LocalDateTime.now();
            return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString());
        }
    }*/

    //private MongoDBDriver(ConfigurationParameters configurationParameters) {
    private MongoDBDriver() {
        /*
        this.firstIp = configurationParameters.getMongoFirstIp();
        this.firstPort = configurationParameters.getMongoFirstPort();
        this.secondIp = configurationParameters.getMongoSecondIp();
        this.secondPort = configurationParameters.getMongoSecondPort();
        this.thirdIp = configurationParameters.getMongoThirdIp();
        this.thirdPort = configurationParameters.getMongoThirdPort();
        this.username = configurationParameters.getMongoUsername();
        this.password = configurationParameters.getMongoPassword();
        this.dbName = configurationParameters.getMongoDbName();*/
        this.dbName = "WeRead";
    }

    public static MongoDBDriver getInstance() {
        if (instance == null) {
            instance = new MongoDBDriver();
            //instance = new MongoDBDriver(Utils.readConfigurationParameters());
        }
        return instance;
    }

    public boolean startConnection() {
        try {
            /*
            String string = "mongodb://";
            if(!username.equals("")){
                string += username + ":" + password + "@";
            }
            string += firstIp + ":" + firstPort + ", " + secondIp + ":" + secondPort + ", " + thirdIp + ":" + thirdPort;*/
            String string = "mongodb://localhost:27017";
            ConnectionString connectionString = new ConnectionString(string);
            MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .readPreference(ReadPreference.secondaryPreferred())
                    .retryWrites(true)
                    .writeConcern(WriteConcern.W1) //W<x> dipende da quante repliche abbiamo, e quindi in quante repliche scrivere
                    .build();
            mongoClient = MongoClients.create(mongoClientSettings);

            database = mongoClient.getDatabase(dbName);

            pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build()));

            userCollection = database.getCollection("users");
            bookCollection = database.getCollection("books");
        } catch (Exception ex) {
            System.out.println("MongoDB is not available");
            return false;
        }
        return true;
    }

    public void closeConnection(){
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
                .append("favourite", u.getFavourite())
                .append("reviews", u.getReviewList())
                .append("readingList", u.getReadingLists());
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
        //Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter()).create();

        Gson gson = new Gson();
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
        //Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter()).create();
        Gson gson = new Gson();
        Document res = (Document) userCollection.find(eq("username", username)).projection(fields(excludeId(), include("favourite"))).first();
        List<Document> bookList = (List<Document>) res.get("favourite");
        Type bookListType = new TypeToken<ArrayList<Book>>(){}.getType();
        List<Book> favourite = gson.fromJson(gson.toJson(bookList), bookListType);
        return favourite;
    }

    public Book getBookInformation(String isbn){
        //Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter()).create();
        Gson gson = new Gson();
        Document res = (Document) bookCollection.find(eq("isbn", isbn)).first();
        Book b = null;
        try {
            b = gson.fromJson(gson.toJson(res), Book.class);
        }catch(Exception e){
            System.out.println("AAAAA");
            e.printStackTrace();
            System.out.println("AAAAA");
        }
        return b;
    }

    public List<Review> getBookReviews(String isbn){
        //Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter()).create();
        Gson gson = new Gson();
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

    public long countBookByTitle(String title){
        Pattern pattern  = Pattern.compile("^.*" + title + ".*$", Pattern.CASE_INSENSITIVE);
        return bookCollection.countDocuments(regex("title", pattern));
    }

    public List<Book> searchBookByTitle(String titleExpr, int skip, int limit){
        List<Book> bookList = null;
        //Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter()).create();

        Gson gson = new Gson();
        Pattern pattern  = Pattern.compile("^.*" + titleExpr + ".*$", Pattern.CASE_INSENSITIVE);
        List<Document> res = (List<Document>) bookCollection.find(regex("title", pattern)).projection(fields(excludeId(), include("title", "author", "category", "imageURL", "isbn"))).skip(skip).limit(limit).into(new ArrayList());
        Type bookListType = new TypeToken<ArrayList<Book>>(){}.getType();
        bookList = gson.fromJson(gson.toJson(res), bookListType);
        return  bookList;
    }

    public long countBookByCategory(String category){
        Pattern pattern  = Pattern.compile("^.*" + category + ".*$", Pattern.CASE_INSENSITIVE);
        return bookCollection.countDocuments(regex("category", pattern));
    }

    public List<Book> searchBookByCategory(String category, int skip, int limit){
        List<Book> bookList = null;
        //Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter()).create();

        Gson gson = new Gson();
        Pattern pattern  = Pattern.compile("^.*" + category + ".*$", Pattern.CASE_INSENSITIVE);
        List<Document> res = (List<Document>) bookCollection.find(regex("category", pattern)).projection(fields(excludeId(), include("title", "author", "category", "imageURL", "isbn"))).skip(skip).limit(limit).into(new ArrayList());
        Type bookListType = new TypeToken<ArrayList<Book>>(){}.getType();
        bookList = gson.fromJson(gson.toJson(res), bookListType);
        return  bookList;
    }

    public long countBookByAuthor(String author){
        Pattern pattern  = Pattern.compile("^.*" + author + ".*$", Pattern.CASE_INSENSITIVE);
        return bookCollection.countDocuments(regex("author", pattern));
    }

    public List<Book> searchBookByAuthor(String author, int skip, int limit){
        List<Book> bookList = null;
        //Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter()).create();

        Gson gson = new Gson();
        Pattern pattern  = Pattern.compile("^.*" + author + ".*$", Pattern.CASE_INSENSITIVE);
        List<Document> res = (List<Document>) bookCollection.find(regex("author", pattern)).projection(fields(excludeId(), include("title", "author", "category", "imageURL", "isbn"))).skip(skip).limit(limit).into(new ArrayList());
        Type bookListType = new TypeToken<ArrayList<Book>>(){}.getType();
        bookList = gson.fromJson(gson.toJson(res), bookListType);
        return  bookList;
    }

    public long countBookByPublisher(String publisher){
        Pattern pattern  = Pattern.compile("^.*" + publisher + ".*$", Pattern.CASE_INSENSITIVE);
        return bookCollection.countDocuments(regex("publisher", pattern));
    }

    public List<Book> searchBookByPublisher(String publisher, int skip, int limit){
        List<Book> bookList = null;
        //Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new DateTimeAdapter()).create();

        Gson gson = new Gson();
        Pattern pattern  = Pattern.compile("^.*" + publisher + ".*$", Pattern.CASE_INSENSITIVE);
        List<Document> res = (List<Document>) bookCollection.find(regex("publisher", publisher)).projection(fields(excludeId(), include("title", "author", "category", "imageURL", "isbn"))).skip(skip).limit(limit).into(new ArrayList());
        Type bookListType = new TypeToken<ArrayList<Book>>(){}.getType();
        bookList = gson.fromJson(gson.toJson(res), bookListType);
        return  bookList;
    }

    public long countUsersByString(String user){
        Pattern pattern  = Pattern.compile("^.*" + user + ".*$", Pattern.CASE_INSENSITIVE);
        return userCollection.countDocuments(regex("username", pattern));
    }

    public List<User> searchUsersByString(String user, int skip, int limit){
        List<User> userList = null;

        Gson gson = new Gson();
        Pattern pattern  = Pattern.compile("^.*" + user + ".*$", Pattern.CASE_INSENSITIVE);
        List<Document> res = (List<Document>) userCollection.find(regex("username", pattern)).projection(fields(excludeId(), include("username", "email", "name", "surname"))).skip(skip).limit(limit).into(new ArrayList());
        Type userListType = new TypeToken<ArrayList<User>>(){}.getType();
        userList = gson.fromJson(gson.toJson(res), userListType);
        return  userList;
    }

    public boolean addBookToFavorite(String username, Book book){
        Document toAdd = new Document("isbn", book.getIsbn())
                .append("title", book.getTitle())
                .append("author", book.getAuthor())
                .append("imageURL", book.getImageURL());
        Bson match = eq("username", username);
        Bson push = Updates.push("favourite", toAdd);
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
    public boolean addNewReview(Review review){
        Document toAdd = new Document("reviewId", review.getReviewId())
                .append("title", review.getBookTitle())
                .append("text", review.getText())
                .append("rating", review.getRating())
                .append("time", review.getTime())
                .append("numLikes", review.getNumLikes())
                .append("likers", review.getLikers());
        //first I update users collection
        Bson match = eq("username", review.getReviewer());
        Bson push = Updates.push("reviews", toAdd);
        UpdateResult res = userCollection.updateOne(match, push);
        if(res.getModifiedCount() < 1)
            return false;

        //update books collection
        toAdd.remove("title");
        match = eq("isbn", review.getReviewedBookIsbn());
        push = Updates.push("reviews", toAdd);
        res = bookCollection.updateOne(match, push);
        if(res.getModifiedCount() < 1) {
            //rimuovi review da utente??
            return false;
        }
        return true;
    }

    //return true only if the review has been succesfully removed by both the collections
    public boolean removeReview(Review review){
        Document toRemove = new Document("reviewId", review.getReviewId());
        //update users collection
        Bson match = eq("username", review.getReviewer());
        Bson pull = pull("reviews", toRemove);
        UpdateResult res = userCollection.updateOne(match, pull);
        if(res.getModifiedCount() < 1)
            return false;

        //update books collection
        match = eq("isbn", review.getReviewedBookIsbn());
        res = bookCollection.updateOne(match, pull);
        if(res.getModifiedCount() < 1)
            return false;
        return true;
    }

    //return true only if both the queries are successful
    public boolean addLikeReview(Review review, String liker){
        //update users collection
        Bson match = and(eq("username", review.getReviewer()), eq("reviews.reviewId", review.getReviewId()));
        Bson push = Updates.push("reviews.$.likers", liker);
        Bson inc = inc("reviews.$.numLikes", 1);
        UpdateResult res = userCollection.updateOne(match, combine(inc, push));
        if(res.getModifiedCount() < 1)
            return false;
        //update books collection
        match = and(eq("isbn", review.getReviewedBookIsbn()), eq("reviews.reviewId", review.getReviewId()));
        res = bookCollection.updateOne(match, combine(inc, push));
        if(res.getModifiedCount() < 1)
            return false;
        return true;
    }

    //return true only if both the queries are successful
    public boolean removeLikeReview(Review review, String unliker){
        //update users collection
        Bson match = and(eq("username", review.getReviewer()), eq("reviews.reviewId", review.getReviewId()));
        Bson pull = Updates.pull("reviews.$.likers", unliker);
        Bson dec = inc("reviews.$.numLikes", -1);
        UpdateResult res = userCollection.updateOne(match, combine(dec, pull));
        if(res.getModifiedCount() < 1)
            return false;
        //update books collection
        match = and(eq("isbn", review.getReviewedBookIsbn()), eq("reviews.reviewId", review.getReviewId()));
        res = bookCollection.updateOne(match, combine(dec, pull));
        if(res.getModifiedCount() < 1)
            return false;
        return true;
    }

    public boolean addNewReadingList(ReadingList rl, String user){
        Document toAdd = new Document("name", rl.getName())
                .append("numLikes", rl.getNumLikes())
                .append("books", rl.getBooks());
        Bson match = eq("username", user);
        Bson push = Updates.push("readingList", toAdd);
        UpdateResult res = userCollection.updateOne(match, push);
        if(res.getModifiedCount() < 1){
            System.out.println("FALSE");
            return false;
        }
        System.out.println("TRUE");
        return true;
    }

    public boolean removeReadingList(ReadingList rl, String user){
        Document toRemove = new Document("name", rl.getName());
        Bson match = eq("username", user);
        Bson pull = pull("readingList", toRemove);
        UpdateResult res = userCollection.updateOne(match, pull);
        if(res.getModifiedCount() < 1)
            return false;
        return true;
    }

    public boolean addBookToReadingList(String user, String rlName, Book book){
        Document toAdd = new Document("isbn", book.getIsbn())
                .append("title", book.getTitle())
                .append("author", book.getAuthor())
                .append("imageURL", book.getImageURL());
        Bson match = and(eq("username", user), eq("readingList.name", rlName));
        Bson push = Updates.push("readingList.$.books", toAdd);
        UpdateResult res = userCollection.updateOne(match, push);
        if(res.getModifiedCount() < 1)
            return false;
        return true;
    }

    public boolean removeBookFromReadingList(String user, String rlName, Book book){
        Document toRemove = new Document("isbn", book.getIsbn());
        Bson match = and(eq("username", user), eq("readingList.name", rlName));
        Bson pull = pull("readingList.$.books", toRemove);
        UpdateResult res = userCollection.updateOne(match, pull);
        if(res.getModifiedCount() < 1)
            return false;
        return true;
    }

    public boolean addLikeReadingList(String user, String rlName){
        Bson match = and(eq("username", user), eq("readingList.name", rlName));
        Bson inc = inc("readingList.$.numLikes", 1);
        UpdateResult res = userCollection.updateOne(match, inc);
        if(res.getModifiedCount() < 1)
            return false;
        return true;
    }

    public boolean removeLikeReadingList(String user, String rlName){
        Bson match = and(eq("username", user), eq("readingList.name", rlName));
        Bson dec = inc("readingList.$.numLikes", -1);
        UpdateResult res = userCollection.updateOne(match, dec);
        if(res.getModifiedCount() < 1)
            return false;
        return true;
    }

    public boolean changeUserPassword(String username, String newPassword){
        Bson match = eq("username", username);
        Bson set = set("password", newPassword);
        UpdateResult res = userCollection.updateOne(match, set);
        if(res.getModifiedCount() < 1)
            return false;
        return true;
    }

    public boolean deleteUser(String username){
        Bson match = eq("username", username);
        DeleteResult res = userCollection.deleteOne(match);
        if(res.getDeletedCount() < 1)
            return false;
        return true;
    }

    //returns a list of list of objects. Each list is composed as [title | author | imageURL | averageRating]
    public List<Map<String, Object>> topAvgRatingBooks(int booksNumber){
        Gson gson = new Gson();
        //controllo che ci siano review
        Bson match = match(ne("reviews", new ArrayList<>()));
        Bson project = project(fields(excludeId(), include("title", "author", "imageURL"), computed("averageRating", computed("$avg", "$reviews.rating"))));
        Bson sort = sort(descending("averageRating"));
        Bson limit = limit(booksNumber);
        MongoCursor<Document> iterator = (MongoCursor<Document>) bookCollection.aggregate(Arrays.asList(match, project, sort, limit)).iterator();
        //List<List<Object>> ranking = new ArrayList<>();
        List<Map<String, Object>> ranking = new ArrayList<>();
        while(iterator.hasNext()){
            Document doc = iterator.next();
            //List<Object> res = new ArrayList<>();
            Map<String, Object> res = new HashMap<>();
            res.put("title", doc.getString("title"));
            res.put("author", doc.getString("author"));
            res.put("imageURL", doc.getString("imageURL"));
            res.put("averageRating", doc.getDouble("averageRating"));
            ranking.add(res);
        }
        return ranking;
    }

    //if higher is true, we are searching for the users who assigned higher ratings in their reviews
    //if higher is false, we are searching for the users who assigned lower ratings in their reviews
    public List<Map<String, Object>> usernameListByAvgRating(int usersNumber, boolean higher){
        //controllo che ci siano review
        Bson match = match(ne("reviews", new ArrayList<>()));
        Bson project = project(fields(excludeId(), include("username"), computed("averageRating", computed("$avg", "$reviews.rating"))));
        Bson sort = (higher) ? sort(descending("averageRating")) : sort(ascending("averageRating"));
        Bson limit = limit(usersNumber);
        MongoCursor<Document> iterator = (MongoCursor<Document>) userCollection.aggregate(Arrays.asList(match, project, sort, limit)).iterator();
        List<Map<String, Object>> ranking = new ArrayList<>();
        while(iterator.hasNext()){
            Document doc = iterator.next();
            Map<String, Object> res = new HashMap<>();
            res.put("username", doc.getString("username"));
            res.put("averageRating", doc.getDouble("averageRating"));
            ranking.add(res);
        }
        return ranking;
    }

    public List<Map<String, Object>> userWithMoreReviews(int usersNumber){
        //controllo che ci siano review
        Bson match = match(ne("reviews", new ArrayList<>()));
        Bson project = project(fields(excludeId(), include("username"), computed("numReviews", computed("$size", "$reviews"))));
        Bson sort = sort(descending("numReviews"));
        Bson limit = limit(usersNumber);
        MongoCursor<Document> iterator = (MongoCursor<Document>) userCollection.aggregate(Arrays.asList(match, project, sort, limit)).iterator();
        List<Map<String, Object>> ranking = new ArrayList<>();
        while(iterator.hasNext()){
            Document doc = iterator.next();
            Map<String, Object> res = new HashMap<>();
            res.put("username", doc.getString("username"));
            res.put("numReviews", doc.getDouble("numReviews"));
            ranking.add(res);
        }
        return ranking;
    }

    //returns a list of review, each review contains partial information, likers (= null) not included for the sake of performance
    public List<Review> moreLikedReviews(int reviewsNumber){
        Gson gson = new Gson();
        Bson match = match(ne("reviews", new ArrayList<>()));
        Bson unwind = unwind("$reviews");
        Bson project = project(fields(excludeId(),
                computed("reviewId", "$reviews.reviewId"),
                computed("title", "$reviews.title"),
                computed("text", "$reviews.text"),
                computed("rating", "$reviews.rating"),
                computed("time", "$reviews.time"),
                computed("numLikes", "$reviews.numLikes")));
        Bson sort = sort(descending("numLikes"));
        Bson limit = limit(reviewsNumber);

        List<Document> res = (List<Document>) userCollection.aggregate(Arrays.asList(match, unwind, project, sort, limit)).into(new ArrayList());
        Type reviewListType = new TypeToken<ArrayList<Review>>(){}.getType();
        List<Review> ranking = gson.fromJson(gson.toJson(res), reviewListType);
        return ranking;
    }

    //returns a list of reading lists, each rl contains partial information, books (= null) not included for the sake of performance
    public List<ReadingList> moreLikedReadingLists(int rlNumber){
        Gson gson = new Gson();
        Bson match = match(ne("readingList", new ArrayList<>()));
        Bson unwind = unwind("readingList");
        Bson project = project(fields(excludeId(),
                computed("name", "$readingList.name"),
                computed("numLikes", "$readingList.numLikes")));
        Bson sort = sort(descending("numLikes"));
        Bson limit = limit(rlNumber);

        List<Document> res = (List<Document>) userCollection.aggregate(Arrays.asList(match, unwind, project, sort, limit)).into(new ArrayList());
        Type rlListType = new TypeToken<ArrayList<ReadingList>>(){}.getType();
        List<ReadingList> ranking = gson.fromJson(gson.toJson(res), rlListType);
        return ranking;
    }

    //Average length in pages of Books given the Category per year
    public List<Map<String, Object>> bookLenPerYear(String category){
        Bson match = match(eq("category", category));
        Bson group = group("$publicationYear", avg("avgLength", "$numPages"));
        Bson project = project(fields(excludeId(), computed("publicationYear", "$_id"), include("avgLength")));
        Bson sort = sort(descending("publicationYear"));

        MongoCursor<Document> iterator = (MongoCursor<Document>) bookCollection.aggregate(Arrays.asList(match, group, project, sort)).iterator();
        List<Map<String, Object>> ranking = new ArrayList<>();
        while(iterator.hasNext()){
            Document doc = iterator.next();
            Map<String, Object> res = new HashMap<>();
            res.put("publicationYear", doc.getString("publicationYear"));
            res.put("avgLength", doc.getDouble("avgLength"));
            ranking.add(res);
        }
        return ranking;
    }

    public List<Map<String, Object>> howManyBooksPerLanguage(){
        Bson group = group("$language", sum("count", 1));
        Bson project = project(fields(excludeId(), computed("language", "$_id"), include("count")));
        Bson sort = sort(descending("count"));

        MongoCursor<Document> iterator = (MongoCursor<Document>) bookCollection.aggregate(Arrays.asList(group, project, sort)).iterator();
        List<Map<String, Object>> ranking = new ArrayList<>();
        while(iterator.hasNext()){
            Document doc = iterator.next();
            Map<String, Object> res = new HashMap<>();
            res.put("language", doc.getString("language"));
            res.put("count", doc.getInteger("count"));
            ranking.add(res);
        }
        return ranking;
    }

    //How many books are published per year by a certain publisher
    public List<Map<String, Object>> howManyBooksPerYear(String publisher){
        Bson match = match(eq("publisher", publisher));
        Bson group = group("$publicationYear", sum("count", 1));
        Bson project = project(fields(excludeId(), computed("publicationYear", "$_id"), include("count")));
        Bson sort = sort(descending("publicationYear"));

        MongoCursor<Document> iterator = (MongoCursor<Document>) bookCollection.aggregate(Arrays.asList(match, group, project, sort)).iterator();
        List<Map<String, Object>> ranking = new ArrayList<>();
        while(iterator.hasNext()){
            Document doc = iterator.next();
            Map<String, Object> res = new HashMap<>();
            res.put("publicationYear", doc.getString("publicationYear"));
            res.put("count", doc.getInteger("count"));
            ranking.add(res);
        }
        return ranking;
    }

    //List of authors with greatest average rating of their books
    public List<Map<String, Object>> authorsWithHigherAvgRating(int authorsNumber){
        Bson match = match(ne("reviews", new ArrayList<>()));
        Bson unwind = unwind("$reviews");
        Bson group = group("$author", avg("avg_rat", "$reviews.rating"));
        Bson project = project(fields(excludeId(), computed("author", "$_id"), include("avg_rat")));
        Bson sort = sort(descending("avg_rat"));
        Bson limit = limit(authorsNumber);

        MongoCursor<Document> iterator = (MongoCursor<Document>) bookCollection.aggregate(Arrays.asList(match, unwind, group, project, sort, limit)).iterator();
        List<Map<String, Object>> ranking = new ArrayList<>();
        while(iterator.hasNext()){
            Document doc = iterator.next();
            Map<String, Object> res = new HashMap<>();
            res.put("author", doc.getString("author"));
            res.put("avg_rat", doc.getDouble("avg_rat"));
            ranking.add(res);
        }
        return ranking;
    }

}