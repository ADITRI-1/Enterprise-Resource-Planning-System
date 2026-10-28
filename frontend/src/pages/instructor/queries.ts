import { useQuery } from "@tanstack/react-query";
import { api, type Schemas } from "@/lib/api";
import { useInstructorProfile } from "@/lib/session";
import type { Term } from "@/lib/utils";

/** The instructor's code (needed in every instructor URL), resolved from /api/instructor/me. */
export function useInstructorCode() {
  return useInstructorProfile().data?.instructorCode;
}

export function useMySections(code: string | undefined, term?: Term) {
  return useQuery({
    queryKey: ["instructor", code, "sections", term?.semester, term?.year],
    queryFn: () => api<Schemas["InstructorSectionResponse"][]>(`/api/instructor/${code}/sections`, { params: { semester: term?.semester, year: term?.year } }),
    enabled: !!code,
  });
}

export function useInstructorTimetable(code: string | undefined, term: Term) {
  return useQuery({
    queryKey: ["instructor", code, "timetable", term.semester, term.year],
    queryFn: () => api<Schemas["InstructorTimetableSlotResponse"][]>(`/api/instructor/${code}/timetable`, { params: { ...term } }),
    enabled: !!code,
  });
}

export const sectionKey = (code: string | undefined, sectionId: string) => ["instructor", code, "section", sectionId];
