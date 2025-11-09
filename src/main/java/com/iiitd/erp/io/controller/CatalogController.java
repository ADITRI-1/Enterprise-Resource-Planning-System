package com.iiitd.erp.io.controller;

import com.iiitd.erp.core.service.CatalogService;
import com.iiitd.erp.io.dto.request.CatalogSearchRequest;
import com.iiitd.erp.io.dto.response.CourseRow;
import com.iiitd.erp.io.dto.response.SectionRow;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController @RequestMapping("/api/catalog")
public class CatalogController {
  private final CatalogService catalogService;
  public CatalogController(CatalogService catalogService){ this.catalogService = catalogService; }
  @PostMapping("/list")
  public List<CourseRow> list(@RequestBody CatalogSearchRequest req){
    final String semester = req.semester()==null? "" : req.semester();
    final int year = req.year(); final String q = req.q()==null? "" : req.q().trim();
    return catalogService.listCourses(semester, year, q);
  }
  @GetMapping("/{courseId}/sections")
  public List<SectionRow> sections(@PathVariable long courseId, @RequestParam String semester, @RequestParam int year){
    return catalogService.listSections(courseId, semester, year);
  }
}
