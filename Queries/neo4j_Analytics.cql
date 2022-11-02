// ------------- Most Followed Users --------------------------------------------------------------------------------

MATCH (u:User)<-[r:FOLLOWS]-(:User)
RETURN DISTINCT u.username AS Username,
COUNT(DISTINCT r) AS numFollower ORDER BY numFollower DESC
LIMIT <limit>

//--------------------------------------------------------------------------------------------------------------------

//------------------ Most Liked Reading List -------------------------------------------------------------------------

MATCH (:User)-[l:LIKES]->(rl:ReadingList)
RETURN DISTINCT rl.name AS ReadingList,
COUNT(l) AS numLikes ORDER BY numLikes DESC
LIMIT <limit>



//------------------ Most Favorited Book -----------------------------------------------------------------------------

MATCH (:User)-[f:FAVORITES]->(b: Book)
RETURN b.title AS title, b.author AS author, b.imageURL AS imageURL, b.isbn AS isbn
COUNT(f) AS numFavorites ORDER BY numFavorites DESC
LIMIT <limit>

//--------------------------------------------------------------------------------------------------------------------

//--- Suggest Users followed by users that the logged user follow, who is interested to the same categories ---------- 

MATCH (me:User {username: <logged_username>}) -[: FOLLOW]-> () -[: FOLLOW]-> (target: User)
WHERE NOT EXISTS ((me) -[: FOLLOW]-> (target)) 
	AND (me) -[: IS_INTERESTED_TO]-> (: Category) <-[: IS_INTERESTED_TO]- (target)
	RETURN DISTINCT target

//--------------------------------------------------------------------------------------------------------------------

/ Get categories summary by likes of reading lists ------------------------------------------------

MATCH (: User) -[: LIKES]-> (rl: ReadingList) -[h: HAS]-> (: Book) -[be: BELONGS_TO]-> (c: Category)
RETURN c.name AS Category,
COUNT(be) AS numTop ORDER BY numTop DESC
LIMIT <limit>

//--------------------------------------------------------------------------------------------------------------------

//------------------ Get categories summary by favorites books -------------------------------------------------------

MATCH (: User) -[: FAVORITES]-> (: Book) -[be: BELONGS_TO]-> (c: Category)
RETURN c.name AS Category,
COUNT(be) AS numFavorites ORDER BY numFavorites DESC
LIMIT <limit>

//--------------------------------------------------------------------------------------------------------------------

//------------------ Check how many followers, follow you back -------------------------------------------------------

MATCH (ua:User {username: <username> })-[:FOLLOWS]->(ub:User)
OPTIONAL MATCH p = (ub)-[r:FOLLOWS]->(ua)
RETURN COUNT(p), COUNT(ua)

//--------------------------------------------------------------------------------------------------------------------

//------------------ Suggested Book ----------------------------------------------------------------------------------


MATCH (u:User{username: <username>})-[i:IS_INTERESTED_TO]->(c:Category)<-[bt:BELONGS_TO]-(bf:Book)
WHERE NOT EXISTS((u)-[:FAVORITES]->(bf))
RETURN bf
LIMIT <limit>
UNION
MATCH (u:User{username: <username>})-[f:FOLLOWS]->(ua:User)-[l:LIKES]->(r:ReadingList)-[o:HAS]->(bf:Book)
WHERE NOT EXISTS((u)-[:FAVORITES]->(bf))
RETURN bf
LIMIT <limit>
UNION
MATCH (u:User{username: <username>})-[f:FOLLOWS]->(uf:User)-[fav:FAVORITES]->(bf:Book)
WHERE NOT EXISTS((u)-[:FAVORITES]->(bf))
RETURN bf
LIMIT <limit>
UNION
MATCH (u:User{username: <username>})-[f:FAVORITES]->(b:Book)<-[h:HAS]-(r:ReadingList)-[h2:HAS]->(bf:Book)
WHERE NOT EXISTS((u)-[:FAVORITES]->(bf))
RETURN u,f,b,h,r,bf
LIMIT <limit>

//--------------------------------------------------------------------------------------------------------------------

//------------------ Suggested Reading List --------------------------------------------------------------------------

MATCH (u:User{username:<username>})-[f:FOLLOW]->(ua:User)-[l:LIKES]->(rl:ReadingList)
WHERE NOT EXISTS((u)-[:LIKES]->(rl))
RETURN rl
LIMIT <limit>
UNION 
MATCH (u:User{username:<username>})-[f:FOLLOW]->(ub:User)-[f2:FOLLOW]->(uc:User)-[l:LIKES]->(rl:ReadingList)
WHERE NOT EXISTS((u)-[:FOLLOW]->(uc))
RETURN rl
LIMIT <limit>





