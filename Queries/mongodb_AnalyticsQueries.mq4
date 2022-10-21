/*
QUERIES TO BE IMPLEMENTED:

- List of books with greatest average rating -> DONE
- List of usernames with best/worst average rating assigned in their reviews (i recensitori più buoni e i più cattivi) -> DONE
- Usernames that have written more reviews than the others -> DONE
- Reviews that have received greatest number of likes
- Average length in pages per Category per Category per year
- How many books are presents per language
- How many books are published per year by a certain publisher
- List of authors with greatest average rating of their books 
*/

// ------------- List of books with greatest average rating ---------------------------------------------------------------------------
//given x the number of books we want to show
//Elimino dalla query risultato tutti quei libri che non hanno review 

db.books.aggregate([
    {$match: {reviews: {$ne: []}}},
    {$project: {_id: 0, title: 1, author: 1, imageURL: 1, averageRating: {$avg: "$reviews.rating"}}},
    {$sort: {averageRating: 1}},
    {$limit: <x>}
  ])

//ESEMPIO CON DUE LIBRI
db.books.aggregate([
    {$match: {reviews: {$ne: []}}},
    {$project: {_id: 0, title: 1, author: 1, imageURL: 1, averageRating: {$avg: "$reviews.rating"}}},
    {$sort: {averageRating: -1}},
    {$limit: 2}
  ])

// ------------------------------------------------------------------------------------------------------------------------------------

// ------------- List of usernames with best/worst average rating assigned in their reviews -------------------------------------------
//given x the number of users we want to show
//BEST => {sort: -1}, WORST => {sort: 1}

db.users.aggregate([
	{$match: {reviews: {$ne: []}}},
	{$project: {_id: 0, username: 1, averageRating: {$avg: "$reviews.rating"}}},
	{$sort: {averageRating: -1}},
	{$limit: <x>}
])

//ESEMPIO
db.users.aggregate([
	{$match: {reviews: {$ne: []}}},
	{$project: {_id: 0, username: 1, averageRating: {$avg: "$reviews.rating"}}},
	{$sort: {averageRating: -1}},
	{$limit: 2}
])

// ------------------------------------------------------------------------------------------------------------------------------------

// ------------- Usernames that have written more reviews than the others -------------------------------------------------------------
//given x the number of users we want to show

db.users.aggregate([
	{$match: {reviews: {$ne: []}}},
	{$project: {_id: 0, username: 1, numReviews: {$size: "$reviews"}}},
	{$sort: {numReviews: -1}},
	{$limit: <x>}
])

// ------------------------------------------------------------------------------------------------------------------------------------

// ------------- Reviews that have received greatest number of likes (DA FINIRE) ------------------------------------------------------------------
//given x the number of reviews we want to show

db.books.aggregate([
	{$match: {reviews: {$ne: []}}},
	{$project: {_id: 0, "reviews.$."},
	{$sort: {numReviews: -1}},
	{$limit: <x>}
])

// ------------------------------------------------------------------------------------------------------------------------------------