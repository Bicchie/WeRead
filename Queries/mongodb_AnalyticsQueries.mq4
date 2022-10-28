/*
QUERIES TO BE IMPLEMENTED:

- List of books with greatest average rating
- List of usernames with best/worst average rating assigned in their reviews
- Usernames that have written more reviews than the others
- Reviews that have received greatest number of likes
- Reading Lists liked by highest number of users
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

// ------------- Reviews that have received greatest number of likes ------------------------------------------------------------------
//given x the number of reviews we want to show

db.books.aggregate([
  {$match: {reviews: {$ne: []}}},
  {$unwind: "$reviews"},
  {$project: {_id: 0, bookTitle: "$title", reviewer: "$reviews.reviewer", text: "$reviews.text", numLikes: "$reviews.numLikes"}},
  {$sort: {numLikes: -1}},
  {$limit: <x>}
  ])

// ------------------------------------------------------------------------------------------------------------------------------------

// ------------- Reading Lists liked by highest number of users -----------------------------------------------------------------------
//given x the number of reading lists we want to show

db.users.aggregate([
  {$match: {readingList: {$ne: []}}},
  {$unwind: "$readingList"},
  {$project: {_id: 0, name: "$readingList.name", numLikes: "$readingList.numLikes"}},
  {$sort: {numLikes: -1}},
  {$limit: <x>}
  ])

// ------------------------------------------------------------------------------------------------------------------------------------

// ------------- Average length in pages of Books given the Category per year ---------------------------------------------------------
//given x the category

db.books.aggregate([
	{ $match: {category: <x>}},
	{ $project : { publicationYear: 1, numPages: 1}},
	{ $group: {
            _id: '$publicationYear', 
            avgLength: {
                '$avg': '$numPages'
            }
        }},
  	{ $sort: {_id:-1} }
])

// ------------------------------------------------------------------------------------------------------------------------------------

// ------------- How many books are presents per language -----------------------------------------------------------------------------

db.books.aggregate([
    	{$group : {_id:"$language", count:{$sum:1}}},
    	{$sort: {count: -1}}
])

// ------------------------------------------------------------------------------------------------------------------------------------

// ------------- How many books are published per year by a certain publisher ---------------------------------------------------------
given x the publisher
db.books.aggregate([
	{ $match: {publisher: <x>}},
	{ $project: { publicationYear:1}},
	{ $group: {
            _id: '$publicationYear', 
            count: {
                $sum: 1
            }
    	}},
  	{ $sort: {_id:-1}}
])

// ------------------------------------------------------------------------------------------------------------------------------------

// ------------- List of authors with greatest average rating of their books ----------------------------------------------------------

db.books.aggregate([
  { $match: {reviews: {$ne: []}}},
  { $project: {author:1, reviews:1 }},
  { $unwind : "$reviews" },
	{ $group: {
          _id: '$author', 
          avg_rat: {
            $avg: '$reviews.rating'
          }
  	}
  },
  { $sort: { avg_rat : -1 }}
]
