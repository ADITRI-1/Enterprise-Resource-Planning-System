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

/** Register / drop; refreshes everything that shows seats or enrollments. */
export function useEnrollmentAction(uid: string, action: "register" | "drop") {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (sectionId: string) => api(`/api/student/${uid}/${action}`, { body: { sectionId } }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["student", uid] });
      qc.invalidateQueries({ queryKey: ["catalog"] });
    },
  });
}
