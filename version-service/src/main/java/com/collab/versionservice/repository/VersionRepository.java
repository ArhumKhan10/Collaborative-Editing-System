package com.collab.versionservice.repository;

import com.collab.versionservice.model.Version;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionRepository extends MongoRepository<Version, ObjectId> {
    
    List<Version> findByDocumentId(ObjectId documentId, Sort sort);
    
    List<Version> findByDocumentIdAndUserId(ObjectId documentId, ObjectId userId);
    
    long countByDocumentId(ObjectId documentId);
}
