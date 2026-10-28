import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api, errorMessage } from "@/lib/api";
import { notify } from "@/lib/toast";
import { Button } from "@/components/ui/button";
import { Spinner } from "@/components/ui/misc";
import { Modal } from "@/components/ui/modal";
import type { Option } from "./CrudPage";

/** Pick which courses must be passed before registering for `courseId`. */
export function PrerequisitesDialog({ courseId, courses, onClose }: { courseId: string; courses: Option[]; onClose: () => void }) {
  const qc = useQueryClient();
  const current = useQuery({ queryKey: ["admin", "prereq", courseId], queryFn: () => api<string[]>(`/api/admin/courses/${courseId}/prerequisites`) });
  const [selected, setSelected] = useState<Set<string> | null>(null);
  useEffect(() => { if (current.data && !selected) setSelected(new Set(current.data)); }, [current.data, selected]);

  const save = useMutation({
    mutationFn: () => api(`/api/admin/courses/${courseId}/prerequisites`, { method: "PUT", body: { prerequisites: [...(selected ?? [])] } }),
    onSuccess: () => { notify.success(`Prerequisites for ${courseId} saved`); qc.invalidateQueries({ queryKey: ["admin"] }); qc.invalidateQueries({ queryKey: ["catalog"] }); onClose(); },
  });
  const toggle = (id: string) => setSelected((s) => { const n = new Set(s); n.has(id) ? n.delete(id) : n.add(id); return n; });

  return (
    <Modal
      open
      onClose={onClose}
      title={`Prerequisites · ${courseId}`}
      description="Students must have passed (any grade but F) every selected course before registering."
      footer={<><Button variant="outline" onClick={onClose}>Cancel</Button><Button onClick={() => save.mutate()} loading={save.isPending}>Save</Button></>}
    >
      {!selected ? <Spinner /> : (
        <div className="space-y-1">
          {courses.filter((c) => c.value !== courseId).map((c) => (
            <label key={c.value} className="flex cursor-pointer items-center gap-3 rounded-lg px-2 py-2 text-sm hover:bg-muted">
              <input type="checkbox" className="h-4 w-4 accent-[rgb(var(--primary))]" checked={selected.has(c.value)} onChange={() => toggle(c.value)} />
              {c.label}
            </label>
          ))}
          {save.error && <p role="alert" className="pt-2 text-sm text-danger">{errorMessage(save.error)}</p>}
        </div>
      )}
    </Modal>
  );
}
