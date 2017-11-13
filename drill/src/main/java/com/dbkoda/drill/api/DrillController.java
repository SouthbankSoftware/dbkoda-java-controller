package com.dbkoda.drill.api;

import com.dbkoda.drill.exceptions.DrillException;
import com.dbkoda.drill.model.ConnectionProfile;
import com.dbkoda.drill.services.DrillService;
import com.dbkoda.drill.utils.DrillLogger;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/drill")
@Api(basePath = "/drill", value = "/", description = "dbKoda Drill Controller")
public class DrillController implements DrillLogger {

    @Autowired
    private DrillService drillService;

    @RequestMapping(method = RequestMethod.GET)
    public String greeting(@RequestParam(value = "name", required = true) String name) {
        return "Hello " + name;
    }

//    @RequestMapping(method = RequestMethod.POST)
//    public void createConnection(@RequestParam(value = "name", required = true) String name,
//                                 @RequestParam(value = "id", required = true) String id) throws DrillException {
//        drillService.createConnection(id, name, "mongodb://localhost");
//    }

    @RequestMapping(method = RequestMethod.POST, value = "/executing/{id}", produces = "application/json")
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

    @RequestMapping(method = RequestMethod.POST, value="/profile")
    public @ResponseBody
    Object addProfile(@RequestParam String id) {
        return drillService.addProfile(id);
    }


    @RequestMapping(method = RequestMethod.DELETE, value="/profile")
    public @ResponseBody
    Object deleteProfile(@RequestParam String id) {
        return drillService.removeProfile(id);
    }



}
