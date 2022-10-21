# UPDATE READING LIST
# Input: CSV file with the reading list

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

i = 0

for index, row in tqdm.tqdm(df.iterrows(), total=df.shape[0]):

    j = 0
    
    username = row['username']
    reading_list = row['name']

    list_isbn = row['ISBN']
    list_title = row['title']
    list_imgURL = row['imageURL']
    list_author = row['author']

    # Add a new empty reading list
    collection_users.update_one({"username": username},
        {
            "$push": {"readingList": {
                        "name": reading_list,
                        "numLikes": 0,
                        "books": []
                    }}
        }
    )

    string_isbn = list_isbn[1:-1]
    string_title = list_title[1:-1]
    string_imgURL = list_imgURL[1:-1]
    string_author = list_author[1:-1]

    list_isbn = string_isbn.split(',')
    list_title = string_title.split(',')
    list_imgURL = string_imgURL.split(',')
    list_author = string_author.split(',')

    for (isbn,title,imgURL,author) in zip(list_isbn, list_title, list_imgURL, list_author): 

        if j == 0:
            isbn = isbn[1:-1]
            title = title[1:-1]
            imgURL = imgURL[1:-1]
            author = author[1:-1]
        else:
            isbn = isbn[2:-2]
            title = title[2:-2]
            imgURL = imgURL[2:-2]
            author = author[2:-2]  

        j = j + 1

        # Add a new book to a reading list of a certain user
        collection_users.update_one({"username": username, "readingList.name": reading_list},
            {
                "$push": {"readingList.$.books": {
                            "isbn": isbn,
                            "title": title,
                            "imageURL": imgURL,
                            "author": author
                        }}
            }
        )
