package com.ops.zen.controller;

import com.ops.zen.support.ReturnOriginalControllerValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xyn
 * @date 2026/5/7 20:06
 * @description
 **/
@RestController
@RequestMapping("/actuator")
@Slf4j
public class ActuatorController {

    @RequestMapping("/health")
    @ReturnOriginalControllerValue
    public String health() {
        return "service is ok!";
    }
}
