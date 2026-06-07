package com.grade.system.controller;

import com.grade.system.annotation.AuditLog;
import com.grade.system.dto.ApiResponse;
import com.grade.system.dto.PageResponse;
import com.grade.system.entity.Announcement;
import com.grade.system.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping("/active")
    public ApiResponse<List<Announcement>> getActiveAnnouncements() {
        List<Announcement> announcements = announcementService.getActiveAnnouncements();
        return ApiResponse.success(announcements);
    }

    @GetMapping
    public ApiResponse<?> getAnnouncements(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        if (page != null && size != null) {
            PageResponse<Announcement> announcementPage = announcementService.getAnnouncementsPage(
                    title, type, status, startDate, endDate, page, size);
            return ApiResponse.success(announcementPage);
        } else {
            return ApiResponse.success("请使用分页参数查询");
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<Announcement> getAnnouncement(@PathVariable Long id) {
        Announcement announcement = announcementService.getAnnouncementById(id);
        if (announcement != null) {
            return ApiResponse.success(announcement);
        } else {
            return ApiResponse.error("公告不存在");
        }
    }

    @PostMapping
    @AuditLog(module = "公告管理", action = "新增", description = "新增公告", saveParams = true)
    public ApiResponse<Announcement> createAnnouncement(@RequestBody Announcement announcement) {
        Announcement created = announcementService.createAnnouncement(announcement);
        return ApiResponse.success("创建成功", created);
    }

    @PutMapping("/{id}")
    @AuditLog(module = "公告管理", action = "修改", description = "修改公告", saveParams = true)
    public ApiResponse<Announcement> updateAnnouncement(@PathVariable Long id, @RequestBody Announcement announcement) {
        Announcement updated = announcementService.updateAnnouncement(id, announcement);
        if (updated != null) {
            return ApiResponse.success("更新成功", updated);
        } else {
            return ApiResponse.error("公告不存在");
        }
    }

    @PatchMapping("/{id}/status")
    @AuditLog(module = "公告管理", action = "修改", description = "更新公告状态", saveParams = true)
    public ApiResponse<?> updateStatus(@PathVariable Long id, @RequestParam Boolean status) {
        boolean success = announcementService.updateStatus(id, status);
        if (success) {
            return ApiResponse.success("状态更新成功", null);
        } else {
            return ApiResponse.error("公告不存在");
        }
    }

    @DeleteMapping("/{id}")
    @AuditLog(module = "公告管理", action = "删除", description = "删除公告")
    public ApiResponse<?> deleteAnnouncement(@PathVariable Long id) {
        boolean success = announcementService.deleteAnnouncement(id);
        if (success) {
            return ApiResponse.success("删除成功", null);
        } else {
            return ApiResponse.error("公告不存在");
        }
    }
}
