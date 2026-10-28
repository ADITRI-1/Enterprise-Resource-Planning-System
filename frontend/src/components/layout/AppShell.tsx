import { useEffect, useRef, useState, type ReactNode } from "react";
import { NavLink, useLocation, useNavigate } from "react-router-dom";
import { AlertTriangle, ChevronDown, KeyRound, LogOut, Menu, Moon, Sun, X } from "lucide-react";
import { cn, initials, titleCase } from "@/lib/utils";
import { useInstructorProfile, useLogout, useMaintenance, useStudentProfile, type Session } from "@/lib/session";
import { useTheme } from "@/lib/theme";
import { Avatar } from "@/components/ui/misc";
import { Button } from "@/components/ui/button";
import { NAV } from "./nav";

export function BrandMark({ className }: { className?: string }) {
  return (
    <div className={cn("flex items-center gap-3", className)}>
      <div className="flex h-8 items-end gap-1" aria-hidden>
        {[0.55, 1, 0.75, 0.9].map((h, i) => (
          <span key={i} className={cn("w-1.5 rounded-full", i % 2 ? "bg-white" : "bg-accent")} style={{ height: `${h * 100}%` }} />
        ))}
      </div>
      <div className="leading-tight">
        <p className="text-[15px] font-semibold tracking-tight text-white">University ERP</p>
        <p className="text-[11px] font-medium uppercase tracking-[0.14em] text-accent">Academic portal</p>
      </div>
    </div>
  );
}

export function MaintenanceBanner() {
  const { data } = useMaintenance();
  if (!data?.maintenanceOn) return null;
  return (
    <div role="status" className="flex items-center justify-center gap-2 bg-warning px-4 py-2 text-center text-sm font-medium text-white">
      <AlertTriangle className="h-4 w-4 shrink-0" />
      {data.bannerMessage ?? "Maintenance mode is on. You can view data, but changes are blocked."}
    </div>
  );
}

function useDisplayName(session: Session) {
  const student = useStudentProfile(session.role === "student" && !session.mustChangePassword);
  const instructor = useInstructorProfile(session.role === "instructor" && !session.mustChangePassword);
  return student.data?.name ?? instructor.data?.name ?? session.username;
}

function UserMenu({ session, name }: { session: Session; name: string }) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  const logout = useLogout();
  const navigate = useNavigate();

  useEffect(() => {
    const close = (e: MouseEvent) => ref.current && !ref.current.contains(e.target as Node) && setOpen(false);
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, []);

  return (
    <div ref={ref} className="relative">
      <button
        onClick={() => setOpen((o) => !o)}
        className="flex items-center gap-2 rounded-lg px-2 py-1.5 text-white/90 hover:bg-white/10"
        aria-haspopup="menu"
        aria-expanded={open}
      >
        <Avatar text={initials(name)} className="h-8 w-8 bg-white/15 text-white" />
        <span className="hidden text-sm font-medium sm:block">{name}</span>
        <ChevronDown className="hidden h-4 w-4 sm:block" />
      </button>
      {open && (
        <div role="menu" className="absolute right-0 top-full z-40 mt-2 w-56 overflow-hidden rounded-xl border border-border bg-surface py-1 shadow-xl">
          <div className="border-b border-border px-4 py-3">
            <p className="truncate text-sm font-medium">{name}</p>
            <p className="text-xs text-fg-muted">{session.username} · {titleCase(session.role)}</p>
          </div>
          <button role="menuitem" className="flex w-full items-center gap-2 px-4 py-2 text-sm hover:bg-muted" onClick={() => { setOpen(false); navigate("/account/password"); }}>
            <KeyRound className="h-4 w-4" /> Change password
          </button>
          <button role="menuitem" className="flex w-full items-center gap-2 px-4 py-2 text-sm text-danger hover:bg-muted" onClick={() => logout.mutate(undefined, { onSettled: () => navigate("/login") })}>
            <LogOut className="h-4 w-4" /> Log out
          </button>
        </div>
      )}
    </div>
  );
}

export function AppShell({ session, children }: { session: Session; children: ReactNode }) {
  const [drawer, setDrawer] = useState(false);
  const { dark, toggle } = useTheme();
  const location = useLocation();
  const name = useDisplayName(session);
  const items = NAV[session.role] ?? [];

  useEffect(() => setDrawer(false), [location.pathname]);

  const nav = (
    <nav className="space-y-1 p-3" aria-label="Main">
      {items.map(({ to, label, icon: Icon }) => (
        <NavLink
          key={to}
          to={to}
          end={to.split("/").length === 2}
          className={({ isActive }) =>
            cn(
              "flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors",
              isActive ? "bg-primary/10 text-primary" : "text-fg-muted hover:bg-muted hover:text-fg",
            )
          }
        >
          <Icon className="h-[18px] w-[18px]" />
          {label}
        </NavLink>
      ))}
    </nav>
  );

  const profile = (
    <div className="m-3 flex items-center gap-3 rounded-xl bg-muted p-3">
      <Avatar text={initials(name)} />
      <div className="min-w-0">
        <p className="truncate text-sm font-semibold">{name}</p>
        <p className="text-xs text-fg-muted">{titleCase(session.role)}</p>
      </div>
    </div>
  );

  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-30 bg-brand shadow-md">
        <div className="flex h-16 items-center gap-3 px-4 lg:px-6">
          <Button variant="ghost" size="icon" className="text-white hover:bg-white/10 hover:text-white lg:hidden" onClick={() => setDrawer(true)} aria-label="Open menu">
            <Menu className="h-5 w-5" />
          </Button>
          <BrandMark />
          <div className="ml-auto flex items-center gap-1">
            <Button variant="ghost" size="icon" className="text-white/90 hover:bg-white/10 hover:text-white" onClick={toggle} aria-label={dark ? "Switch to light mode" : "Switch to dark mode"}>
              {dark ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
            </Button>
            <UserMenu session={session} name={name} />
          </div>
        </div>
        <MaintenanceBanner />
      </header>

      <div className="flex flex-1">
        <aside className="sticky top-16 hidden h-[calc(100vh-4rem)] w-64 shrink-0 overflow-y-auto border-r border-border bg-surface lg:block">
          {profile}
          {nav}
        </aside>

        {drawer && (
          <div className="fixed inset-0 z-40 lg:hidden">
            <div className="absolute inset-0 bg-black/40" onClick={() => setDrawer(false)} aria-hidden />
            <aside className="absolute inset-y-0 left-0 w-72 overflow-y-auto bg-surface shadow-2xl">
              <div className="flex items-center justify-between bg-brand px-4 py-4">
                <BrandMark />
                <Button variant="ghost" size="icon" className="text-white hover:bg-white/10 hover:text-white" onClick={() => setDrawer(false)} aria-label="Close menu">
                  <X className="h-5 w-5" />
                </Button>
              </div>
              {profile}
              {nav}
            </aside>
          </div>
        )}

        <main className="min-w-0 flex-1 px-4 py-6 sm:px-6 lg:px-8 lg:py-8">
          <div className="mx-auto max-w-7xl">{children}</div>
        </main>
      </div>
    </div>
  );
}
