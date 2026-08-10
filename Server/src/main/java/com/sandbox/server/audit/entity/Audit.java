package com.sandbox.server.audit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "AUDIT")
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AUDIT_ID")
    private Long id;

    @Column(name = "URL")
    private String url;

    @Column(name = "USER_UUID")
    private UUID userUUID;

    @Column(name = "ACTION_TIME")
    private ZonedDateTime actionTime;
}
