/*
QUERIES TO BE IMPLEMENTED:
Create
- Create a User \\ -> DONE
- Create a Book \\ -> DONE

Read
- Get User information by Username \\ -> DONE
- Check uniqueness of a username -> DONE
- Check username and password of a certain User \\ -> DONE
- Get the reading lists created by a certain user \\ -> DONE
- Get the favourite books list of a certain user \\ -> DONE
- Get Book information by book's isbn \\ -> DONE
- Get the reviews written about a certain book (ordinato in ordine decrescente per timestamp o per rating) \\ -> DONE
- Get a book list given a book title -> DONE
- Get a book list given a book category -> DONE
- Get a book list given the name of an author -> DONE
- Get a book list given the year of publication -> DONE
- Get a book list given the publisher -> DONE

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
		reviews: [],
		readingList: []
	}
)

//Create a Book
db.books.insertOne(
	{
		isbn : <new_book_isbn>,
		title : <new_book_title>,
		language : <new_book_language>,
		category : <new_book_category>,
		publisher : <new_book_publisher>,
		description : <new_book_description>,
		numPages : <new_book_numPages>,
		imageURL : <new_book_image>,
		author : <new_book_author>,
		publicationYear : <new_book_pubYear>,
		reviews : []
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
		isAdministrator: true,
		favourite: [],
		reviews: [],
		readingList: []
	}
)

// ***************************************************************************************************************************************************************************************

// ************************** READ *******************************************************************************************************************************************************

//Get (all) User information by Username. !! The password must not be returned, otherwise everyone could see other people's passwords !!
db.users.find({username: <searched_username>}, {_id: 0, password: 0})

//Check uniqueness of a username
db.users.countDocuments({username: <inserted_username>})

//Check username and password of a certain User (it should return 1 or 0)
db.users.countDocuments({username: <inserted_username>, password: <inserted_password>})

//Get the reading lists created by a certain user, ordered by the number of received likes
db.users.aggregate([
	{$match: {username: <searched_usename>}},
	{
		$project: {
			_id: 0,
			result:{
				$sortArray: {input: "$readingList", sortBy: {numLikes: -1}}
			}
		}
	}
])

//Get the favourite books list of a certain user
db.users.find({username: <searched_username>}, {_id: 0, favourite: 1})

//Get Book information by book's title. This query is executed when we are showing information related to one and only book, so we need to use the isbn
db.books.find({isbn: <book_isbn>}, {_id: 0})

//Get the reviews written about a certain book (ordered by rating [-1 => DESCENDENT, 1 => ASCENDENT])
db.books.aggregate([
	{$match: {isbn: <loaded_book_isbn>}},
	{
		$project: {
			_id: 0,
			result:{
				$sortArray: {input: "$reviews", sortBy: {rating: -1}}
			}
		}
	}
])

//Get a book list given a book title. The <inserted_title> must not be substituted with something between double quotes, see the example
//this query finds all the books that contains the substring <inserted_title> in their titles
//dato che è pensato per le ricerche, ritorno informazioni per fare un elenco di libri, quindi quelle fondamentali
db.books.find({title: /<inserted_title>/i}, {_id: 0, title: 1, author: 1, category: 1, imageURL: 1})

// Get a book list given a book category
db.books.find({category: /<inserted_category>/i}, {_id: 0, title: 1, author: 1, category: 1, imageURL: 1})

// Get a book list given the name of an author
db.books.find({author: /<inserted_author>/i}, {_id: 0, title: 1, author: 1, category: 1, imageURL: 1})

// Get a book list given the year of publication
db.books.find({publicationYear: /<inserted_year>/i}, {_id: 0, title: 1, author: 1, category: 1, imageURL: 1})

// Get a book list given the publisher
db.books.find({publisher: /<inserted_publisher>/i}, {_id: 0, title: 1, author: 1, category: 1, imageURL: 1})

//ESEMPIO CHECK USERNAME E PASSWORD
db.users.countDocuments({username: "matteGuido", password: "password"})

//ESEMPIO FIND READING LISTS DI UN UTENTE
db.users.aggregate([
	{$match: {username: "Mark_Chang"}},
	{
		$project: {
			_id: 0,
			result:{
				$sortArray: {input: "$readingList", sortBy: {numLikes: -1}}
			}
		}
	}
])
db.users.find({username: "matteGuido"}, {_id: 0, readingList: 1})

//ESEMPIO REVIEW ORDINATE PER TIMESTAMP
db.books.aggregate([
	{$match: {isbn: "9788864116433"}},
	{
		$project: {
			_id: 0,
			result:{
				$sortArray: {input: "$reviews", sortBy: {rating: -1}}
			}
		}
	}
])

//ESEMPIO LISTA DI LIBRI
db.books.find({title: /sto/i}, {_id: 0, title: 1, author: 1, category: 1, imageURL: 1})

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
		$push: {reviews: {
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
		$pull: {reviews: {
					reviewId: <logged_username> + <loaded_book_isbn>
				}},
		$inc: {numReviews: -1}
	}
)

//Add a like to the review about a certain book written by another user (user document)
db.users.updateOne({username: <logged_username>, "reviews.reviewId": <selectedReview_reviewId>},
	{
		$inc: {"reviews.$.numLikes": 1},
		$push: {"reviews.$.likers": <logged_username>}
	}
)

//Remove a like to the review about a certain book written by another user (user document)
db.users.updateOne({username: <logged_username>, "reviews.reviewId": <selectedReview_reviewId>},
	{
		$inc: {"reviews.$.numLikes": -1},
		$pull: {"reviews.$.likers": <logged_username>}
	}
)

//ESEMPIO AGGIUNTA DI UNA REVIEW
db.books.updateOne({isbn: "9788838920936"},
	{
		$push: {reviews: {
					reviewer: "matteGuido",
					text: "Libro osceno",
					rating: 1,
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
		$push: {readingList: {
					name: <new_readingList_name>,
					numLikes: 0,
					books: []
				}}
	}
)

//Add a new book to a reading list of a certain user
db.users.updateOne({username: <logged_username>, "readingList.name": <readingList_name>},
	{
		$push: {"readingList.$.books": {
					isbn : <loaded_book_isbn>,
					title : <loaded_book_title>,
					imageURL : <loaded_book_image>,
					author : <loaded_book_author>
				}}
	}
)

//Remove a book from a reading list of a certain user
db.users.updateOne({username: <logged_user>, "readingList.name": <readingList_name>},
	{
		$pull: {"readingList.$.books": {
					isbn: <loaded_book_isbn>
				}}
	}
)

//Remove a reading list (elimina la reading list anche se questa contiene libri!!)
db.users.updateOne({username: <logged_user>},
	{
		$pull: {readingList: {
					name: <readingList_name>
				}}
	}
)

//Add a like to a reading list of a certain user
db.users.updateOne({username: <username_who_created_readingList>, "readingList.name": <readingList_name>},
	{
		$inc: {"readingList.$.numLikes": 1}
	}
)

//Remove a like to a reading list of a certain user
db.users.updateOne({username: <username_who_created_readingList>, "readingList.name": <readingList_name>},
	{
		$inc: {"readingList.$.numLikes": -1}
	}
)

//INSERIMENTO LIBRO IN READING LIST ESEMPIO
db.users.updateOne({username: "Mark_Chang", "readingList.name": "nuovaRL"},
	{
		$push: {"readingList.$.books": {
					isbn : "9788864116433",
					title : "Daje",
					imageURL : "https://images.gr-assets.com/books/1380976410m/18628480.jpg",
					author : "John Williams"
				}}
	}
)
//RIMOZIONE LIBRO IN READING LIST ESEMPIO
db.users.updateOne({username: "Mark_Chang", "readingList.name": "nuovaRL"},
	{
		$pull: {"readingList.$.books": {
					isbn: "9788864116433"
				}}
	}
)
//NUOVA READING LIST VUOTA ESEMPIO
db.users.updateOne({username: "Mark_Chang"},
	{
		$push: {readingList: {
					name: "nuovaRL",
					numLikes: 0,
					books: []
				}}
	}
)
//ELIMINARE READING LIST
db.users.updateOne({username: "Mark_Chang"},
	{
		$pull: {readingList: {
					name: "nuovaRL"
				}}
	}
)

//ESEMPIO AGGIUNTA LIKE A UNA READING LIST
db.users.updateOne({username: "Mark_Chang", "readingList.name": "nuovaRL"},
	{
		$inc: {"readingList.$.numLikes": 1}
	}
)

//ESEMPIO RIMOZIONE LIKE A UNA READING LIST
db.users.updateOne({username: "Mark_Chang", "readingList.name": "nuovaRL"},
	{
		$inc: {"readingList.$.numLikes": -1}
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

//Delete a user
db.users.deleteOne({username: <logged_username>})

// ***************************************************************************************************************************************************************************************