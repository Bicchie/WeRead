/*
QUERIES TO BE IMPLEMENTED:
Create
- Create a User \\
- Create a Book \\

Read
- Get User information by Username \\
- Check username and password of a certain User \\
- Get the reading list created by a certain user \\
- Get the favourite books list of a certain user \\
- Get Book information by book's title \\
- Get the reviews written about a certain book (ordinato in ordine decrescente per timestamp o per rating) \\
- Get a book list given a book category
- Get a book list given the name of an author
- Get a book list given the year of publication
- Get a book list given the publisher

(Immaginiamoci i criteri di ricerca : nome autore, titolo, anno pubblicazione, categoria, casa editrice


Update
- Update password of a User -> DONE
- Add a book to the favourite books list of a user -> DONE
- Remove a book to the favourite books list of a user -> DONE
- Add a new reading list -> DONE
- Remove a reading list -> DONE
- Add a new book to a reading list of a certain user -> DONE
- Remove a book from a reading list of a certain user -> DONE
- Add a new review to a book -> DONE
- Add a like to a reading list of a certain user -> DONE
- Add a like to the review about a certain book written by another user -> DONE

Delete
- Delete a User
*/

// ************************** CREATE *****************************************************************************************************************************************************

//Create a new user
db.users.insertOne(
	{
		username: <new_username>,
		name: <name>,
		surname: <surname>,
		email: <new_user_mail>,
		numReviews: 0,
		password: <new_user_password>,
		isAdministrator: <is_administrator?>,
		favourite: [],
		Reviews: [],
		ReadingList: []
	}
)

//ESEMPIO CREAZIONE UTENTE
db.users.insertOne(
	{
		username: "matteGuido",
		name: 'Matteo',
		surname: 'Guidotti',
		email: 'matteoguidotti@gmail.com',
		numReviews: 0,
		password: 'password',
		isAdministrator: false,
		favourite: [],
		Reviews: [],
		ReadingList: []
	}
)

// ***************************************************************************************************************************************************************************************

// ************************** READ *****************************************************************************************************************************************************
// ***************************************************************************************************************************************************************************************

// ************************** UPDATE *****************************************************************************************************************************************************

// -------------------------- FAVOURITE BOOKS -----------------------------------------------
//Add a book to the favourite books list of a user
db.users.updateOne({username: <logged_user>},
	{
		$push: {favourite: {
					isbn: <loaded_book_isbn>,
					title: <loaded_book_title>,
					imageURL: <loaded_book_image>,
					author: <loaded_book_author>
				}}
	}
)

//Remove a book to the favourite books list of a user
db.users.updateOne({username: <logged_user>},
	{
		$pull: {favourite: {
					isbn: <loaded_book_isbn>
				}}
	}
)
// ------------------------------------------------------------------------------------------

// -------------------------- REVIEWS -------------------------------------------------------
//Add a new review (book's document)
db.books.updateOne({isbn: <loaded_book_isbn>},
	{
		$push: {reviews: {
					reviewer: <logged_username>,
					title: <loaded_book_title>,
					text: <review_text>,
					rating: <review_rating>,
					time: <now_timestamp>,
					numLikes: 0,
					likers: []
				}}
	}
)

//Remove a review (book's document)
db.books.updateOne({isbn: <loaded_book_isbn>},
	{
		$pull: {reviews: {
					reviewer: <logged_username>
				}}
	}
)

//Add a like to the review about a certain book written by another user (book document)
db.books.updateOne({isbn: <loaded_book_isbn>, "reviews.reviewer": <selected_review_reviewer_username>},
	{
		$inc: {"reviews.$.numLikes": 1},
		$push: {"reviews.$.likers": <logged_username>}
	}
)

//Remove a like to the review about a certain book written by another user (book document)
db.books.updateOne({isbn: <loaded_book_isbn>, "reviews.reviewer": <selected_review_reviewer_username>},
	{
		$inc: {"reviews.$.numLikes": -1},
		$pull: {"reviews.$.likers": <logged_username>}
	}
)

//Add a new review to a book (user's document)
db.users.updateOne({username: <logged_username>},
	{
		$push: {Reviews: {
					reviewId: <logged_username> + <loaded_book_isbn>,
					title: <loaded_book_title>,
					text: <review_text>,
					rating: <review_rating>,
					time: <now_timestamp>,
					numLikes: 0,
					likers: []
				}},
		$inc: {numReviews: 1}
	}
)

//Remove a review (user's document)
db.users.updateOne({username: <logged_username>},
	{
		$pull: {Reviews: {
					reviewId: <logged_username> + <loaded_book_isbn>
				}},
		$inc: {numReviews: -1}
	}
)

//Add a like to the review about a certain book written by another user (user document)
db.users.updateOne({username: <logged_username>, "Reviews.reviewId": <selectedReview_reviewId>},
	{
		$inc: {"Reviews.$.numLikes": 1},
		$push: {"Reviews.$.likers": <logged_username>}
	}
)

//Remove a like to the review about a certain book written by another user (user document)
db.users.updateOne({username: <logged_username>, "Reviews.reviewId": <selectedReview_reviewId>},
	{
		$inc: {"Reviews.$.numLikes": -1},
		$pull: {"Reviews.$.likers": <logged_username>}
	}
)

//ESEMPIO AGGIUNTA DI UNA REVIEW
db.books.updateOne({isbn: "9788864116433"},
	{
		$push: {reviews: {
					reviewer: "Mark_Chang",
					title: "Stoner",
					text: "Gran libro!",
					rating: 4,
					time: "",
					numLikes: 0,
					likers: []
				}}
	}
)

//ESEMPIO RIMOZIONE DI UNA REVIEW
db.books.updateOne({isbn: "9788864116433"},
	{
		$pull: {reviews: {
					reviewer: "Mark_Chang"
				}}
	}
)

//ESEMPIO AGGIUNTA LIKE AD UNA REVIEW
db.books.updateOne({isbn: "9788864116433", "reviews.reviewer": "Mark_Chang"},
	{
		$inc: {"reviews.$.numLikes": 1},
		$push: {"reviews.$.likers": "Franco"}
	}
)

//ESEMPIO RIMOZIONE LIKE AD UNA REVIEW
db.books.updateOne({isbn: "9788864116433", "reviews.reviewer": "Mark_Chang"},
	{
		$inc: {"reviews.$.numLikes": -1},
		$pull: {"reviews.$.likers": "Franco"}
	}
)

//-------------------------------------------------------------------------------------------

// -------------------------- READING LISTS -------------------------------------------------
//Add a new empty reading list
db.users.updateOne({username: <logged_username>},
	{
		$push: {ReadingList: {
					name: <new_readingList_name>,
					numLikes: 0,
					books: []
				}}
	}
)

//Add a new book to a reading list of a certain user
db.users.updateOne({username: <logged_username>, "ReadingList.name": <readingList_name>},
	{
		$push: {"ReadingList.$.books": {
					isbn : <loaded_book_isbn>,
					title : <loaded_book_title>,
					imageURL : <loaded_book_image>,
					author : <loaded_book_author>
				}}
	}
)

//Remove a book from a reading list of a certain user
db.users.updateOne({username: <logged_user>, "ReadingList.name": <readingList_name>},
	{
		$pull: {"ReadingList.$.books": {
					isbn: <loaded_book_isbn>
				}}
	}
)

//Remove a reading list (elimina la reading list anche se questa contiene libri!!)
db.users.updateOne({username: <logged_user>},
	{
		$pull: {ReadingList: {
					name: <readingList_name>
				}}
	}
)

//Add a like to a reading list of a certain user
db.users.updateOne({username: <username_who_created_readingList>, "ReadingList.name": <readingList_name>},
	{
		$inc: {"ReadingList.$.numLikes": 1}
	}
)

//Remove a like to a reading list of a certain user
db.users.updateOne({username: <username_who_created_readingList>, "ReadingList.name": <readingList_name>},
	{
		$inc: {"ReadingList.$.numLikes": -1}
	}
)

//INSERIMENTO LIBRO IN READING LIST ESEMPIO
db.users.updateOne({username: "Mark_Chang", "ReadingList.name": "nuovaRL"},
	{
		$push: {"ReadingList.$.books": {
					isbn : "9788864116433",
					title : "Daje",
					imageURL : "https://images.gr-assets.com/books/1380976410m/18628480.jpg",
					author : "John Williams"
				}}
	}
)
//RIMOZIONE LIBRO IN READING LIST ESEMPIO
db.users.updateOne({username: "Mark_Chang", "ReadingList.name": "nuovaRL"},
	{
		$pull: {"ReadingList.$.books": {
					isbn: "9788864116433"
				}}
	}
)
//NUOVA READING LIST VUOTA ESEMPIO
db.users.updateOne({username: "Mark_Chang"},
	{
		$push: {ReadingList: {
					name: "nuovaRL",
					numLikes: 0,
					books: []
				}}
	}
)
//ELIMINARE READING LIST
db.users.updateOne({username: "Mark_Chang"},
	{
		$pull: {ReadingList: {
					name: "nuovaRL"
				}}
	}
)

//ESEMPIO AGGIUNTA LIKE A UNA READING LIST
db.users.updateOne({username: "Mark_Chang", "ReadingList.name": "nuovaRL"},
	{
		$inc: {"ReadingList.$.numLikes": 1}
	}
)

//ESEMPIO RIMOZIONE LIKE A UNA READING LIST
db.users.updateOne({username: "Mark_Chang", "ReadingList.name": "nuovaRL"},
	{
		$inc: {"ReadingList.$.numLikes": -1}
	}
)
// -------------------------------------------------------------------------------------------

// -------------------------- USERS' DATA UPDATE ---------------------------------------------

//Update password of a User
db.users.updateOne({username: <logged_username>},
	{
		$set: {password: <new_password>}
	}
)

//ESEMPIO CAMBIO PASSWORD UTENTE
db.users.updateOne({username: "Mark_Chang"},
	{
		$set: {password: "new_password"}
	}
)

// ------------------------------------------------------------------------------------------

// ***************************************************************************************************************************************************************************************

// ************************** DELETE *****************************************************************************************************************************************************
// ***************************************************************************************************************************************************************************************