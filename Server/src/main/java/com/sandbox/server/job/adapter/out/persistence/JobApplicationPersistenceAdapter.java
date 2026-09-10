package com.sandbox.server.job.adapter.out.persistence;

import com.sandbox.server.job.adapter.out.persistence.mapper.JobApplicationPersistenceMapper;
import com.sandbox.server.job.application.port.out.SaveJobApplicationPort;
import com.sandbox.server.job.domain.JobApplication;
import com.sandbox.server.job.domain.JobApplicationId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
class JobApplicationPersistenceAdapter implements SaveJobApplicationPort {

    private final JobApplicationMongoRepository mongoRepository;
    private final JobApplicationPersistenceMapper mapper;

    @Override
    public JobApplication save(JobApplication jobApplication) {
        return mapper.toDomain(mongoRepository.save(mapper.toDocument(jobApplication)));
    }

    @Override
    public Optional<JobApplication> findById(JobApplicationId id) {
        return mongoRepository.findById(id.value().toString()).map(mapper::toDomain);
    }

    @Override
    public Page<JobApplication> findAll(Pageable pageable) {
        return mongoRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public void deleteById(JobApplicationId id) {
        mongoRepository.deleteById(id.value().toString());
    }
}
