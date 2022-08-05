// Load Graph A data
LOAD CSV FROM "https://raw.githubusercontent.com/PacktPublishing/Graph-Data-Science-with-Neo4J/main/Chapter03/data/graph_A.edgelist" AS line 
FIELDTERMINATOR " "
MERGE (a:NodeA {id: toInteger(line[0])})
MERGE (b:NodeA {id: toInteger(line[1])})
CREATE (a)-[:LINK]->(b)


// Create an isolated NodeA:
CREATE (:NodeA {id: 99999})


// Load Graph B data
LOAD CSV FROM "https://raw.githubusercontent.com/PacktPublishing/Graph-Data-Science-with-Neo4J/main/Chapter03/data/graph_B.edgelist" AS line 
FIELDTERMINATOR " "
MERGE (a:NodeB {id: toInteger(line[0])})
MERGE (b:NodeB {id: toInteger(line[1])})
CREATE (a)-[:LINK]->(b)


/*
CREATE (A:NodeC {id: "A"})
CREATE (B:NodeC {id: "B"})
CREATE (C:NodeC {id: "C"})
CREATE (D:NodeC {id: "D"})
CREATE (E:NodeC {id: "E"})

CREATE (A)-[:REL]->(B)
CREATE (A)-[:REL]->(C)
CREATE (A)-[:REL]->(D)
CREATE (B)-[:REL]->(D)
CREATE (B)-[:REL->(E)
CREATE (C)-[:REL→(D)
*/

