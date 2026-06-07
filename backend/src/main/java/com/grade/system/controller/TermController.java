package com.grade.system.controller;

import com.grade.system.annotation.AuditLog;
import com.grade.system.context.UserContext;
import com.grade.system.dto.ApiResponse;
import com.grade.system.dto.PageResponse;
import com.grade.system.entity.Term;
import com.grade.system.service.TermService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terms")
public class TermController {

    @Autowired
    private TermService termService;

    private String getAdminDeniedMessage(String message) {
        if (!UserContext.isLoggedIn()) {
            return "用户未登录";
        }
        if (!"ADMIN".equals(UserContext.getUserRole())) {
            return message;
        }
        return null;
    }

    @GetMapping("/enabled")
    public ApiResponse<List<Term>> getEnabledTerms() {
        List<Term> terms = termService.getEnabledTerms();
        return ApiResponse.success(terms);
    }

    @GetMapping("/names")
    public ApiResponse<List<String>> getAllTermNames() {
        List<String> termNames = termService.getAllTermNames();
        return ApiResponse.success(termNames);
    }

    @GetMapping
    public ApiResponse<?> getTerms(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean enabled) {

        String deniedMessage = getAdminDeniedMessage("无权限访问学期管理");
        if (deniedMessage != null) {
            return ApiResponse.error(deniedMessage);
        }

        if (page != null && size != null) {
            PageResponse<Term> termPage = termService.getTermsPageWithFilter(name, enabled, page, size);
            return ApiResponse.success(termPage);
        } else {
            List<Term> terms = termService.getAllTerms();
            return ApiResponse.success(terms);
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<Term> getTerm(@PathVariable Long id) {
        String deniedMessage = getAdminDeniedMessage("无权限访问学期管理");
        if (deniedMessage != null) {
            return ApiResponse.error(deniedMessage);
        }

        Term term = termService.getTermById(id);
        return ApiResponse.success(term);
    }

    @PostMapping
    @AuditLog(module = "学期管理", action = "新增", description = "新增学期")
    public ApiResponse<Term> createTerm(@RequestBody Term term) {
        String deniedMessage = getAdminDeniedMessage("无权限维护学期");
        if (deniedMessage != null) {
            return ApiResponse.error(deniedMessage);
        }

        Term created = termService.createTerm(term);
        return ApiResponse.success("创建成功", created);
    }

    @PutMapping("/{id}")
    @AuditLog(module = "学期管理", action = "修改", description = "修改学期")
    public ApiResponse<Term> updateTerm(@PathVariable Long id, @RequestBody Term term) {
        String deniedMessage = getAdminDeniedMessage("无权限维护学期");
        if (deniedMessage != null) {
            return ApiResponse.error(deniedMessage);
        }

        Term updated = termService.updateTerm(id, term);
        return ApiResponse.success("更新成功", updated);
    }

    @DeleteMapping("/{id}")
    @AuditLog(module = "学期管理", action = "删除", description = "删除学期")
    public ApiResponse<Void> deleteTerm(@PathVariable Long id) {
        String deniedMessage = getAdminDeniedMessage("无权限维护学期");
        if (deniedMessage != null) {
            return ApiResponse.error(deniedMessage);
        }

        termService.deleteTerm(id);
        return ApiResponse.success("删除成功", null);
    }
}
