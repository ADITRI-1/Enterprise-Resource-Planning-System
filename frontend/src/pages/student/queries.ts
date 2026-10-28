import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api, type Schemas } from "@/lib/api";
import type { Term } from "@/lib/utils";

export function useRegistrations(uid: string, term?: Term) {
  return useQuery({
    queryKey: ["student", uid, "registrations", term?.semester, term?.year],
    queryFn: () =>
      api<Schemas["RegistrationView"][]>(`/api/student/${uid}/registrations`, { params: { semester: term?.semester, year: term?.year } }),
  });
}

export function useStudentTimetable(uid: string, term: Term) {
  return useQuery({
    queryKey: ["student", uid, "timetable", term.semester, term.year],
    queryFn: () => api<Schemas["TimetableSlot"][]>(`/api/student/${uid}/timetable`, { params: { ...term } }),
  });
}

export function useStudentGrades(uid: string) {
  return useQuery({
    queryKey: ["student", uid, "grades"],
    queryFn: () => api<Schemas["GradeRow"][]>(`/api/student/${uid}/grades`),
  });
}

export function useAcademicRecord(uid: string) {
  return useQuery({
    queryKey: ["student", uid, "record"],
    queryFn: () => api<Schemas["AcademicRecordView"]>(`/api/student/${uid}/academic-record`),
  });
}

/** Courses passed so far (final grade other than F), for checking prerequisites in the catalog. */
export function usePassedCourses(uid: string) {
  const record = useAcademicRecord(uid);
  return new Set(
    (record.data?.terms ?? []).flatMap((t) => t.courses ?? []).filter((c) => c.grade && c.grade !== "F").map((c) => c.courseId!),
  );
}

/** Register / drop; refreshes everything that shows seats or enrollments. */
export function useEnrollmentAction(uid: string, action: "register" | "drop") {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (sectionId: string) => api(`/api/student/${uid}/${action}`, { body: { sectionId } }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["student", uid] });
      qc.invalidateQueries({ queryKey: ["catalog"] });
      qc.invalidateQueries({ queryKey: ["notifications"] });
    },
  });
}
