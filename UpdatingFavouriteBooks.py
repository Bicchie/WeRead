# UPDATE FAVOURITE BOOKS
# Input: CSV file with the favourite books of the users

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

for index, row in tqdm.tqdm(df.iterrows(), total=df.shape[0]):

    isbn = row['ISBN']
    username = row['username']
    title = row['title']
    imgURL = row['imageURL']
    author = row['author']

    collection_users.update_one({"username": username},
    {
        "$push": {"favourite": {
                    "isbn": isbn,
                    "title": title,
                    "imageURL": imgURL,
                    "author": author,
                }}	
    }
    )
