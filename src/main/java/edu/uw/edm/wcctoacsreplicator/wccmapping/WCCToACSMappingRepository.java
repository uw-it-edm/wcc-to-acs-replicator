package edu.uw.edm.wcctoacsreplicator.wccmapping;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

/**
 * @author Maxime Deravet Date: 10/4/18
 */
 interface WCCToACSMappingRepository extends CrudRepository<WCCToACSMapping, String> {
    @EnableScan
    @EnableScanCount
    Page<WCCToACSMapping> findAll(Pageable pageable);

    @EnableScan
    @EnableScanCount
    Iterable<WCCToACSMapping> findAll();

    @EnableScan
    @EnableScanCount
    Iterable<WCCToACSMapping> findByProfile(String profile);
}
