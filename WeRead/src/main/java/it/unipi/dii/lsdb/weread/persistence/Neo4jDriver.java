package it.unipi.dii.lsdb.weread.persistence;

import it.unipi.dii.lsdb.weread.utils.ConfigurationParameters;
import it.unipi.dii.lsdb.weread.utils.Utils;
import it.unipi.dii.lsdb.weread.model.User;
import it.unipi.dii.lsdb.weread.model.Book;
import it.unipi.dii.lsdb.weread.model.ReadingList;
import org.neo4j.driver.*;

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




}
