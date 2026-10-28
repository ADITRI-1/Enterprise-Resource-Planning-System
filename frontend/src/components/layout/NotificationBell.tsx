import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { AlertTriangle, Bell, CheckCheck, ClipboardList, GraduationCap } from "lucide-react";
import { api, type Page, type Schemas } from "@/lib/api";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";

type Item = Schemas["NotificationView"];

const ICONS: Record<string, typeof Bell> = { REGISTRATION: ClipboardList, GRADES: GraduationCap, MAINTENANCE: AlertTriangle };

function timeAgo(iso?: string) {
  if (!iso) return "";
  const s = Math.max(0, (Date.now() - new Date(iso).getTime()) / 1000);
  if (s < 60) return "just now";
  if (s < 3600) return `${Math.floor(s / 60)}m ago`;
  if (s < 86400) return `${Math.floor(s / 3600)}h ago`;
  return `${Math.floor(s / 86400)}d ago`;
}

export function NotificationBell() {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  const qc = useQueryClient();
  const navigate = useNavigate();

  const count = useQuery({
    queryKey: ["notifications", "count"],
    queryFn: () => api<{ count: number }>("/api/notifications/unread-count"),
    refetchInterval: 30_000,
  });
  const list = useQuery({
    queryKey: ["notifications", "list"],
    queryFn: () => api<Page<Item>>("/api/notifications", { params: { size: 15 } }),
    enabled: open,
  });
  const refresh = () => qc.invalidateQueries({ queryKey: ["notifications"] });
  const markRead = useMutation({ mutationFn: (id: string) => api(`/api/notifications/${id}/read`, { method: "POST" }), onSuccess: refresh });
  const markAll = useMutation({ mutationFn: () => api("/api/notifications/read-all", { method: "POST" }), onSuccess: refresh });

  useEffect(() => {
    const close = (e: MouseEvent) => ref.current && !ref.current.contains(e.target as Node) && setOpen(false);
    document.addEventListener("mousedown", close);
    return () => document.removeEventListener("mousedown", close);
  }, []);

  const unread = count.data?.count ?? 0;
  const open_ = (n: Item) => {
    if (!n.read) markRead.mutate(n.notificationId!);
    setOpen(false);
    if (n.link) navigate(n.link);
  };

  return (
    <div ref={ref} className="relative">
      <Button
        variant="ghost"
        size="icon"
        className="relative text-white/90 hover:bg-white/10 hover:text-white"
        onClick={() => setOpen((o) => !o)}
        aria-label={unread ? `Notifications, ${unread} unread` : "Notifications"}
        aria-expanded={open}
      >
        <Bell className="h-5 w-5" />
        {unread > 0 && (
          <span className="absolute -right-0.5 -top-0.5 flex h-[18px] min-w-[18px] items-center justify-center rounded-full bg-danger px-1 text-[10px] font-bold text-white ring-2 ring-brand">
            {unread > 9 ? "9+" : unread}
          </span>
        )}
      </Button>
      {open && (
        <div className="absolute right-0 top-full z-40 mt-2 w-[min(22rem,calc(100vw-2rem))] overflow-hidden rounded-xl border border-border bg-surface shadow-xl">
          <div className="flex items-center justify-between border-b border-border px-4 py-3">
            <p className="font-semibold">Notifications</p>
            <Button variant="ghost" size="sm" disabled={!unread} onClick={() => markAll.mutate()}>
              <CheckCheck className="h-4 w-4" /> Mark all read
            </Button>
          </div>
          <ul className="max-h-[60vh] divide-y divide-border overflow-y-auto">
            {list.isLoading && <li className="px-4 py-6 text-center text-sm text-fg-muted">Loading…</li>}
            {list.data?.items.length === 0 && <li className="px-4 py-8 text-center text-sm text-fg-muted">You're all caught up.</li>}
            {list.data?.items.map((n) => {
              const Icon = ICONS[n.type ?? ""] ?? Bell;
              return (
                <li key={n.notificationId}>
                  <button onClick={() => open_(n)} className={cn("flex w-full gap-3 px-4 py-3 text-left hover:bg-muted/60", !n.read && "bg-primary/5")}>
                    <span className={cn("mt-0.5 rounded-lg p-1.5", n.type === "MAINTENANCE" ? "bg-warning/15 text-warning" : n.type === "GRADES" ? "bg-success/10 text-success" : "bg-primary/10 text-primary")}>
                      <Icon className="h-4 w-4" />
                    </span>
                    <span className="min-w-0 flex-1">
                      <span className="flex items-start justify-between gap-2">
                        <span className={cn("text-sm", !n.read && "font-semibold")}>{n.title}</span>
                        {!n.read && <span className="mt-1.5 h-2 w-2 shrink-0 rounded-full bg-primary" aria-label="unread" />}
                      </span>
                      <span className="mt-0.5 block text-xs text-fg-muted">{n.message}</span>
                      <span className="mt-1 block text-[11px] text-fg-muted/80">{timeAgo(n.createdAt)}</span>
                    </span>
                  </button>
                </li>
              );
            })}
          </ul>
        </div>
      )}
    </div>
  );
}
