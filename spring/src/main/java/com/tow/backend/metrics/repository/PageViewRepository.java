package com.tow.backend.metrics.repository;

import com.tow.backend.metrics.entity.PageView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PageViewRepository extends JpaRepository<PageView, Long> {

    // Contar visitas totales por URL
    @Query("SELECT p.url as url, COUNT(p) as visitas FROM PageView p GROUP BY p.url ORDER BY visitas DESC")
    List<Map<String, Object>> countVisitsByUrl();

    // Contar visitas por zona/país
    @Query("SELECT p.zona as zona, COUNT(p) as visitas FROM PageView p GROUP BY p.zona ORDER BY visitas DESC")
    List<Map<String, Object>> countVisitsByZona();

    // Obtener todas las visitas ordenadas por fecha (para exportación)
    List<PageView> findAllByOrderByFechaDesc();
}

