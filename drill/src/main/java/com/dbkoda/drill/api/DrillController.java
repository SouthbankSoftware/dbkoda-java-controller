package com.dbkoda.drill.api;

import com.dbkoda.drill.services.DrillService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/drill")
@Api(basePath = "/drill", value = "/", description = "dbKoda Drill Controller")
public class DrillController {

    @Autowired
    private DrillService drillService;

    @RequestMapping(method= RequestMethod.GET)
    public String greeting(@RequestParam(value="name", required=false, defaultValue="World") String name) {
        drillService.createConnection();
        return "greeting";
    }
}
