package edu.uw.edm.wcctoacsreplicator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import edu.uw.edm.wcctoacsreplicator.wccmapping.WCCToACSMapping;
import edu.uw.edm.wcctoacsreplicator.wccmapping.WCCToACSMappingService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Maxime Deravet Date: 10/4/18
 */
@RestController
@RequestMapping("/replicator")
@Slf4j

public class ListMappingController {

    private WCCToACSMappingService wccToACSMappingService;

    @Autowired
    public ListMappingController(WCCToACSMappingService wccToACSMappingService) {
        this.wccToACSMappingService = wccToACSMappingService;
    }

    @RequestMapping(value = "list", method = RequestMethod.GET)
    public ResponseEntity<List<WCCToACSMapping>> listMapping() {

        return new ResponseEntity<>(wccToACSMappingService.listAll(), HttpStatus.OK);
    }

    @RequestMapping(value = "list/{profile}", method = RequestMethod.GET)
    public ResponseEntity<List<WCCToACSMapping>> listMappingForProfile(@PathVariable String profile) {

        return new ResponseEntity<>(wccToACSMappingService.listAllForProfile(profile), HttpStatus.OK);
    }

    //TODO probably delete me
    @RequestMapping(value = "addMapping", method = RequestMethod.POST)
    public ResponseEntity<WCCToACSMapping> addMapping(
            @RequestParam(value = "wccId") String wccId,
            @RequestParam(value = "acsId") String acsID,
            @RequestParam(value = "profile") String profile
    ) {

        return new ResponseEntity<>(wccToACSMappingService.createEntry(wccId,acsID,profile), HttpStatus.OK);
    }
}
