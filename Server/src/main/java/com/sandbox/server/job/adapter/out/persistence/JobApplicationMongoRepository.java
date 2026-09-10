package com.sandbox.server.job.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface JobApplicationMongoRepository extends MongoRepository<JobApplicationMongoDocument, String> {
}
