package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * 自定义定时任务类
 */
@Component // 标记这是一个Spring组件
@Slf4j
public class MyTask {
    /**
     * 定时任务 每隔300秒触发一次
     */
    @Scheduled(cron = "0/300 * * * * ?")
    public void executeTask() {
        log.info("定时任务开始执行：{}", LocalDateTime.now());
    }
}
