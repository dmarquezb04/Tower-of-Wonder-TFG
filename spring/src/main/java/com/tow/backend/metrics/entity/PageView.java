package com.tow.backend.metrics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "page_views")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private LocalDateTime fecha;

    private String ip;

    private String navegador;

    private String zona; // Aquí guardaremos el país o región si lo detectamos
}

