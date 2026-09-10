package com.sandbox.server.job.adapter.in.web;

import com.sandbox.server.job.adapter.in.web.dto.CreateJobApplicationRequest;
import com.sandbox.server.job.adapter.in.web.dto.JobApplicationDto;
import com.sandbox.server.job.adapter.in.web.dto.UpdateJobApplicationDescriptionRequest;
import com.sandbox.server.job.adapter.in.web.dto.UpdateJobApplicationRequest;
import com.sandbox.server.job.adapter.in.web.dto.UpdateJobApplicationStatusRequest;
import com.sandbox.server.job.adapter.in.web.mapper.JobApplicationWebMapper;
import com.sandbox.server.job.application.port.in.DeleteJobApplicationUseCase;
import com.sandbox.server.job.application.port.in.FindJobApplicationsUseCase;
import com.sandbox.server.job.application.port.in.SubmitJobApplicationUseCase;
import com.sandbox.server.job.application.port.in.UpdateJobApplicationUseCase;
import com.sandbox.server.job.domain.JobApplication;
import com.sandbox.server.job.domain.JobApplicationId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/job-applications")
public class JobApplicationController {

    private final SubmitJobApplicationUseCase submitJobApplicationUseCase;
    private final UpdateJobApplicationUseCase updateJobApplicationUseCase;
    private final FindJobApplicationsUseCase findJobApplicationsUseCase;
    private final DeleteJobApplicationUseCase deleteJobApplicationUseCase;
    private final JobApplicationWebMapper mapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JobApplicationDto> create(@Valid @ModelAttribute CreateJobApplicationRequest request) throws IOException {

        JobApplication saved = submitJobApplicationUseCase.submit(
                mapper.toCompanyName(request),
                request.description(),
                mapper.toRate(request),
                mapper.toPaidLeave(request),
                mapper.toCv(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(saved));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<JobApplicationDto> updateStatus(@PathVariable UUID id, @RequestBody UpdateJobApplicationStatusRequest request) {

        JobApplication updated = updateJobApplicationUseCase.updateStatus(new JobApplicationId(id), request.version(), request.status());

        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @PatchMapping("/{id}/description")
    public ResponseEntity<JobApplicationDto> updateDescription(@PathVariable UUID id, @RequestBody UpdateJobApplicationDescriptionRequest request) {

        JobApplication updated = updateJobApplicationUseCase.updateDescription(new JobApplicationId(id), request.version(), request.description());

        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<JobApplicationDto> update(@PathVariable UUID id, @RequestBody UpdateJobApplicationRequest request) {

        JobApplication updated = updateJobApplicationUseCase.update(new JobApplicationId(id), request.version(), request.description(), request.status());

        return ResponseEntity.ok(mapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        deleteJobApplicationUseCase.delete(new JobApplicationId(id));

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedModel<JobApplicationDto>> list(
            @PageableDefault(size = 500, sort = "sentAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(new PagedModel<>(findJobApplicationsUseCase.findAll(pageable).map(mapper::toDto)));
    }

    @GetMapping("/{id}/cv")
    public ResponseEntity<byte[]> downloadCv(@PathVariable UUID id) {
        JobApplication jobApplication = findJobApplicationsUseCase.findById(new JobApplicationId(id))
                .orElseThrow(() -> new NoSuchElementException("no job application " + id));

        if (jobApplication.cv() == null) {
            return ResponseEntity.notFound().build();
        }

        String filename = jobApplication.companyName().value().replaceAll("[^a-zA-Z0-9-]+", "_") + "-cv.pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(jobApplication.cv());
    }
}
