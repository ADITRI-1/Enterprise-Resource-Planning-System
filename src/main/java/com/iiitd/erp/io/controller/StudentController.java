package com.iiitd.erp.io.controller;

import com.iiitd.erp.core.service.AccessService;
import com.iiitd.erp.core.service.MaintenanceService;
import com.iiitd.erp.core.service.StudentService;
import com.iiitd.erp.io.dto.request.DropRequest;
import com.iiitd.erp.io.dto.request.RegisterRequest;
import com.iiitd.erp.io.dto.response.GradeRow;
import com.iiitd.erp.io.dto.response.RegistrationResult;
import com.iiitd.erp.io.dto.response.TimetableSlot;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;
@RestController @RequestMapping("/api/student")
public class StudentController {
  private final StudentService students; private final AccessService access; private final MaintenanceService maintenance;
  public StudentController(StudentService students, AccessService access, MaintenanceService maintenance){
    this.students = students; this.access = access; this.maintenance = maintenance;
  }
  @PostMapping("/{uid}/register")
  public RegistrationResult register(@PathVariable long uid, @RequestBody RegisterRequest req){
    access.ensureStudent(uid);
    if (maintenance.isReadOnly()) return RegistrationResult.blocked("Maintenance mode is ON");
    return students.register(uid, req.sectionId());
  }
  @PostMapping("/{uid}/drop")
  public boolean drop(@PathVariable long uid, @RequestBody DropRequest req){
    access.ensureStudent(uid);
    if (maintenance.isReadOnly()) throw new IllegalStateException("Maintenance mode is ON");
    return students.drop(uid, req.sectionId());
  }
  @GetMapping("/{uid}/timetable")
  public List<TimetableSlot> timetable(@PathVariable long uid, @RequestParam String semester, @RequestParam int year){
    access.ensureStudent(uid); return students.myTimetable(uid, semester, year);
  }
  @GetMapping("/{uid}/grades")
  public List<GradeRow> grades(@PathVariable long uid){
    access.ensureStudent(uid); return students.myGrades(uid);
  }
  @GetMapping(value = "/{uid}/transcript.csv", produces = MediaType.TEXT_PLAIN_VALUE)
  public String transcriptPath(@PathVariable long uid){
    access.ensureStudent(uid); Path p = students.exportTranscriptCsv(uid); return p.toString();
  }
  @GetMapping("/{uid}/transcript/download")
  public ResponseEntity<Resource> transcriptDownload(@PathVariable long uid){
    access.ensureStudent(uid); Path p = students.exportTranscriptCsv(uid);
    FileSystemResource res = new FileSystemResource(p); if(!res.exists()) return ResponseEntity.notFound().build();
    return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv"))
      .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transcript-"+uid+".csv").body(res);
  }
}
