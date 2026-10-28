import { useState } from "react";
import { Check, Copy, KeyRound } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Modal } from "@/components/ui/modal";

/** Shows a one-time temporary password; it's never retrievable again. */
export function TempPasswordDialog({ value, onClose }: { value: { username: string; password: string } | null; onClose: () => void }) {
  const [copied, setCopied] = useState(false);
  if (!value) return null;
  const copy = async () => {
    await navigator.clipboard?.writeText(value.password);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };
  return (
    <Modal open onClose={onClose} title="Temporary password" footer={<Button onClick={onClose}>Done</Button>}>
      <div className="space-y-4 text-sm">
        <p className="text-fg-muted">
          Share these sign-in details with the user. They'll be asked to choose their own password the first time they sign in.
          <b className="text-fg"> This password won't be shown again.</b>
        </p>
        <dl className="space-y-3 rounded-xl bg-muted p-4">
          <div className="flex items-center justify-between gap-3">
            <dt className="text-fg-muted">Username</dt>
            <dd className="font-mono font-medium">{value.username}</dd>
          </div>
          <div className="flex items-center justify-between gap-3">
            <dt className="flex items-center gap-1.5 text-fg-muted"><KeyRound className="h-4 w-4" /> Password</dt>
            <dd className="flex items-center gap-2">
              <span className="font-mono text-base font-semibold tracking-wider">{value.password}</span>
              <Button variant="ghost" size="icon" onClick={copy} aria-label="Copy password">
                {copied ? <Check className="h-4 w-4 text-success" /> : <Copy className="h-4 w-4" />}
              </Button>
            </dd>
          </div>
        </dl>
      </div>
    </Modal>
  );
}
