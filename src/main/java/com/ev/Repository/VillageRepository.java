package com.ev.Repository;

import com.ev.Model.User;
import com.ev.Model.Village;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VillageRepository extends JpaRepository<Village , Long> {

    Optional<Village> findByVillageEmail(String villageEmail);
    Optional<Village> findByVillageNameIgnoreCase(String villageName);
    Optional<Village> findByVillageRegNumberIgnoreCase(String villageRegNumber);
    Optional<Village> findByVillageEmailAndVillageStatusIgnoreCase(String villageEmail , String villageStatus);
    Optional<Village> findByBankAccountNumber(String bankAccountNumber);

    List<Village> findByVillageStatusOrderByVillageIdDesc(String status);
    List<Village> findAllByOrderByVillageIdDesc();
    List<Village> findByVillageNameStartsWithIgnoreCaseAndVillageStatusIgnoreCaseOrderByVillageIdDesc(String villageName , String villageStatus);
    List<Village> findByVillageNameStartsWithIgnoreCaseOrderByVillageIdDesc(String villageName);
    List<Village> findByVillageCurrentStatusIgnoreCaseAndVillageStatusIgnoreCaseOrderByVillageIdDesc(String currentStatus,String villageName);
    List<Village> findByVillageCurrentStatusIgnoreCaseOrderByVillageIdDesc(String currentStatus);
    List<Village> findByVillageStatusIgnoreCaseOrderByVillageIdDesc(String villageStatus);

    @Query("SELECT DISTINCT v.villageLocation FROM Village v WHERE LOWER(v.villageLocation) LIKE CONCAT(:query, '%') ORDER BY v.villageLocation ASC")
    List<String> findDistinctLocationsContaining(@Param("query") String query);

    List<Village> findByVillageLocationStartingWithIgnoreCaseAndVillageNameStartingWithIgnoreCaseAndVillageStatusAndVillageCurrentStatusOrderByVillageNameAsc(String location, String villageName ,String villageStatus ,String villageCurrentStatus);
    List<Village> findByVillageLocationStartingWithIgnoreCaseAndVillageStatusAndVillageCurrentStatusOrderByVillageNameAsc(String villageLocation ,String villageStatus ,String villageCurrentStatus);
    List<Village> findByVillageNameStartingWithIgnoreCaseAndVillageStatusAndVillageCurrentStatusOrderByVillageNameAsc(String villageName ,String villageStatus ,String villageCurrentStatus);

}

