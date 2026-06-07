package com.grade.system.service;

import com.grade.system.dto.PageResponse;
import com.grade.system.entity.Term;
import com.grade.system.enums.ErrorCode;
import com.grade.system.exception.DuplicateResourceException;
import com.grade.system.exception.ResourceNotFoundException;
import com.grade.system.repository.GradeRepository;
import com.grade.system.repository.TermRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TermService {

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private GradeRepository gradeRepository;

    public List<Term> getAllTerms() {
        return termRepository.findAllByOrderBySortOrderDescCreatedAtDesc();
    }

    public List<Term> getEnabledTerms() {
        return termRepository.findByEnabledTrueOrderBySortOrderDescCreatedAtDesc();
    }

    public PageResponse<Term> getTermsPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Term> termPage = termRepository.findAllByOrderBySortOrderDescCreatedAtDesc(pageable);
        return createPageResponse(termPage);
    }

    public PageResponse<Term> getTermsPageWithFilter(String name, Boolean enabled, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Term> termPage = termRepository.findByConditions(name, enabled, pageable);
        return createPageResponse(termPage);
    }

    private PageResponse<Term> createPageResponse(Page<Term> termPage) {
        PageResponse<Term> response = new PageResponse<>();
        response.setContent(termPage.getContent());
        response.setPageNumber(termPage.getNumber());
        response.setPageSize(termPage.getSize());
        response.setTotalElements(termPage.getTotalElements());
        response.setTotalPages(termPage.getTotalPages());
        response.setFirst(termPage.isFirst());
        response.setLast(termPage.isLast());
        return response;
    }

    public Term getTermById(Long id) {
        return termRepository.findById(id).orElseThrow(() ->
            new ResourceNotFoundException(ErrorCode.TERM_NOT_FOUND));
    }

    public Term createTerm(Term term) {
        if (termRepository.existsByName(term.getName())) {
            throw new DuplicateResourceException(ErrorCode.TERM_ALREADY_EXISTS);
        }
        return termRepository.save(term);
    }

    public Term updateTerm(Long id, Term termDetails) {
        Term term = termRepository.findById(id).orElseThrow(() ->
            new ResourceNotFoundException(ErrorCode.TERM_NOT_FOUND));

        if (termRepository.existsByNameAndIdNot(termDetails.getName(), id)) {
            throw new DuplicateResourceException(ErrorCode.TERM_ALREADY_EXISTS);
        }

        term.setName(termDetails.getName());
        term.setStartDate(termDetails.getStartDate());
        term.setEndDate(termDetails.getEndDate());
        term.setEnabled(termDetails.getEnabled());
        term.setSortOrder(termDetails.getSortOrder());
        return termRepository.save(term);
    }

    public void deleteTerm(Long id) {
        if (!termRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCode.TERM_NOT_FOUND);
        }
        termRepository.deleteById(id);
    }

    public List<String> getEnabledTermNames() {
        return termRepository.findEnabledTermNames();
    }

    public List<String> getAllTermNames() {
        Set<String> termNames = new LinkedHashSet<>();

        List<String> configuredTerms = termRepository.findEnabledTermNames();
        termNames.addAll(configuredTerms);

        List<String> historicalTerms = gradeRepository.findDistinctTerms();
        termNames.addAll(historicalTerms);

        List<String> result = new ArrayList<>(termNames);
        result.sort((a, b) -> {
            int idxA = configuredTerms.indexOf(a);
            int idxB = configuredTerms.indexOf(b);
            if (idxA != -1 && idxB != -1) {
                return Integer.compare(idxA, idxB);
            }
            if (idxA != -1) return -1;
            if (idxB != -1) return 1;
            return b.compareTo(a);
        });

        return result;
    }
}
