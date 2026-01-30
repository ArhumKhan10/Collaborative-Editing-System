package com.collab.documentservice.repository;

import com.collab.documentservice.model.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends MongoRepository<Document, ObjectId> {
    
    List<Document> findByOwnerId(ObjectId ownerId);
    
    @Query("{'collaborators.userId': ?0}")
    List<Document> findByCollaboratorUserId(ObjectId userId);
    
    @Query("{'$or': [{'ownerId': ?0}, {'collaborators.userId': ?0}]}")
    List<Document> findAllAccessibleByUserId(ObjectId userId);
}
