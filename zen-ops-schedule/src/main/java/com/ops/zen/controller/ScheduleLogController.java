package com.ops.zen.controller;

import com.ops.zen.entity.request.ScheduleLogQueryRequest;
import com.ops.zen.entity.response.ScheduleLogContentResponse;
import com.ops.zen.entity.response.ScheduleLogQueryResponse;
import com.ops.zen.service.ScheduleLogService;
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
@RequestMapping("/schedule/log")
public class ScheduleLogController {


    @Autowired
    ScheduleLogService logService;

    /**
     * 根据定时任务查询日志
     */
    @PostMapping("/queryBySchedule")
    public PageResult<ScheduleLogQueryResponse> queryBySchedule(
            @RequestBody ScheduleLogQueryRequest request) {
        return logService.queryBySchedule(request);
    }


    /**
     * 查看运行日志信息
     */
    @GetMapping("/content")
    public ScheduleLogContentResponse getContent(
            @RequestParam Long pkScheduleLog,
            @RequestParam(required = false) Integer offset) {
        return logService.getContent(pkScheduleLog, offset);
    }


    /**
     * 删除日志
     */
    @GetMapping("/delete")
    public String delete(@RequestParam String pkScheduleLog) {
        logService.delete(Long.parseLong(pkScheduleLog));
        return null;
    }

}
