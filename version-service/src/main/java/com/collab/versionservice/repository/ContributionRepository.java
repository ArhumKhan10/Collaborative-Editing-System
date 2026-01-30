package com.collab.versionservice.repository;

import com.collab.versionservice.model.Contribution;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContributionRepository extends MongoRepository<Contribution, ObjectId> {
    
    List<Contribution> findByDocumentId(ObjectId documentId);
    
    Optional<Contribution> findByDocumentIdAndUserId(ObjectId documentId, ObjectId userId);
}
