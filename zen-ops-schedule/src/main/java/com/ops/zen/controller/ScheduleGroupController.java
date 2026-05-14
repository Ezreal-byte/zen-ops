package com.ops.zen.controller;

import com.ops.zen.entity.response.ScheduleGroupListResponse;
import com.ops.zen.service.ScheduleGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author xyn
 * @date 2025/5/23 20:53
 * @description
 **/
@RestController
@Slf4j
@RequestMapping("/schedule/group")
public class ScheduleGroupController {


    @Autowired
    ScheduleGroupService service;
    @GetMapping("/list")
    public List<ScheduleGroupListResponse> get() {
        return service.queryList();
    }

    @GetMapping("/delete")
    public String delete(@RequestParam  Long pkScheduleGroup) {
        return service.delete(pkScheduleGroup);
    }

}
