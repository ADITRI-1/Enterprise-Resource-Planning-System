package com.iiitd.erp.io.controller;

import com.iiitd.erp.Domain.CatalogSectionView;
import com.iiitd.erp.core.service.CatalogService;
import com.iiitd.erp.io.controller.support.Paging;
import com.iiitd.erp.io.dto.request.ListParams;
import com.iiitd.erp.io.dto.response.CourseResponse;
import com.iiitd.erp.io.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import static com.iiitd.erp.mappers.DomainToResponseMapper.DOMAIN_TO_RESPONSE_MAPPER_INSTANCE;

/** Course catalog, readable by any logged-in user. */
@RestController
@RequestMapping("/api/catalog")
@Tag(name = "Catalog")
public class CatalogController {

    private final CatalogService catalogService;
    private final Paging paging;

    public CatalogController(CatalogService catalogService, Paging paging) {
        this.catalogService = catalogService;
        this.paging = paging;
    }

    @GetMapping("/courses")
    @Operation(summary = "All courses")
    public PageResponse<CourseResponse> courses(@ParameterObject ListParams params) {
        return paging.page(catalogService.listCourses().stream()
                .map(DOMAIN_TO_RESPONSE_MAPPER_INSTANCE::courseDomainToResponse)
                .toList(), params);
    }

    @GetMapping("/sections")
    @Operation(summary = "Open sections with seats left, instructor and weekly schedule",
            description = "open = active, before the registration deadline and not full.")
    public PageResponse<CatalogSectionView> sections(@RequestParam(required = false) String semester,
                                                     @RequestParam(required = false) Integer year,
                                                     @RequestParam(required = false) String courseId,
                                                     @ParameterObject ListParams params) {
        return paging.page(catalogService.catalogSections(semester, year, courseId), params);
    }
}
