package com.grade.system.service;

import com.grade.system.dto.PageResponse;
import com.grade.system.entity.Announcement;
import com.grade.system.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<Announcement> getActiveAnnouncements() {
        return announcementRepository.findByStatusTrueOrderByCreatedAtDesc();
    }

    public PageResponse<Announcement> getAnnouncementsPage(
            String title,
            String type,
            Boolean status,
            String startDate,
            String endDate,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;

        if (startDate != null && !startDate.isEmpty()) {
            startTime = LocalDate.parse(startDate, DATE_FORMATTER).atStartOfDay();
        }
        if (endDate != null && !endDate.isEmpty()) {
            endTime = LocalDate.parse(endDate, DATE_FORMATTER).atTime(LocalTime.MAX);
        }

        Page<Announcement> announcementPage = announcementRepository.findByConditions(
                title, type, status, startTime, endTime, pageable);

        PageResponse<Announcement> response = new PageResponse<>();
        response.setContent(announcementPage.getContent());
        response.setPageNumber(announcementPage.getNumber());
        response.setPageSize(announcementPage.getSize());
        response.setTotalElements(announcementPage.getTotalElements());
        response.setTotalPages(announcementPage.getTotalPages());
        response.setFirst(announcementPage.isFirst());
        response.setLast(announcementPage.isLast());
        return response;
    }

    public Announcement getAnnouncementById(Long id) {
        return announcementRepository.findById(id).orElse(null);
    }

    public Announcement createAnnouncement(Announcement announcement) {
        return announcementRepository.save(announcement);
    }

    public Announcement updateAnnouncement(Long id, Announcement announcement) {
        Announcement existing = announcementRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setTitle(announcement.getTitle());
            existing.setContent(announcement.getContent());
            existing.setType(announcement.getType());
            existing.setSortOrder(announcement.getSortOrder() != null ? announcement.getSortOrder() : 0);
            return announcementRepository.save(existing);
        }
        return null;
    }

    public boolean updateStatus(Long id, Boolean status) {
        Announcement existing = announcementRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setStatus(status);
            announcementRepository.save(existing);
            return true;
        }
        return false;
    }

    public boolean deleteAnnouncement(Long id) {
        if (announcementRepository.existsById(id)) {
            announcementRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
