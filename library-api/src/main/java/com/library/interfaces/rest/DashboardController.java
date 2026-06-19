package com.library.interfaces.rest;

import com.library.application.dashboard.AdminDashboardView;
import com.library.application.dashboard.DashboardApplicationService;
import com.library.application.dashboard.ReaderDashboardView;
import com.library.domain.shared.Result;
import com.library.interfaces.dto.dashboard.AdminDashboardResponse;
import com.library.interfaces.dto.dashboard.ReaderDashboardResponse;
import com.library.interfaces.security.RequireLibrarian;
import com.library.interfaces.security.RequireReader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardApplicationService dashboardApplicationService;

    public DashboardController(DashboardApplicationService dashboardApplicationService) {
        this.dashboardApplicationService = dashboardApplicationService;
    }

    @GetMapping("/admin")
    @RequireLibrarian
    public Result<AdminDashboardResponse> getAdminDashboard() {
        AdminDashboardView view = dashboardApplicationService.getAdminStats();
        return Result.ok(new AdminDashboardResponse(
                view.totalBooks(),
                view.totalReaders(),
                view.activeBorrows(),
                view.unpaidFines()
        ));
    }

    @GetMapping("/reader")
    @RequireReader
    public Result<ReaderDashboardResponse> getReaderDashboard(@RequestAttribute("userId") Long userId) {
        ReaderDashboardView view = dashboardApplicationService.getReaderStats(userId);
        return Result.ok(new ReaderDashboardResponse(
                view.activeBorrows(),
                view.overdueBorrows(),
                view.unpaidFines()
        ));
    }
}
