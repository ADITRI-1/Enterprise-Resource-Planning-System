import { createContext, useContext, useState, type ReactNode } from "react";
import { currentTerm, type Term } from "./utils";

const TermContext = createContext<{ term: Term; setTerm: (t: Term) => void } | null>(null);

/** The term being browsed, shared across pages so switching it on one page sticks. */
export function TermProvider({ children }: { children: ReactNode }) {
  const [term, setTerm] = useState<Term>(currentTerm);
  return <TermContext.Provider value={{ term, setTerm }}>{children}</TermContext.Provider>;
}

export function useTerm() {
  const ctx = useContext(TermContext);
  if (!ctx) throw new Error("useTerm must be used inside TermProvider");
  return ctx;
}
