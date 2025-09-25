package com.ev.Repository;

import com.ev.Model.VillageStay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VillageStayRepository extends JpaRepository<VillageStay,Long> {

    boolean existsByVillageStayNameIgnoreCaseAndVillage_VillageId(String villageStayName , Long villageId);

    List<VillageStay> findByVillage_VillageIdOrderByStayIdDesc(Long villageId);

    long countByVillage_VillageIdAndIsActiveTrue(Long villageId);

    long countByVillage_VillageId(Long villageId);

    @Query("SELECT s FROM VillageStay s WHERE " +
            "(:villageName IS NULL OR LOWER(s.village.villageName) LIKE LOWER(CONCAT('%', :villageName, '%'))) AND " +
            "(:region IS NULL OR LOWER(s.village.villageLocation) LIKE LOWER(CONCAT('%', :region, '%')))")
    List<VillageStay> filterStays(@Param("villageName") String villageName, @Param("region") String region);

}
