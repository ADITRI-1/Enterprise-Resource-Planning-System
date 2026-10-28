import { lazy, Suspense, useEffect, type ComponentType, type ReactNode } from "react";
import { Navigate, Route, Routes, useNavigate } from "react-router-dom";
import { useQueryClient } from "@tanstack/react-query";
import { UNAUTHORIZED_EVENT } from "@/lib/api";
import { useSession, type Role, type Session } from "@/lib/session";
import { TermProvider } from "@/lib/term";
import { Spinner } from "@/components/ui/misc";
import { AppShell } from "@/components/layout/AppShell";
import { LoginPage } from "@/pages/auth/LoginPage";
import { ChangePasswordPage, ForcedPasswordChange } from "@/pages/auth/ChangePasswordPage";

function lazyPage<M extends Record<string, unknown>, K extends keyof M>(load: () => Promise<M>, name: K) {
  return lazy(async () => ({ default: (await load())[name] as ComponentType<any> }));
}

// Pages load on demand so each role only downloads its own screens.
const StudentDashboard = lazyPage(() => import("@/pages/student/StudentDashboard"), "StudentDashboard");
const CatalogPage = lazyPage(() => import("@/pages/student/CatalogPage"), "CatalogPage");
const RegistrationsPage = lazyPage(() => import("@/pages/student/RegistrationsPage"), "RegistrationsPage");
const StudentTimetablePage = lazyPage(() => import("@/pages/student/TimetablePage"), "StudentTimetablePage");
const GradesPage = lazyPage(() => import("@/pages/student/GradesPage"), "GradesPage");
const InstructorDashboard = lazyPage(() => import("@/pages/instructor/InstructorDashboard"), "InstructorDashboard");
const InstructorSectionsPage = lazyPage(() => import("@/pages/instructor/SectionsPage"), "InstructorSectionsPage");
const SectionPage = lazyPage(() => import("@/pages/instructor/SectionPage"), "SectionPage");
const InstructorTimetablePage = lazyPage(() => import("@/pages/instructor/TimetablePage"), "InstructorTimetablePage");
const AdminDashboard = lazyPage(() => import("@/pages/admin/AdminDashboard"), "AdminDashboard");
const AccountsPage = lazyPage(() => import("@/pages/admin/AccountsPage"), "AccountsPage");
const SectionsPage = lazyPage(() => import("@/pages/admin/SectionsPage"), "SectionsPage");
const CoursesPage = lazyPage(() => import("@/pages/admin/resources"), "CoursesPage");
const DepartmentsPage = lazyPage(() => import("@/pages/admin/resources"), "DepartmentsPage");
const InstructorsPage = lazyPage(() => import("@/pages/admin/resources"), "InstructorsPage");
const ProgramsPage = lazyPage(() => import("@/pages/admin/resources"), "ProgramsPage");
const RoomsPage = lazyPage(() => import("@/pages/admin/resources"), "RoomsPage");
const StudentsPage = lazyPage(() => import("@/pages/admin/resources"), "StudentsPage");

const HOME: Record<Role, string> = { student: "/student", instructor: "/instructor", admin: "/admin" };

function Only({ role, session, children }: { role: Role; session: Session; children: ReactNode }) {
  return session.role === role ? <>{children}</> : <Navigate to={HOME[session.role] ?? "/login"} replace />;
}

function NotFound() {
  return (
    <div className="py-24 text-center">
      <p className="text-5xl font-bold text-primary">404</p>
      <p className="mt-3 text-fg-muted">That page doesn't exist.</p>
    </div>
  );
}

/** Any 401 from the API (e.g. the session expired) sends the user back to the login page. */
function useSessionExpiry() {
  const qc = useQueryClient();
  const navigate = useNavigate();
  useEffect(() => {
    const onUnauthorized = () => {
      qc.clear();
      qc.setQueryData(["session"], null);
      navigate("/login", { replace: true });
    };
    window.addEventListener(UNAUTHORIZED_EVENT, onUnauthorized);
    return () => window.removeEventListener(UNAUTHORIZED_EVENT, onUnauthorized);
  }, [qc, navigate]);
}

export default function App() {
  const { data: session, isLoading } = useSession();
  useSessionExpiry();

  if (isLoading) return <Spinner className="min-h-screen" />;
  if (!session) {
    return (
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }
  if (session.mustChangePassword) return <ForcedPasswordChange session={session} />;

  return (
    <TermProvider>
      <AppShell session={session}>
        <Suspense fallback={<Spinner />}>
        <Routes>
          <Route path="/" element={<Navigate to={HOME[session.role]} replace />} />
          <Route path="/login" element={<Navigate to={HOME[session.role]} replace />} />
          <Route path="/account/password" element={<ChangePasswordPage session={session} />} />

          <Route path="/student" element={<Only role="student" session={session}><StudentDashboard session={session} /></Only>} />
          <Route path="/student/catalog" element={<Only role="student" session={session}><CatalogPage session={session} /></Only>} />
          <Route path="/student/registrations" element={<Only role="student" session={session}><RegistrationsPage session={session} /></Only>} />
          <Route path="/student/timetable" element={<Only role="student" session={session}><StudentTimetablePage session={session} /></Only>} />
          <Route path="/student/grades" element={<Only role="student" session={session}><GradesPage session={session} /></Only>} />

          <Route path="/instructor" element={<Only role="instructor" session={session}><InstructorDashboard /></Only>} />
          <Route path="/instructor/sections" element={<Only role="instructor" session={session}><InstructorSectionsPage /></Only>} />
          <Route path="/instructor/sections/:sectionId" element={<Only role="instructor" session={session}><SectionPage /></Only>} />
          <Route path="/instructor/timetable" element={<Only role="instructor" session={session}><InstructorTimetablePage /></Only>} />

          <Route path="/admin" element={<Only role="admin" session={session}><AdminDashboard /></Only>} />
          <Route path="/admin/accounts" element={<Only role="admin" session={session}><AccountsPage /></Only>} />
          <Route path="/admin/students" element={<Only role="admin" session={session}><StudentsPage /></Only>} />
          <Route path="/admin/instructors" element={<Only role="admin" session={session}><InstructorsPage /></Only>} />
          <Route path="/admin/courses" element={<Only role="admin" session={session}><CoursesPage /></Only>} />
          <Route path="/admin/sections" element={<Only role="admin" session={session}><SectionsPage /></Only>} />
          <Route path="/admin/rooms" element={<Only role="admin" session={session}><RoomsPage /></Only>} />
          <Route path="/admin/departments" element={<Only role="admin" session={session}><DepartmentsPage /></Only>} />
          <Route path="/admin/programs" element={<Only role="admin" session={session}><ProgramsPage /></Only>} />

          <Route path="*" element={<NotFound />} />
        </Routes>
        </Suspense>
      </AppShell>
    </TermProvider>
  );
}
