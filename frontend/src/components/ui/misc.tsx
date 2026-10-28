import type { ReactNode } from "react";
import { Inbox, Loader2 } from "lucide-react";
import { cn } from "@/lib/utils";

type Tone = "neutral" | "primary" | "success" | "warning" | "danger" | "accent";
const tones: Record<Tone, string> = {
  neutral: "bg-muted text-fg-muted",
  primary: "bg-primary/10 text-primary",
  success: "bg-success/10 text-success",
  warning: "bg-warning/15 text-warning",
  danger: "bg-danger/10 text-danger",
  accent: "bg-accent/15 text-accent",
};

export function Badge({ tone = "neutral", children, className }: { tone?: Tone; children: ReactNode; className?: string }) {
  return (
    <span className={cn("inline-flex items-center gap-1 whitespace-nowrap rounded-full px-2.5 py-0.5 text-xs font-medium", tones[tone], className)}>
      {children}
    </span>
  );
}

export function Card({ className, children }: { className?: string; children: ReactNode }) {
  return <div className={cn("card", className)}>{children}</div>;
}

export function CardHeader({ title, description, action }: { title: ReactNode; description?: ReactNode; action?: ReactNode }) {
  return (
    <div className="flex flex-wrap items-start justify-between gap-3 border-b border-border px-5 py-4">
      <div>
        <h2 className="text-base font-semibold">{title}</h2>
        {description && <p className="mt-0.5 text-sm text-fg-muted">{description}</p>}
      </div>
      {action}
    </div>
  );
}

export function PageHeader({ title, description, actions }: { title: string; description?: ReactNode; actions?: ReactNode }) {
  return (
    <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
        {description && <p className="mt-1 text-sm text-fg-muted">{description}</p>}
      </div>
      {actions && <div className="flex flex-wrap items-center gap-2">{actions}</div>}
    </div>
  );
}

export function Spinner({ label = "Loading…", className }: { label?: string; className?: string }) {
  return (
    <div role="status" className={cn("flex items-center justify-center gap-2 py-12 text-sm text-fg-muted", className)}>
      <Loader2 className="h-5 w-5 animate-spin" aria-hidden />
      {label}
    </div>
  );
}

export function EmptyState({ title, description, icon, action }: { title: string; description?: string; icon?: ReactNode; action?: ReactNode }) {
  return (
    <div className="flex flex-col items-center justify-center px-6 py-12 text-center">
      <div className="mb-3 rounded-full bg-muted p-3 text-fg-muted">{icon ?? <Inbox className="h-6 w-6" />}</div>
      <p className="font-medium">{title}</p>
      {description && <p className="mt-1 max-w-sm text-sm text-fg-muted">{description}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}

export function StatCard({ label, value, hint, icon, tone = "primary" }: { label: string; value: ReactNode; hint?: ReactNode; icon?: ReactNode; tone?: Tone }) {
  return (
    <div className="card flex items-start gap-4 p-5">
      {icon && <div className={cn("rounded-lg p-2.5", tones[tone])}>{icon}</div>}
      <div className="min-w-0">
        <p className="text-sm text-fg-muted">{label}</p>
        <p className="mt-1 text-2xl font-semibold tabular-nums">{value}</p>
        {hint && <p className="mt-1 text-xs text-fg-muted">{hint}</p>}
      </div>
    </div>
  );
}

export function Field({ label, hint, error, children, className }: { label: string; hint?: string; error?: string; children: ReactNode; className?: string }) {
  return (
    <label className={cn("block space-y-1.5", className)}>
      <span className="text-sm font-medium">{label}</span>
      {children}
      {error ? <span className="block text-xs text-danger">{error}</span> : hint && <span className="block text-xs text-fg-muted">{hint}</span>}
    </label>
  );
}

export function Tabs<T extends string>({ tabs, value, onChange }: { tabs: { id: T; label: string }[]; value: T; onChange: (id: T) => void }) {
  return (
    <div role="tablist" className="flex gap-1 overflow-x-auto border-b border-border">
      {tabs.map((t) => (
        <button
          key={t.id}
          role="tab"
          aria-selected={value === t.id}
          onClick={() => onChange(t.id)}
          className={cn(
            "-mb-px whitespace-nowrap border-b-2 px-4 py-2.5 text-sm font-medium transition-colors",
            value === t.id ? "border-primary text-primary" : "border-transparent text-fg-muted hover:text-fg",
          )}
        >
          {t.label}
        </button>
      ))}
    </div>
  );
}

export function Avatar({ text, className }: { text: string; className?: string }) {
  return (
    <div className={cn("flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-accent/20 text-sm font-semibold text-accent", className)}>
      {text}
    </div>
  );
}
