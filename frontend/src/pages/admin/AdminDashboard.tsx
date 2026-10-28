import { Link } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { AlertTriangle, BookOpen, ClipboardList, DoorOpen, KeyRound, Layers, Lock, School, Users } from "lucide-react";
import { api, type Schemas } from "@/lib/api";
import { notify } from "@/lib/toast";
import { cn } from "@/lib/utils";
import { Card, CardHeader, PageHeader, Spinner, StatCard } from "@/components/ui/misc";

export function AdminDashboard() {
  const qc = useQueryClient();
  const stats = useQuery({ queryKey: ["admin", "dashboard"], queryFn: () => api<Schemas["DashboardView"]>("/api/admin/dashboard") });
  const toggle = useMutation({
    mutationFn: (on: boolean) => api<Schemas["MaintenanceStatusResponse"]>("/api/admin/settings/maintenance", { method: "PUT", body: { maintenanceOn: on } }),
    onSuccess: (res) => {
      notify.success(`Maintenance mode ${res.maintenanceOn ? "on" : "off"}`);
      qc.invalidateQueries({ queryKey: ["maintenance"] });
      qc.invalidateQueries({ queryKey: ["admin", "dashboard"] });
    },
    onError: notify.error,
  });

  if (stats.isLoading || !stats.data) return <Spinner />;
  const s = stats.data;
  const on = !!s.maintenanceOn;

  return (
    <>
      <PageHeader title="Admin dashboard" description="An overview of people, courses and system state." />
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Students" value={s.students} icon={<Users className="h-5 w-5" />} />
        <StatCard label="Instructors" value={s.instructors} icon={<School className="h-5 w-5" />} tone="accent" />
        <StatCard label="Courses" value={s.courses} icon={<BookOpen className="h-5 w-5" />} hint={`${s.departments} departments · ${s.programs} programs`} tone="warning" />
        <StatCard label="Rooms" value={s.rooms} icon={<DoorOpen className="h-5 w-5" />} tone="success" />
        <StatCard label="Active sections" value={s.activeSections} icon={<Layers className="h-5 w-5" />} hint={`${s.fullSections} full`} />
        <StatCard label="Active enrollments" value={s.activeEnrollments} icon={<ClipboardList className="h-5 w-5" />} tone="accent" />
        <StatCard label="Locked accounts" value={s.lockedAccounts} icon={<Lock className="h-5 w-5" />} tone={s.lockedAccounts ? "danger" : "neutral"} />
        <StatCard label="Pending password changes" value={s.pendingPasswordChanges} icon={<KeyRound className="h-5 w-5" />} tone="neutral" />
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader title="Maintenance mode" description="While on, students and instructors can view data but can't change anything." />
          <div className="flex items-center justify-between gap-4 p-5">
            <div className="flex items-center gap-3">
              <div className={cn("rounded-lg p-2", on ? "bg-warning/15 text-warning" : "bg-success/10 text-success")}><AlertTriangle className="h-5 w-5" /></div>
              <div>
                <p className="font-medium">{on ? "Maintenance is ON" : "System is live"}</p>
                <p className="text-sm text-fg-muted">{on ? "A banner is shown to every user." : "All users can make changes."}</p>
              </div>
            </div>
            <button
              role="switch"
              aria-checked={on}
              aria-label="Maintenance mode"
              disabled={toggle.isPending}
              onClick={() => toggle.mutate(!on)}
              className={cn("relative h-7 w-12 shrink-0 rounded-full transition-colors disabled:opacity-50", on ? "bg-warning" : "bg-border")}
            >
              <span className={cn("absolute top-1 h-5 w-5 rounded-full bg-white shadow transition-transform", on ? "translate-x-6" : "translate-x-1")} />
            </button>
          </div>
        </Card>
        <Card>
          <CardHeader title="Quick actions" />
          <div className="grid grid-cols-2 gap-2 p-4">
            {[
              { to: "/admin/students", label: "Add a student" },
              { to: "/admin/instructors", label: "Add an instructor" },
              { to: "/admin/courses", label: "Create a course" },
              { to: "/admin/sections", label: "Schedule sections" },
            ].map((a) => (
              <Link key={a.to} to={a.to} className="rounded-lg border border-border px-4 py-3 text-sm font-medium hover:border-primary hover:text-primary">{a.label}</Link>
            ))}
          </div>
        </Card>
      </div>
    </>
  );
}
