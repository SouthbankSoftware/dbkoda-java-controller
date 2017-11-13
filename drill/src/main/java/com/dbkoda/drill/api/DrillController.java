package com.dbkoda.drill.api;

import com.dbkoda.drill.exceptions.DrillException;
import com.dbkoda.drill.model.ConnectionProfile;
import com.dbkoda.drill.services.DrillConnection;
import com.dbkoda.drill.services.DrillService;
import com.dbkoda.drill.utils.DrillLogger;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/drill")
@Api(basePath = "/drill", value = "/", description = "dbKoda Drill Controller")
public class DrillController implements DrillLogger {

    @Autowired
    private DrillService drillService;

    @RequestMapping(method = RequestMethod.POST, value = "/executing/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String executeSql(@PathVariable(value = "id", required = true) String id,
                             @RequestParam(value = "sql", required = true) String sql) throws DrillException {
        return drillService.executeSql(id, sql);
    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody
    ConnectionProfile createConnection(@RequestBody ConnectionProfile connectionProfile) {
        info("receive:" + connectionProfile.toString());
        drillService.createConnection(connectionProfile);
        return connectionProfile;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/profile")
    public @ResponseBody
    Object addProfile(@RequestParam String id) {
        return drillService.addProfile(id);
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "/profile/{id}")
    public @ResponseBody
    Object deleteProfile(@PathVariable String id) {
        return drillService.removeProfile(id);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/profiles", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Map<String, DrillConnection> getProfiles() {
        return drillService.getProfiles();
    }

}
