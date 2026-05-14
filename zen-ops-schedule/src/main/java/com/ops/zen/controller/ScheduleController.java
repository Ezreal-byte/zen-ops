package com.ops.zen.controller;

import com.ops.zen.entity.request.ScheduleAddUpdateRequest;
import com.ops.zen.entity.request.ScheduleQueryRequest;
import com.ops.zen.entity.response.ScheduleGetResponse;
import com.ops.zen.entity.response.ScheduleQueryResponse;
import com.ops.zen.service.ScheduleService;
import com.ops.zen.utils.map.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xyn
 * @date 2025/5/23 20:53
 * @description
 **/
@RestController
@Slf4j
@RequestMapping("/schedule")
public class ScheduleController {


    @Autowired
    ScheduleService service;

    @PostMapping("/queryByPage")
    public PageResult<ScheduleQueryResponse> queryByPage(
            @RequestBody ScheduleQueryRequest request) {
        return service.queryByPage(request);
    }

    @PostMapping("/add")
    public String add(@RequestBody ScheduleAddUpdateRequest request) {
        return service.add(request);
    }

    @PostMapping("/update")
    public String update(@RequestBody ScheduleAddUpdateRequest request) {
        return service.update(request);
    }

    @GetMapping("/get")
    public ScheduleGetResponse get(@RequestParam String pkSchedule) {
        return service.get(Long.parseLong(pkSchedule));
    }

    @GetMapping("/delete")
    public String delete(@RequestParam String pkSchedule) {
        return service.delete(Long.parseLong(pkSchedule));
    }


}
