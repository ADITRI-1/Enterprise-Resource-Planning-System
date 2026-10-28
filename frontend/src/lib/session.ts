import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api, ApiError, type Schemas } from "./api";

export type Role = "student" | "instructor" | "admin";

export type Session = {
  userId: string;
  username: string;
  role: Role;
  mustChangePassword: boolean;
  lastLogin?: string;
};

function toSession(raw: Schemas["AuthResultDomain"]): Session {
  return {
    userId: raw.userId!,
    username: raw.username!,
    role: (raw.role ?? "").toLowerCase() as Role,
    mustChangePassword: !!raw.mustChangePassword,
    lastLogin: raw.lastLogin,
  };
}

/** The logged-in account, or null when there is no session. */
export function useSession() {
  return useQuery({
    queryKey: ["session"],
    queryFn: async () => {
      try {
        return toSession(await api<Schemas["AuthResultDomain"]>("/api/auth/me"));
      } catch (e) {
        if (e instanceof ApiError && e.status === 401) return null;
        throw e;
      }
    },
    staleTime: 5 * 60_000,
    retry: false,
  });
}

export function useLogin() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: { username: string; password: string }) =>
      api<Schemas["LoginResponse"]>("/api/auth/login", { body }),
    onSuccess: async () => {
      qc.removeQueries();
      await qc.fetchQuery({ queryKey: ["session"], queryFn: async () => toSession(await api("/api/auth/me")) });
    },
  });
}

export function useLogout() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => api("/api/auth/logout", { method: "POST" }),
    onSettled: () => {
      qc.clear();
      qc.setQueryData(["session"], null);
    },
  });
}

export function useStudentProfile(enabled = true) {
  return useQuery({
    queryKey: ["student", "me"],
    queryFn: () => api<Schemas["StudentProfileResponse"]>("/api/student/me"),
    enabled,
    staleTime: Infinity,
  });
}

export function useInstructorProfile(enabled = true) {
  return useQuery({
    queryKey: ["instructor", "me"],
    queryFn: () => api<Schemas["InstructorProfileResponse"]>("/api/instructor/me"),
    enabled,
    staleTime: Infinity,
  });
}

/** Polled so the banner appears for everyone shortly after an admin flips the switch. */
export function useMaintenance() {
  return useQuery({
    queryKey: ["maintenance"],
    queryFn: () => api<Schemas["MaintenanceStatusResponse"]>("/api/system/maintenance"),
    refetchInterval: 30_000,
  });
}
