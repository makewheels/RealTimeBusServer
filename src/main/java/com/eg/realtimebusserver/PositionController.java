package com.eg.realtimebusserver;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @time 2020-01-27 15:34
 */
@Controller
@RequestMapping("position")
public class PositionController {
    @RequestMapping("getNearestStations")
    @ResponseBody
    public String getNearestStations(@RequestParam String position) {
        return null;
    }
}
