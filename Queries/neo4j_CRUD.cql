//CRUD NEO4J
//CREATE

// Create a user node -> DONE
// Create IS\_INTERESTED\_TO relation -> DONE
// Create a book node -> DONE
// Create a reading list node -> DONE
// Create a category node -> DONE
// Create HAS relation -> DONE
// Create FOLLOW relation -> DONE
// Create LIKE relation -> DONE
// Create FAVORITE relation -> DONE
// create BELONG relation -> DONE

//READ

// Retrieve the number of followed user
// Retrieve the number of user’s followers
// Retrieve the number of favorites received by a book
// Retrieve the number of likes received by reading list
// Check if a user follows another user
// Check if a user likes a reading list
// Check if a user likes a book

//UPDATE

// Update username

//DELETE

// Delete a User node and detach all its relations -> DONE
// Delete IS\_INTERESTED\_TO relation -> DONE
// Delete a reading list node -> DONE
// Delete FOLLOW relation -> DONE
// Delete LIKE relation
// Delete HAS relation
// Delete FAVORITE relation

//*********************************************** CREATE ********************************************************************************************

// Create a user node -------------------------------------------------------------------------------------------------------------------------------

CREATE (ee: User {username: <new_username>})

// --------------------------------------------------------------------------------------------------------------------------------------------------

// Create a book node -------------------------------------------------------------------------------------------------------------------------------

CREATE (ee: Book {title: <new_book_title>, isbn: <book_isbn>, author: <book_author>, imageURL: <book_image>})

// --------------------------------------------------------------------------------------------------------------------------------------------------

// Create a reading list node -----------------------------------------------------------------------------------------------------------------------

CREATE (ee: ReadingList {name: <readingList_name>})

// --------------------------------------------------------------------------------------------------------------------------------------------------

// Create a category node ---------------------------------------------------------------------------------------------------------------------------

CREATE (ee: Category {name: <category_name>})

// --------------------------------------------------------------------------------------------------------------------------------------------------

// Create FOLLOW relation ---------------------------------------------------------------------------------------------------------------------------

MATCH (u1: User) WHERE u1.username = <follower>
MATCH (u2: User) WHERE u2.username = <followed>
CREATE (u1) - [:FOLLOWS] -> (u2)

// --------------------------------------------------------------------------------------------------------------------------------------------------

// Create IS_INTERESTED_TO relation -----------------------------------------------------------------------------------------------------------------

MATCH (u: User) WHERE u.username = <logged_username>
MATCH (c: Category) WHERE c.name = <category>
CREATE (u) - [:IS_INTERESTED_TO] -> (c)

// --------------------------------------------------------------------------------------------------------------------------------------------------

// Create HAS relation ------------------------------------------------------------------------------------------------------------------------------

MATCH (rl: ReadingList) WHERE rl.name = <rl_name>
MATCH (b: Book) WHERE b.isbn = <book_isbn>
CREATE (rl) - [:HAS] -> (b)

// --------------------------------------------------------------------------------------------------------------------------------------------------

// Create LIKE relation -----------------------------------------------------------------------------------------------------------------------------

MATCH (u: User) WHERE u.username = <logged_username>
MATCH (rl: ReadingList) WHERE rl.name = <rl_name>
CREATE (u) - [:LIKES] -> (rl)

// --------------------------------------------------------------------------------------------------------------------------------------------------

// Create FAVORITE relation -------------------------------------------------------------------------------------------------------------------------

MATCH (u: User) WHERE u.username = <logged_username>
MATCH (b: Book) WHERE b.isbn = <book_isbn>
CREATE (u) - [:FAVORITES] -> (b)

// --------------------------------------------------------------------------------------------------------------------------------------------------

// Create BELONGS_TO relation -----------------------------------------------------------------------------------------------------------------------

MATCH (b: Book) WHERE b.isbn = <book_isbn>
MATCH (c: Category) WHERE c.name = <category>
CREATE (b) - [:BELONGS_TO] -> (c)

// --------------------------------------------------------------------------------------------------------------------------------------------------

//***************************************************************************************************************************************************

// ************************** READ ******************************************************************************************************************

//Retrieve the number of followed user

MATCH (:User {username: $username })-[r:FOLLOWS]->()
RETURN COUNT (r) AS numFollowed 

// Retrieve the number of user’s followers

MATCH (:User {username: $username })<-[r:FOLLOWS]-()
RETURN COUNT (r) AS numFollowers 

// Retrieve the number of favorites received by a book

MATCH (:Book {isbn: $isbn })<-[r:FAVORITES]-()
RETURN COUNT (r) AS numFavorites 

// Retrieve the number of likes received by reading list

MATCH (:ReadingList {id: $readinglistname })<-[r:LIKES]-()
RETURN COUNT (r) AS numLikes 

// Check if a user follows another user

MATCH (a:User{id: $usernameA })-[r:FOLLOWS]->(b:User{id: $usernameB })
RETURN COUNT (*)

// Check if a user likes a reading list

MATCH (a:User{id: $user })-[r:LIKES]->(b:ReadingList{id: $readinglistname })
RETURN COUNT (*)

// Check if a user favorites a book

MATCH (a:User{id: $username })-[r:FAVORITES]->(b:Book{isbn: $isbnbook })
RETURN COUNT (*)

// ***********************************************************************************************************************************************

// ************************** UPDATE *************************************************************************************************************

// Update username

MATCH (u:User {username: $oldusername })
SET u.username = $newusername ,  u.id = $newusername

//*********************************************** DELETE ********************************************************************************************

// Delete a User node and detach all its relations --------------------------------------------------------------------------------------------------

MATCH (u:User) WHERE u.username = $username DETACH DELETE u

// --------------------------------------------------------------------------------------------------------------------------------------------------

// Delete FOLLOWS relation --------------------------------------------------------------------------------------------------------------------------

MATCH (:User {username: <logged_username>})-[r:FOLLOWS]->(:User {username: <target_username>}) DELETE r

// --------------------------------------------------------------------------------------------------------------------------------------------------

// Delete IS_INTERESTED_TO relation -----------------------------------------------------------------------------------------------------------------

MATCH (:User {username: <logged_username>})-[r:IS_INTERESTED_TO]->(:Category {name: <target_category>}) DELETE r

// --------------------------------------------------------------------------------------------------------------------------------------------------

// Delete a reading list node -----------------------------------------------------------------------------------------------------------------------

MATCH (r:ReadingList {name: <readingList_name>}) DETACH DELETE r

// --------------------------------------------------------------------------------------------------------------------------------------------------

// Delete a LIKES relation --------------------------------------------------------------------------------------------------------------------------

MATCH (:User {username: <logged_username>})-[r:LIKES]->(:ReadingList {name: <target_rl>}) DELETE r

// --------------------------------------------------------------------------------------------------------------------------------------------------

// Delete a HAS relation ----------------------------------------------------------------------------------------------------------------------------

MATCH (:ReadingList {name: <target_rl>})-[r:HAS]->(:Book {isbn: <target_isbn_book>}) DELETE r

// --------------------------------------------------------------------------------------------------------------------------------------------------

// Delete a FAVORITE relation -----------------------------------------------------------------------------------------------------------------------

MATCH (:User {username: <logged_username>})-[r:FAVORITES]->(:Book {isbn: <target_isbn_book>}) DELETE r

// --------------------------------------------------------------------------------------------------------------------------------------------------

//***************************************************************************************************************************************************
