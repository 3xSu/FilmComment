package com.fc.controller.user;

import com.fc.result.PageResult;
import com.fc.result.Result;
import com.fc.service.user.NotificationService;
import com.fc.entity.Notification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/user/notification")
@Tag(name = "用户通知接口")
@Slf4j
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数量")
    public Result<Integer> getUnreadCount() {
        Long userId = com.fc.context.BaseContext.getCurrentId();
        Integer count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 获取通知列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取通知列表（无限滚动分页）")
    public Result<PageResult> getNotifications(
            @RequestParam(required = false) @Parameter(description = "游标（最后一条通知的创建时间）") LocalDateTime cursor,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页大小") Integer size) {

        Long userId = com.fc.context.BaseContext.getCurrentId();

        // 参数校验
        if (size < 1 || size > 100) size = 20;

        PageResult pageResult = notificationService.getNotifications(userId, cursor, size);
        return Result.success(pageResult);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记通知为已读")
    public Result<String> markAsRead(@PathVariable Long id) {
        Long userId = com.fc.context.BaseContext.getCurrentId();
        notificationService.markAsRead(id, userId);
        return Result.success("标记成功");
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read-all")
    @Operation(summary = "标记所有通知为已读")
    public Result<String> markAllAsRead() {
        Long userId = com.fc.context.BaseContext.getCurrentId();
        notificationService.markAllAsRead(userId);
        return Result.success("全部标记为已读");
    }
}