package com.sandbox.server.job.adapter.in.web;

import com.sandbox.server.job.adapter.in.web.dto.CreateJobApplicationRequest;
import com.sandbox.server.job.adapter.in.web.dto.JobApplicationDto;
import com.sandbox.server.job.adapter.in.web.dto.UpdateJobApplicationStatusRequest;
import com.sandbox.server.job.adapter.in.web.mapper.JobApplicationWebMapper;
import com.sandbox.server.job.adapter.out.persistence.mapper.port.in.SaveJobApplicationUseCase;
import com.sandbox.server.job.domain.JobApplication;
import com.sandbox.server.job.domain.JobApplicationId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/job-applications")
public class JobApplicationController {

    private final SaveJobApplicationUseCase saveJobApplicationUseCase;
    private final JobApplicationWebMapper mapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JobApplicationDto> create(@Valid @ModelAttribute CreateJobApplicationRequest request) throws IOException {

        JobApplication saved = saveJobApplicationUseCase.submit(
                mapper.toCompanyName(request),
                request.description(),
                mapper.toRate(request),
                mapper.toPaidLeave(request),
                mapper.toCv(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(saved));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<JobApplicationDto> updateStatus(@PathVariable UUID id, @RequestBody UpdateJobApplicationStatusRequest request) {

        JobApplication updated = saveJobApplicationUseCase.updateStatus(new JobApplicationId(id), request.status());

        return ResponseEntity.ok(mapper.toDto(updated));
    }
}
