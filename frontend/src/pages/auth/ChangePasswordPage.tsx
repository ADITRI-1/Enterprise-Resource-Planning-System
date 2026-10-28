import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Check, ShieldCheck, X } from "lucide-react";
import { api, errorMessage } from "@/lib/api";
import { useLogout, type Session } from "@/lib/session";
import { notify } from "@/lib/toast";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Card, Field, PageHeader } from "@/components/ui/misc";
import { BrandMark } from "@/components/layout/AppShell";

/** Mirrors PasswordPolicy on the server so users see the rules as they type. */
function rules(password: string, username: string) {
  return [
    { ok: password.length >= 8, text: "At least 8 characters" },
    { ok: /[a-z]/i.test(password) && /\d/.test(password), text: "Contains a letter and a digit" },
    { ok: !!password && !password.toLowerCase().includes(username.toLowerCase()), text: "Doesn't contain your username" },
  ];
}

function PasswordForm({ session, onDone }: { session: Session; onDone: () => void }) {
  const [current, setCurrent] = useState("");
  const [next, setNext] = useState("");
  const [confirm, setConfirm] = useState("");
  const qc = useQueryClient();
  const change = useMutation({
    mutationFn: () => api("/api/auth/change-password", { body: { currentPassword: current, newPassword: next } }),
    onSuccess: async () => {
      notify.success("Password changed");
      await qc.invalidateQueries({ queryKey: ["session"] });
      onDone();
    },
  });
  const checks = rules(next, session.username);
  const valid = checks.every((c) => c.ok) && next === confirm && !!current;

  const submit = (e: FormEvent) => {
    e.preventDefault();
    if (valid) change.mutate();
  };

  return (
    <form onSubmit={submit} className="space-y-4">
      <Field label={session.mustChangePassword ? "Temporary password" : "Current password"}>
        <input className="input" type="password" autoComplete="current-password" value={current} onChange={(e) => setCurrent(e.target.value)} required />
      </Field>
      <Field label="New password">
        <input className="input" type="password" autoComplete="new-password" value={next} onChange={(e) => setNext(e.target.value)} required />
      </Field>
      <ul className="space-y-1 text-sm">
        {checks.map((c) => (
          <li key={c.text} className={cn("flex items-center gap-2", c.ok ? "text-success" : "text-fg-muted")}>
            {c.ok ? <Check className="h-4 w-4" /> : <X className="h-4 w-4" />} {c.text}
          </li>
        ))}
        <li className="flex items-center gap-2 text-fg-muted">
          <span className="w-4" /> Can't be one of your last 3 passwords
        </li>
      </ul>
      <Field label="Confirm new password" error={confirm && confirm !== next ? "Passwords don't match" : undefined}>
        <input className="input" type="password" autoComplete="new-password" value={confirm} onChange={(e) => setConfirm(e.target.value)} required />
      </Field>
      {change.error && <p role="alert" className="text-sm text-danger">{errorMessage(change.error)}</p>}
      <Button type="submit" className="w-full" disabled={!valid} loading={change.isPending}>Change password</Button>
    </form>
  );
}

/** Full-screen step shown after logging in with a temporary password. */
export function ForcedPasswordChange({ session }: { session: Session }) {
  const logout = useLogout();
  return (
    <div className="flex min-h-screen items-center justify-center bg-bg px-4 py-12">
      <div className="w-full max-w-md">
        <div className="mb-6 rounded-xl bg-brand p-4"><BrandMark /></div>
        <Card className="p-6">
          <div className="mb-5 flex items-start gap-3">
            <div className="rounded-lg bg-primary/10 p-2 text-primary"><ShieldCheck className="h-5 w-5" /></div>
            <div>
              <h1 className="text-lg font-semibold">Choose your own password</h1>
              <p className="mt-1 text-sm text-fg-muted">
                You signed in with a temporary password, {session.username}. Set a new one to continue.
              </p>
            </div>
          </div>
          <PasswordForm session={session} onDone={() => undefined} />
          <button className="mt-4 w-full text-center text-sm text-fg-muted hover:text-fg" onClick={() => logout.mutate()}>
            Log out instead
          </button>
        </Card>
      </div>
    </div>
  );
}

export function ChangePasswordPage({ session }: { session: Session }) {
  const navigate = useNavigate();
  return (
    <>
      <PageHeader title="Change password" description="You'll stay signed in on this device." />
      <Card className="max-w-md p-6">
        <PasswordForm session={session} onDone={() => navigate("/")} />
      </Card>
    </>
  );
}
