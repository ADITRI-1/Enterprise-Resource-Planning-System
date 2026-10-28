import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { AlertCircle, Eye, EyeOff, Lock } from "lucide-react";
import { ApiError } from "@/lib/api";
import { useLogin } from "@/lib/session";
import { Button } from "@/components/ui/button";
import { Field } from "@/components/ui/misc";
import { BrandMark, MaintenanceBanner } from "@/components/layout/AppShell";

const DEMO = [
  { label: "Admin", username: "admin1", password: "Admin@123" },
  { label: "Instructor", username: "inst1", password: "Inst@123" },
  { label: "Student", username: "stu1", password: "Stu@123" },
];

function loginErrorText(err: unknown) {
  if (!(err instanceof ApiError)) return "Can't reach the server. Is the backend running?";
  if (err.status === 423) {
    const until = typeof err.details.lockedUntil === "string" ? new Date(err.details.lockedUntil) : null;
    return `Too many failed attempts. Try again${until ? ` after ${until.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}` : " later"} or ask an admin to unlock your account.`;
  }
  const left = err.details.remainingAttempts;
  if (typeof left === "number" && left <= 3) {
    return `Incorrect username or password. ${left} attempt${left === 1 ? "" : "s"} left before your account is locked.`;
  }
  return err.message;
}

export function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [show, setShow] = useState(false);
  const login = useLogin();
  const navigate = useNavigate();

  const submit = (e: FormEvent) => {
    e.preventDefault();
    login.mutate({ username: username.trim(), password }, { onSuccess: () => navigate("/", { replace: true }) });
  };

  return (
    <div className="flex min-h-screen flex-col">
      <MaintenanceBanner />
      <div className="grid flex-1 lg:grid-cols-[1.1fr_1fr]">
        <section className="relative hidden overflow-hidden bg-brand p-12 lg:flex lg:flex-col lg:justify-between">
          <div className="absolute -right-24 -top-24 h-96 w-96 rounded-full bg-accent/20 blur-3xl" aria-hidden />
          <div className="absolute -bottom-32 -left-16 h-96 w-96 rounded-full bg-primary/25 blur-3xl" aria-hidden />
          <BrandMark className="relative" />
          <div className="relative max-w-md">
            <h1 className="text-4xl font-semibold leading-tight tracking-tight text-white">
              Courses, timetables and grades, all in one place.
            </h1>
            <p className="mt-4 text-white/70">
              Register for sections, track your weekly schedule, publish grades and run the semester, from any device.
            </p>
          </div>
          <p className="relative text-xs text-white/50">© {new Date().getFullYear()} University ERP</p>
        </section>

        <section className="flex items-center justify-center px-4 py-12 sm:px-8">
          <div className="w-full max-w-sm">
            <div className="mb-8 rounded-xl bg-brand p-4 lg:hidden">
              <BrandMark />
            </div>
            <h2 className="text-2xl font-semibold tracking-tight">Sign in</h2>
            <p className="mt-1 text-sm text-fg-muted">Use the username and password from your administrator.</p>

            <form onSubmit={submit} className="mt-8 space-y-4">
              <Field label="Username">
                <input className="input" autoComplete="username" autoFocus value={username} onChange={(e) => setUsername(e.target.value)} required />
              </Field>
              <Field label="Password">
                <div className="relative">
                  <input className="input pr-10" type={show ? "text" : "password"} autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} required />
                  <button type="button" onClick={() => setShow((s) => !s)} className="absolute right-2 top-1/2 -translate-y-1/2 rounded p-1 text-fg-muted hover:text-fg" aria-label={show ? "Hide password" : "Show password"}>
                    {show ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                  </button>
                </div>
              </Field>

              {login.error && (
                <div role="alert" className="flex gap-2 rounded-lg border border-danger/30 bg-danger/10 p-3 text-sm text-danger">
                  {login.error instanceof ApiError && login.error.status === 423 ? <Lock className="mt-0.5 h-4 w-4 shrink-0" /> : <AlertCircle className="mt-0.5 h-4 w-4 shrink-0" />}
                  {loginErrorText(login.error)}
                </div>
              )}

              <Button type="submit" className="w-full" loading={login.isPending}>Sign in</Button>
            </form>

            {import.meta.env.DEV && (
              <div className="mt-8 rounded-xl border border-dashed border-border p-4">
                <p className="text-xs font-medium uppercase tracking-wide text-fg-muted">Demo accounts</p>
                <div className="mt-3 grid grid-cols-3 gap-2">
                  {DEMO.map((d) => (
                    <Button key={d.username} type="button" variant="outline" size="sm" onClick={() => { setUsername(d.username); setPassword(d.password); }}>
                      {d.label}
                    </Button>
                  ))}
                </div>
              </div>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
