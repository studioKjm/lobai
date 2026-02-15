package com.lobai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "proactive_message_log", indexes = {
    @Index(name = "idx_pml_user_date", columnList = "user_id, trigger_date, trigger_type")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProactiveMessageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;

    @Column(name = "trigger_type", length = 50, nullable = false)
    private String triggerType;

    @Column(name = "trigger_detail", length = 500)
    private String triggerDetail;

    @Column(name = "trigger_date", nullable = false)
    private LocalDate triggerDate;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "was_displayed")
    @Builder.Default
    private Boolean wasDisplayed = false;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }

    public void markDisplayed() {
        this.wasDisplayed = true;
    }
}
