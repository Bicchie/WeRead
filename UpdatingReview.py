# UPDATE REVIEW
# Input: CSV file with review of the category
#inspiring code https://medium.com/nerd-for-tech/how-to-prepare-a-python-date-object-to-be-inserted-into-mongodb-and-run-queries-by-dates-and-range-bc0da03ea0b2

from datetime import datetime, timedelta
import pandas as pd
import pymongo
from pymongo import MongoClient
import random
import sys
import tqdm as tqdm

# take the name of the csv file containing the review of the category
file = sys.argv[1]

df = pd.read_csv(file)

client = MongoClient('mongodb://localhost:27017/')
db = client.WeRead
collection_users = db.users
collection_books = db.books
#  RANDOMIZE OLD REVIEWS
# datetime(year, month, day, hour, minute, second, microsecond)
#  CURRENT REVIEW
# datetime.today().replace(microsecond=0),

i = 0

for index, row in tqdm.tqdm(df.iterrows(), total=df.shape[0]):

	username = row['Username']
	reviewID = row['reviewID']
	title = row['Title']
	review_text = row['Review_Text']
	rating = row['Rating']
	isbn = row['ISBN']
	time = datetime(random.randint(2012, 2022),random.randint(1, 12),random.randint(1, 28),random.randint(1, 12))

	collection_users.update_one({"username": username},
	{
		"$push": {"reviews": {
					"reviewId": reviewID,
					"title": title,
					"text": review_text,
					"rating": rating,
					"time": time,
					"numLikes": 0,
					"likers": ""
				}},
		"$inc": {"numReviews": 1}		
	}
	)


	collection_books.update_one({'isbn': isbn},
		{
			"$push": {"reviews": {
						'reviewer': username,
						'title': title,
						'text': review_text,
						'rating': rating,
						'time': time,
						'numLikes': 0,
						'likers': []
					}}
		}
	)
