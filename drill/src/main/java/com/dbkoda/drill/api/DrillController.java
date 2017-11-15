package com.dbkoda.drill.api;

import com.dbkoda.drill.exceptions.DrillException;
import com.dbkoda.drill.model.ConnectionProfile;
import com.dbkoda.drill.services.DrillConnection;
import com.dbkoda.drill.services.DrillService;
import com.dbkoda.drill.utils.DrillLogger;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/drill")
@Api(basePath = "/drill", value = "/", description = "dbKoda Drill Controller")
public class DrillController implements DrillLogger {

    @Autowired
    private DrillService drillService;

    @RequestMapping(method = RequestMethod.POST, value = "/executing/{id}/{schema}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity executeSql(@PathVariable(value = "id", required = true) String id,
                                     @PathVariable(value = "schema", required = true) String schema,
                                     @RequestParam(value = "sql") String sql) throws DrillException {
        try {
            String ret = drillService.executeSql(id, schema, sql);
            return ResponseEntity.status(HttpStatus.OK).body(ret);
        } catch (DrillException e) {
            return ResponseEntity.status(e.getCode()).body(e.getMessage());
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity createConnection(@RequestBody ConnectionProfile connectionProfile) {
        info("receive:" + connectionProfile.toString());
        try {
            drillService.createConnection(connectionProfile);
        } catch (DrillException e) {
            return ResponseEntity.status(e.getCode()).body(e.getMessage());
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/profile")
    public @ResponseBody
    ResponseEntity addProfile(@RequestParam String id, @RequestParam String database) {
        ConnectionProfile profile = new ConnectionProfile();
        profile.setId(id);
        profile.setDatabase(database);
        drillService.addProfile(profile);
        return new ResponseEntity(HttpStatus.OK);
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "/profile/{id}/{schema}")
    public @ResponseBody
    Object deleteProfileConnection(@PathVariable String id, @PathVariable String schema) {
        drillService.removeProfile(id, schema);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/profile/{id}")
    public @ResponseBody
    Object deleteProfileConnections(@PathVariable String id) {
        drillService.removeProfileSchemas(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/profiles")
    public @ResponseBody
    Object deleteProfiles() {
        drillService.removeProfiles();
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Map<String, DrillConnection> getProfiles() {
        return drillService.getProfiles();
    }

}
