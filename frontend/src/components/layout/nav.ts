import {
  BarChart3, BookOpen, Building2, CalendarDays, ClipboardList, DoorOpen, GraduationCap, KeyRound,
  LayoutDashboard, Layers, ListChecks, School, Users, type LucideIcon,
} from "lucide-react";
import type { Role } from "@/lib/session";

export type NavItem = { to: string; label: string; icon: LucideIcon };

export const NAV: Record<Role, NavItem[]> = {
  student: [
    { to: "/student", label: "Dashboard", icon: LayoutDashboard },
    { to: "/student/catalog", label: "Course catalog", icon: BookOpen },
    { to: "/student/registrations", label: "My registrations", icon: ClipboardList },
    { to: "/student/timetable", label: "Timetable", icon: CalendarDays },
    { to: "/student/grades", label: "Grades", icon: GraduationCap },
  ],
  instructor: [
    { to: "/instructor", label: "Dashboard", icon: LayoutDashboard },
    { to: "/instructor/sections", label: "My sections", icon: Layers },
    { to: "/instructor/timetable", label: "Timetable", icon: CalendarDays },
  ],
  admin: [
    { to: "/admin", label: "Dashboard", icon: LayoutDashboard },
    { to: "/admin/accounts", label: "Accounts", icon: KeyRound },
    { to: "/admin/students", label: "Students", icon: Users },
    { to: "/admin/instructors", label: "Instructors", icon: School },
    { to: "/admin/courses", label: "Courses", icon: BookOpen },
    { to: "/admin/sections", label: "Sections", icon: ListChecks },
    { to: "/admin/rooms", label: "Rooms", icon: DoorOpen },
    { to: "/admin/departments", label: "Departments", icon: Building2 },
    { to: "/admin/programs", label: "Programs", icon: BarChart3 },
  ],
};
