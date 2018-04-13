package main.java;


import java.util.Iterator;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;

//import com.mongodb.;


public class ConnectToDB {


    public static void main( String args[] ) {

        // Creating a Mongo client
//        MongoClient mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        MongoClient mongo = new MongoClient();

        // Creating Credentials
        System.out.println("Connected to the database successfully");

        // Accessing the database
        MongoDatabase database = mongo.getDatabase("cloud");
        //Creating a collection
//        database.createCollection("sampleCollection1");
        System.out.println("Collection created successfully");

        MongoCollection<Document> collection = database.getCollection("myCollection");

        System.out.println("Collection myCollection selected successfully");

        Document document = new Document("title", "MongoDB")
                .append("id", 1)
                .append("description", "database")
                .append("likes", 100)
                .append("url", "http://www.tutorialspoint.com/mongodb/")
                .append("by", "tutorials point");
        collection.insertOne(document);
        System.out.println("Document inserted successfully");

        // Retrieving a collection
        MongoCollection<Document> collection1 = database.getCollection("myCollection");
        System.out.println("Collection sampleCollection selected successfully");

        // Getting the iterable object
        FindIterable<Document> iterDoc = collection1.find();
        int i = 1;

        // Getting the iterator
        Iterator it = iterDoc.iterator();

        while (it.hasNext()) {
            System.out.println(it.next());
            i++;
        }


    }
}