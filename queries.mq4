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
- Update username and/or password of a User
- Add a book to the favourite books list of a user -> DONE
- Remove a book to the favourite books list of a user -> DONE
- Add a new reading list -> DONE
- Add a new book to a reading list of a certain user
- Remove a book from a reading list of a certain user
- Add a new review to a book -> DONE

Delete
- Delete a User
*/

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
//Add a new review to a book's document
db.books.updateOne({isbn: <loaded_book_isbn>},
	{
		$push: {Reviews: {
					reviewId: <logged_username> + <loaded_book_isbn>,
					title: <loaded_book_title>,
					text: <review_text>,
					rating: <review_rating>,
					time: <now_timestamp>,
					numLikes: 0,
					likers: []
				}}
	}
)

//Add a new review to a book to the user's document
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
				}}
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

//idea per aggiungere libro a reading list
db.inventory.updateOne({nome: "giorgio"}, {$push:{"vettore.0.j":{r:5, ciao: "cia"}}})
// ------------------------------------------------------------------------------------------