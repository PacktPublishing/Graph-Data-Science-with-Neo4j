# Chapter 4: Using Graph Algorithms to Characterize a Graph Dataset

## Import all person's citizenship data

1. Copy `./data/person_citizenship.csv` to your Neo4j db import folder
2. Delete existing relationships
    ```cypher
    // first delete existing relationships
    MATCH ()-[r:IS_CITIZEN_OF]-()
    DELETE r
    ```
3. Import data:
    ```cypher
    // then load new ones
    LOAD CSV WITH HEADERS 
    FROM "file:///person_citizenship.csv" AS row
    MATCH (p:Person {name: row.person})
    MERGE (c:Country {name: row.country})
    CREATE (p)-[:IS_CITIZEN_OF]->(c)
    ```
