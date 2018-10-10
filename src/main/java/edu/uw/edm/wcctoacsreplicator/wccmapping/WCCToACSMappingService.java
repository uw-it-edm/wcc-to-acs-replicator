package edu.uw.edm.wcctoacsreplicator.wccmapping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Maxime Deravet Date: 10/4/18
 */
@Service
public class WCCToACSMappingService {

    private WCCToACSMappingRepository repository;

    @Autowired
    public WCCToACSMappingService(WCCToACSMappingRepository repository) {
        this.repository = repository;
    }


    public boolean hasWCCID(String wccId) {
        return repository.existsById(wccId);
    }


    public Optional<WCCToACSMapping> getMappingForWCCId(String wccId) {

        return repository.findById(wccId);

    }

    public List<WCCToACSMapping> listAll() {


        return StreamSupport.stream(repository.findAll().spliterator(), true).collect(Collectors.toList());
    }

    public List<WCCToACSMapping> listAllForProfile(String profile) {


        return StreamSupport.stream(repository.findByProfile(profile).spliterator(), true).collect(Collectors.toList());
    }

    public WCCToACSMapping createEntry(String wccId, String acsId, String profile) {

        WCCToACSMapping wccToACSMapping = WCCToACSMapping.builder().acsId(acsId).wccId(wccId).profile(profile).build();

        return repository.save(wccToACSMapping);

    }
}
