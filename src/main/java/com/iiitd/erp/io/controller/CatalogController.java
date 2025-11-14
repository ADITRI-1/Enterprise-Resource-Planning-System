package com.iiitd.erp.io.controller;

import com.iiitd.erp.core.service.CatalogService;
import com.iiitd.erp.exception.ErrorCode;
import com.iiitd.erp.exception.ErpException;
import com.iiitd.erp.io.dto.request.CatalogSearchRequest;
import com.iiitd.erp.io.dto.response.CourseRow;
import com.iiitd.erp.io.dto.response.SectionRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.iiitd.erp.io.controller.support.ApiResponseSupport.error;
import static com.iiitd.erp.io.controller.support.ApiResponseSupport.from;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogController.class);

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService){
        this.catalogService = catalogService;
    }

    @PostMapping("/list")
    public ResponseEntity<?> list(@RequestBody CatalogSearchRequest request){
        final String semester = request.semester() == null ? "" : request.semester();
        final int year = request.year();
        final String query = request.q() == null ? "" : request.q().trim();

        try {
            List<CourseRow> courses = catalogService.listCourses(semester, year, query);
            LOGGER.debug("Catalog list returned {} courses", courses.size());
            return ResponseEntity.ok(courses);
        } catch (ErpException ex) {
            LOGGER.error("Catalog list failed: {}", ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected catalog list error", ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.CATALOG_LIST_FAILED);
        }
    }

    @GetMapping("/{courseId}/sections")
    public ResponseEntity<?> sections(@PathVariable long courseId,
                                      @RequestParam String semester,
                                      @RequestParam int year){
        try {
            List<SectionRow> sections = catalogService.listSections(courseId, semester, year);
            LOGGER.debug("Catalog sections for course {} returned {}", courseId, sections.size());
            return ResponseEntity.ok(sections);
        } catch (ErpException ex) {
            LOGGER.error("Catalog sections failed for course {}: {}", courseId, ex.getErrorCode().getCode(), ex);
            return from(ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error fetching sections for {}", courseId, ex);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.CATALOG_SECTIONS_FAILED,
                    ErrorCode.CATALOG_SECTIONS_FAILED.format(courseId));
        }
    }
}