package it.unipi.dii.lsdb.weread.persistence;

import it.unipi.dii.lsdb.weread.utils.ConfigurationParameters;
import it.unipi.dii.lsdb.weread.utils.Utils;
import it.unipi.dii.lsdb.weread.model.User;
import it.unipi.dii.lsdb.weread.model.Book;
import it.unipi.dii.lsdb.weread.model.ReadingList;
import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.neo4j.driver.Values.parameters;


public class Neo4jDriver{
    private static Neo4jDriver instance = null; // Singleton Instance

    private Driver driver;
    private String ip;
    private int port;
    private String username;
    private String password;

    private Neo4jDriver(ConfigurationParameters configurationParameters)
    {
        this.ip = configurationParameters.getNeo4jIp();
        this.port = configurationParameters.getNeo4jPort();
        this.username = configurationParameters.getNeo4jUsername();
        this.password = configurationParameters.getNeo4jPassword();
    }

    public static Neo4jDriver getInstance()
    {
        if (instance == null)
        {
            instance = new Neo4jDriver(Utils.readConfigurationParameters());
        }
        return instance;
    }

    /**
     * Method that inits the Driver
     */
    public boolean initConnection()
    {
        try
        {
            driver = GraphDatabase.driver("neo4j://" + ip + ":" + port, AuthTokens.basic(username, password));
            driver.verifyConnectivity();
        } catch (Exception e)
        {
            System.out.println("Neo4J is not available");
            return false;
        }
        return true;
    }

    /**
     * Method for closing the connection of the Driver
     */
    public void closeConnection ()
    {
        if (driver != null)
            driver.close();
    }

    /***************************** CREATE ******************************************/

    /**
     * Create a user node
     * @param u  Object User that will be added
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean newUser(User u)
    {
        try ( Session session = driver.session())
        {
            String username = u.getUsername();
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "CREATE (u:User {username: $username})",
                        parameters( "username", username) );
                return null;
            });
            return true;
        }
        catch (Exception ex)
        {
            System.err.println("Error in adding a new User in Neo4J");
            return false;
        }
    }

    /**
     * Create a book node
     * @param b  Object Book that will be added
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean newBook(Book b)
    {
        try ( Session session = driver.session())
        {
            String title = b.getTitle();
            String isbn = b.getIsbn();
            String author = b.getAuthor();
            String imageURL = b.getImageURL();

            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "CREATE (ee: Book {title: $title, isbn: $isbn, author: $author, imageURL: $imgURL})",
                        parameters( "title", title, "isbn", isbn, "author", author, "imgURL", imageURL) );
                return null;
            });
            return true;
        }
        catch (Exception ex)
        {
            System.err.println("Error in adding a new Book in Neo4J");
            return false;
        }
    }

    /**
     * Create a readinglist node
     * @param rl  Object ReadingList that will be added
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean newReadingList(ReadingList rl)
    {
        try ( Session session = driver.session())
        {
            String name = rl.getName();

            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "CREATE (ee: ReadingList {name: $readinglistname})",
                        parameters( "readinglistname", name) );
                return null;
            });
            return true;
        }
        catch (Exception ex)
        {
            System.err.println("Error in adding a new ReadingList in Neo4J");
            return false;
        }
    }

    /**
     * Create a is interested to relation
     * @param username The user
     * @param category Category in which the user is interested to
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean isInterestedTo(String username, String category)
    {
        try ( Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (u: User) WHERE u.username = $username\n" +
                            "MATCH (c: Category) WHERE c.name = $category\n" +
                            "CREATE (u) - [:IS_INTERESTED_TO] -> (c)",
                        parameters( "username", username, "category", category) );
                return null;
            });
            return true;
        }
        catch (Exception ex)
        {
            System.err.println("Error in adding a new Is Interested To relation in Neo4J");
            return false;
        }
    }

    /**
     * Create has relation
     * @param name The name of the ReadingList
     * @param isbn Isbn of the book inserted in the ReadingList
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean readingListHasBook(String name, String isbn)
    {
        try ( Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (rl: ReadingList) WHERE rl.name = $name\n" +
                            "MATCH (b: Book) WHERE b.isbn = $isbn\n" +
                            "CREATE (rl) - [:HAS] -> (b)",
                        parameters( "name", name, "isbn", isbn) );
                return null;
            });
            return true;
        }
        catch (Exception ex)
        {
            System.err.println("Error in adding a new Has relation in Neo4J");
            return false;
        }
    }

    /**
     * Create like relation
     * @param username The username of the user that likes the ReadingList
     * @param name Name of the ReadingList liked
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean likesReadingList(String username, String name)
    {
        try ( Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (u: User) WHERE u.username = $username\n" +
                            "MATCH (rl: ReadingList) WHERE rl.name = $name\n" +
                            "CREATE (u) - [:LIKES] -> (rl)",
                        parameters( "username", username, "name", name) );
                return null;
            });
            return true;
        }
        catch (Exception ex)
        {
            System.err.println("Error in adding a new Like relation in Neo4J");
            return false;
        }
    }

    /**
     * Create favorites relation
     * @param username The username of the user that favorites the Book
     * @param isbn Isbn of the Book favorited
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean favoritesBook(String username, String isbn)
    {
        try ( Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (u: User) WHERE u.username = $username\n" +
                            "MATCH (b: Book) WHERE b.isbn = $isbn\n" +
                            "CREATE (u) - [:FAVORITES] -> (b)",
                        parameters( "username", username, "isbn", isbn) );
                return null;
            });
            return true;
        }
        catch (Exception ex)
        {
            System.err.println("Error in adding a new Favorites relation in Neo4J");
            return false;
        }
    }


    /**
     * Create belongs to relation
     * @param isbn Isbn of the book which belongs to the category
     * @param category The category
     * @return true if operation is successfully executed, false otherwise
     */
    public boolean bookBelongToCategory(String isbn, String category)
    {
        try ( Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (b: Book) WHERE b.isbn = $isbn\n" +
                            "MATCH (c: Category) WHERE c.name = $category\n" +
                            "CREATE (b) - [:BELONGS_TO] -> (c)",
                        parameters( "isbn", isbn, "category", category) );
                return null;
            });
            return true;
        }
        catch (Exception ex)
        {
            System.err.println("Error in adding a new Belongs To relation in Neo4J");
            return false;
        }
    }


    /***************************** READ ******************************************/

    /**
     * Retrieve the number of followed users
     * @param username Username of the user
     * @return the number of followed users
     */
    public int numberFollowed(String username)
    {
        int followed = 0;

        try(Session session = driver.session())
        {
            followed = session.readTransaction((TransactionWork<Integer>) tx -> {
                Result r = tx.run("MATCH (:User {username: $username })-[r:FOLLOWS]->()\n" +
                                     "RETURN COUNT (r) AS numFollowed",
                                    parameters("username",username));
                Record rec = r.next();
                return rec.get(0).asInt();
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return followed;
    }

    /**
     * Retrieve the number of followers of the user
     * @param username Username of the user
     * @return the number of followed users
     */
    public int numberFollowers(String username)
    {
        int followers = 0;

        try(Session session = driver.session())
        {
            followers = session.readTransaction((TransactionWork<Integer>) tx -> {
                Result r = tx.run("MATCH (:User {username: $username })<-[r:FOLLOWS]-()\n" +
                                     "RETURN COUNT (r) AS numFollowers",
                        parameters("username",username));
                Record rec = r.next();
                return rec.get(0).asInt();
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return followers;
    }


    /**
     * Retrieve the number of favorites received by a book
     * @param isbn Isbn of the book
     * @return the number of favorites received
     */
    public int numberFavoritesOfBook(String isbn)
    {
        int numFavorites = 0;

        try(Session session = driver.session())
        {
            numFavorites = session.readTransaction((TransactionWork<Integer>) tx -> {
                Result r = tx.run("MATCH (:Book {isbn: $isbn })<-[r:FAVORITES]-()\n" +
                                     "RETURN COUNT (r) AS numFavorites",
                        parameters("isbn",isbn));
                Record rec = r.next();
                return rec.get(0).asInt();
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return numFavorites;
    }


    /**
     * Retrieve the number of likes received by reading list
     * @param readinglistname Name of the reading list
     * @return the number of likes received
     */
    public int numberLikesReadingList(String readinglistname)
    {
        int numLikes = 0;

        try(Session session = driver.session())
        {
            numLikes = session.readTransaction((TransactionWork<Integer>) tx -> {
                Result r = tx.run("MATCH (:ReadingList {id: $readinglistname })<-[r:LIKES]-()\n" +
                                     "RETURN COUNT (r) AS numLikes",
                        parameters("readinglistname",readinglistname));
                Record rec = r.next();
                return rec.get(0).asInt();
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return numLikes;
    }

    /**
     * Check if a user follows another user
     * @param usernameA
     * @param usernameB
     * @return True if usernameA follows usernameB
     */
    public Boolean checkUserFollowsUser(String usernameA, String usernameB)
    {
        Boolean check = false;

        try(Session session = driver.session())
        {
            check = session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result r = tx.run("MATCH (a:User{id: $usernameA })-[r:FOLLOWS]->(b:User{id: $usernameB })\n" +
                                     "RETURN COUNT (*)",
                        parameters("usernameA",usernameA, "usernameB", usernameB));
                Record rec = r.next();
                if(rec.get(0).asInt()==0)
                    return false;
                else
                    return true;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return check;
    }


    /**
     * Check if a user likes a reading list
     * @param username
     * @param readinglistname
     * @return True if username likes readinglist
     */
    public Boolean checkUserLikesReadingList(String username, String readinglistname)
    {
        Boolean check = false;

        try(Session session = driver.session())
        {
            check = session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result r = tx.run("MATCH (a:User{id: $user })-[r:LIKES]->(b:ReadingList{id: $readinglistname })\n" +
                                     "RETURN COUNT (*)",
                        parameters("username",username, "readinglistname", readinglistname));
                Record rec = r.next();
                if(rec.get(0).asInt()==0)
                    return false;
                else
                    return true;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return check;
    }

    /**
     * Check if a user favorites a book
     * @param username
     * @param isbn
     * @return True if username likes readinglist
     */
    public Boolean checkUserFavoritesBook(String username, String isbn)
    {
        Boolean check = false;

        try(Session session = driver.session())
        {
            check = session.readTransaction((TransactionWork<Boolean>) tx -> {
                Result r = tx.run("MATCH (a:User{id: $username })-[r:FAVORITES]->(b:Book{isbn: $isbn })\n" +
                                     "RETURN COUNT (*)",
                        parameters("username",username, "isbn", isbn));
                Record rec = r.next();
                if(rec.get(0).asInt()==0)
                    return false;
                else
                    return true;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return check;
    }


    /***************************** UPDATE ******************************************/


    /**
     * Update the information about the user, given the username (username cannot be changed)
     * @param oldusername
     * @param newusername
     */
    public void updateUser(String oldusername, String newusername)
    {
        try(Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Boolean>) tx -> {
                tx.run("MATCH (u:User {username: $oldusername })\n" +
                          "SET u.username = $newusername ,  u.id = $newusername",
                        parameters("oldusername", oldusername, "newusername", newusername));
                return null;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /***************************** DELETE ******************************************/

    /**
     * Delete a User node and detach all its relations
     * @param username username of the user that I want to delete
     */
    public boolean deleteUser(String username)
    {
        try ( Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (u:User) WHERE u.username = $username DETACH DELETE u",
                        parameters( "username", username) );
                return null;
            });
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     * Delete follows relation
     * @param loggedusername
     * @param targetusername
     */
    public boolean unfollow(String loggedusername, String targetusername)
    {
        try ( Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (:User {username: $loggedusername})-[r:FOLLOWS]->(:User {username: $targetusername}) DELETE r",
                        parameters( "loggedusername", loggedusername, "targetusername", targetusername) );
                return null;
            });
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     *  Delete is interested to relation
     * @param loggedusername
     * @param targetcategory
     */
    public boolean removeInterestInCategory(String loggedusername, String targetcategory)
    {
        try ( Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (:User {username: $loggedusername>})-[r:IS_INTERESTED_TO]->(:Category {name: $targetcategory}) DELETE r",
                        parameters( "loggedusername", loggedusername, "targetcategory", targetcategory) );
                return null;
            });
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     *  Delete a reading list node
     * @param readinglistname
     */
    public boolean deleteReadingList(String readinglistname)
    {
        try ( Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (r:ReadingList {name: $readinglistname}) DETACH DELETE r",
                        parameters( "readinglistname", readinglistname) );
                return null;
            });
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     *  Delete a LIKES relation
     * @param loggedusername
     * @param targetrl
     */
    public boolean unlikeReadingList(String loggedusername, String targetrl)
    {
        try ( Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (:User {username: $loggedusername})-[r:LIKES]->(:ReadingList {name: $targetrl }) DELETE r",
                        parameters( "loggedusername", loggedusername, "targetrl", targetrl) );
                return null;
            });
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /**
     *  Delete a HAS relation
     * @param isbn
     * @param targetrl
     */
    public boolean removeBookFromReadingList(String isbn, String targetrl)
    {
        try ( Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (:ReadingList {name: $targetrl})-[r:HAS]->(:Book {isbn: $isbn}) DELETE r",
                        parameters( "targetrl", targetrl, "isbn", isbn) );
                return null;
            });
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }


    /**
     *  Delete a FAVORITE relation
     * @param username
     * @param isbn
     */
    public boolean removeFavoriteBook(String username, String isbn)
    {
        try ( Session session = driver.session())
        {
            session.writeTransaction((TransactionWork<Void>) tx -> {
                tx.run( "MATCH (:User {username: $username})-[r:FAVORITES]->(:Book {isbn: $isbn}) DELETE r",
                        parameters( "username", username, "isbn", isbn) );
                return null;
            });
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
    }

    /***************************** ANALYTICS ******************************************/

    /**
     * This function is used to obtain the most Followed Users
     * @param howMany       How many to obtain
     * @return              List of the most followed Users
     */
    public List<String> mostFollowedUsers (int howMany)
    {
        List<String> usernames = new ArrayList<>();
        try(Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (u:User)<-[r:FOLLOWS]-(:User)\n" +
                                          "RETURN DISTINCT u.username AS Username,\n" +
                                          "COUNT(DISTINCT r) AS numFollower ORDER BY numFollower DESC\n" +
                                          "LIMIT $limit",
                        parameters( "limit", howMany));

                while(result.hasNext()){
                    Record r = result.next();
                    String user = r.get("Username").asString();
                    usernames.add(user);
                }
                return null;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return usernames;
    }

    /**
     * This function is used to obtain the most Liked Reading Lists
     * @param howMany       How many to obtain
     * @return              List of the most Liked Reading Lists
     */
    public List<String> mostLikedReadingLists (int howMany)
    {
        List<String> readingLists = new ArrayList<>();
        try(Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (:User)-[l:LIKES]->(rl:ReadingList)\n" +
                                          "RETURN rl.name AS ReadingList,\n" +
                                          "COUNT(DISTINCT l) AS numLikes ORDER BY numLikes DESC\n" +
                                          "LIMIT $limit",
                        parameters( "limit", howMany));

                while(result.hasNext()){
                    Record r = result.next();
                    String readingList = r.get("ReadingList").asString();
                    readingLists.add(readingList);
                }
                return null;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return readingLists;
    }


    /**
     * This function is used to obtain the most Favorited Books
     * @param howMany       How many to obtain
     * @return              List of the most Favorited Books
     */
    public List<Book> mostFavoritedBooks (int howMany)
    {
        List<Book> books = new ArrayList<>();
        try(Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (:User)-[f:FAVORITES]->(b: Book)\n" +
                                          "RETURN b.title AS title, b.author AS author, b.imageURL AS imageURL, b.isbn AS isbn,\n" +
                                          "COUNT(f) AS numFavorites ORDER BY numFavorites DESC\n" +
                                          "LIMIT $limit",
                        parameters( "limit", howMany));

                while(result.hasNext()){
                    Record r = result.next();
                    String title = r.get("title").asString();
                    String author = r.get("author").asString();
                    String imageURL = r.get("imageURL").asString();
                    String isbn = r.get("isbn").asString();
                    Book book = new Book(isbn,title,author,imageURL);
                    books.add(book);
                }
                return null;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return books;
    }

    /**
     * This function is used to suggest Users followed by users that the logged user follow, who is interested to the same categories
     * @param loggedUsername    Username of the logged User
     * @return                  List of the suggested Users
     */
    public List<String> suggestUsersByCommonInterest (String loggedUsername)
    {
        List<String> suggestedUsers = new ArrayList<>();
        try(Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (me:User {username: $username}) -[f: FOLLOWS]-> (u2) -[f2: FOLLOWS]-> (target: User)\n" +
                                          "WHERE NOT EXISTS ((me) -[: FOLLOWS]-> (target)) \n" +
                                          "AND (me) -[: IS_INTERESTED_TO]-> (: Category) <-[: IS_INTERESTED_TO]- (target)\n" +
                                          "RETURN DISTINCT target.username AS username",
                        parameters( "username", loggedUsername));

                while(result.hasNext()){
                    Record r = result.next();
                    String user = r.get("username").asString();
                    suggestedUsers.add(user);
                }
                return null;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return suggestedUsers;
    }



    /**
     * This function is used to get categories summary by likes of reading lists
     *  @param howMany       How many to obtain
     *  @return Map of categories with likes gathered from the reading lists
     */
    public Map<String,Integer> categoriesSummaryByReadingList (int howMany)
    {
        Map<String, Integer> summary = new HashMap<>();
        try(Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (: User) -[: LIKES]-> (rl: ReadingList) -[h: HAS]-> (: Book) -[be: BELONGS_TO]-> (c: Category)\n" +
                                          "RETURN c.name AS Category,\n" +
                                          "COUNT(be) AS numTop ORDER BY numTop DESC\n" +
                                          "LIMIT $limit",
                        parameters( "limit", howMany));

                while(result.hasNext()){
                    Record r = result.next();
                    String category = r.get("Category").asString();
                    Integer value = r.get("numTop").asInt();
                    summary.put(category,value);
                }
                return null;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return summary;
    }

    /**
     * This function is used to get categories summary by favorites books
     *  @return Map of categories with favorites gathered from books belonging to that category
     */
    public Map<String,Integer> categoriesSummaryByFavoriteBooks (int howMany)
    {
        Map<String, Integer> summary = new HashMap<>();
        try(Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (: User) -[: FAVORITES]-> (: Book) -[be: BELONGS_TO]-> (c: Category)\n" +
                                          "RETURN c.name AS Category,\n" +
                                          "COUNT(be) AS numFavorites ORDER BY numFavorites DESC\n" +
                                          "LIMIT $limit",
                        parameters( "limit", howMany));

                while(result.hasNext()){
                    Record r = result.next();
                    String category = r.get("Category").asString();
                    Integer value = r.get("numFavorites").asInt();
                    summary.put(category,value);
                }
                return null;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return summary;
    }

    /**
     * This function is used to get how many followers, follow you back
     *  @param loggedUsername    Username of the logged User
     *  @return Map with in "Followed" the number of user followed by the user, in "FollowBack" how many follow back the user
     */
    public Map<String,Integer> howManyFollowYouBack (String loggedUsername)
    {
        Map<String, Integer> map = new HashMap<>();
        try(Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (ua:User {username: $loggedusername })-[:FOLLOWS]->(ub:User)\n" +
                                          "OPTIONAL MATCH p = (ub)-[r:FOLLOWS]->(ua)\n" +
                                          "RETURN COUNT(p) AS FollowBack, COUNT(ua) AS Followed",
                        parameters( "loggedusername", loggedUsername));

                while(result.hasNext()){
                    Record r = result.next();
                    Integer Followed = r.get("Followed").asInt();
                    map.put("Followed",Followed);

                    Integer FollowBack = r.get("FollowBack").asInt();
                    map.put("FollowBack",FollowBack);
                }
                return null;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return map;
    }


    /**
     * This function is used suggest Books and it is based on different metrics:
     *  - books present in reading list in which your favorites books belong
     *  - books favorites by your follows
     *  - books belonging to the category that you are interested to
     *  - books belonging to reading lists liked by your follows
     * @param loggedUsername    Username of the logged User
     * @param howMany
     * @return                  List of the suggested Books
     */
    public List<Book> suggestBooks(String loggedUsername, int howMany)
    {
        List<Book> suggestedBooks= new ArrayList<>();
        try(Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (u:User{username: $username})-[i:IS_INTERESTED_TO]->(c:Category)<-[bt:BELONGS_TO]-(bf:Book)\n" +
                                          "WHERE NOT EXISTS((u)-[:FAVORITES]->(bf))\n" +
                                          "RETURN bf.title AS title, bf.author as author, bf.imageURL as imageURL, bf.isbn AS isbn\n" +
                                          "LIMIT $limit\n" +
                                          "UNION\n" +
                                          "MATCH (u:User{username: $username})-[f:FOLLOWS]->(ua:User)-[l:LIKES]->(r:ReadingList)-[o:HAS]->(bf:Book)\n" +
                                          "WHERE NOT EXISTS((u)-[:FAVORITES]->(bf))\n" +
                                          "RETURN bf.title AS title, bf.author as author, bf.imageURL as imageURL, bf.isbn AS isbn\n" +
                                          "LIMIT $limit\n" +
                                          "UNION\n" +
                                          "MATCH (u:User{username: $username})-[f:FOLLOWS]->(uf:User)-[fav:FAVORITES]->(bf:Book)\n" +
                                          "WHERE NOT EXISTS((u)-[:FAVORITES]->(bf))\n" +
                                          "RETURN bf.title AS title, bf.author as author, bf.imageURL as imageURL, bf.isbn AS isbn\n" +
                                          "LIMIT $limit\n" +
                                          "UNION\n" +
                                          "MATCH (u:User{username: $username})-[f:FAVORITES]->(b:Book)<-[h:HAS]-(r:ReadingList)-[h2:HAS]->(bf:Book)\n" +
                                          "WHERE NOT EXISTS((u)-[:FAVORITES]->(bf))\n" +
                                          "RETURN bf.title AS title, bf.author as author, bf.imageURL as imageURL, bf.isbn AS isbn\n" +
                                          "LIMIT $limit",
                        parameters( "username", loggedUsername, "limit", howMany));

                while(result.hasNext()){
                    Record r = result.next();
                    String title = r.get("title").asString();
                    String author = r.get("author").asString();
                    String imageURL = r.get("imageURL").asString();
                    String isbn = r.get("isbn").asString();
                    Book book = new Book(isbn,title,author,imageURL);
                    suggestedBooks.add(book);
                }
                return null;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return suggestedBooks;
    }




    /**
     * This function is used suggest Reading List and it is based on different metrics:
     *  - reading lists liked by your follows
     *  - reading lists liked by users followed by your follows
     * @param loggedUsername    Username of the logged User
     * @param howMany
     * @return                  List of the suggested Reading List
     */
    public List<String> suggestReadingLists(String loggedUsername, int howMany)
    {
        List<String> suggestedReadingList= new ArrayList<>();
        try(Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (u:User{username: $username})-[f:FOLLOWS]->(ua:User)-[l:LIKES]->(rl:ReadingList)\n" +
                                          "WHERE NOT EXISTS((u)-[:LIKES]->(rl))\n" +
                                          "RETURN rl.name AS name\n" +
                                          "LIMIT $limit\n" +
                                          "UNION \n" +
                                          "MATCH (u:User{username: $username})-[f:FOLLOWS]->(ub:User)-[f2:FOLLOW]->(uc:User)-[l:LIKES]->(rl:ReadingList)\n" +
                                          "WHERE NOT EXISTS((u)-[:FOLLOW]->(uc))\n" +
                                          "RETURN rl.name AS name\n" +
                                          "LIMIT $limit\n",
                        parameters( "username", loggedUsername, "limit", howMany));

                while(result.hasNext()){
                    Record r = result.next();
                    String name = r.get("name").asString();
                    suggestedReadingList.add(name);
                }
                return null;
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return suggestedReadingList;
    }

}
