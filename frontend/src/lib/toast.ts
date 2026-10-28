import { toast } from "sonner";
import { errorMessage } from "./api";

export const notify = {
  success: (message: string) => toast.success(message),
  error: (err: unknown) => toast.error(errorMessage(err)),
};
