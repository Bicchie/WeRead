package it.unipi.dii.lsdb.weread.persistence;

import it.unipi.dii.lsdb.weread.utils.ConfigurationParameters;
import it.unipi.dii.lsdb.weread.utils.Utils;
import it.unipi.dii.lsdb.weread.model.User;
import it.unipi.dii.lsdb.weread.model.Book;
import it.unipi.dii.lsdb.weread.model.Review;
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

}
