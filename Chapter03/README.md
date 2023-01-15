# Chapter 3

## Import database

The Netflix graph was created in chapter 2. In order to import it if you have not followed along Chapter 2, here is the recipe:

1. Clone this repository
2. Open the Neo4j Desktop application
3. Click **Add | File**

![](images/neo4j_desktop_add_file.png)

4. Find the file `netflix_endCh2.dump` on your local filesystem and open it
5. Click the `...` next to the file **"Open"** button and click **"Create new DBMS from dump"**

![](images/neo4j_desktop_create_new_dbms_from_dump.png)

6. Fill in the required information (db name and password). Select Neo4j version **5.3.0** (the version the dump from created from)

![](images/neo4j_desktop_new_dbms_config.png)

7. Click **"Create"**
8. Note: you need to **re-install the APOC plugin**.

