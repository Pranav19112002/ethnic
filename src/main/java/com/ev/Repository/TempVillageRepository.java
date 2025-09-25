package com.ev.Repository;

import com.ev.Model.TempVillage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TempVillageRepository extends JpaRepository<TempVillage , Long> {

    List<TempVillage> findByStatusIgnoreCase(String status);
    List<TempVillage> findByVillageIdOrderByIdDesc(Long villageId);

    boolean existsByVillageIdAndStatusIgnoreCase(Long villageId,String status);

    TempVillage findByVillageIdAndStatusIgnoreCase(Long villageId,String status);
}
