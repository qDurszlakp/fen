package com.sandbox.server.job.adapter.in.web;

import com.sandbox.server.job.adapter.in.web.dto.CreateJobApplicationRequest;
import com.sandbox.server.job.adapter.in.web.dto.JobApplicationDto;
import com.sandbox.server.job.adapter.in.web.mapper.JobApplicationWebMapper;
import com.sandbox.server.job.application.port.in.SaveJobApplicationUseCase;
import com.sandbox.server.job.domain.JobApplication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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
                mapper.toRate(request),
                mapper.toPaidLeave(request),
                mapper.toCv(request));

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(saved));
    }
}
