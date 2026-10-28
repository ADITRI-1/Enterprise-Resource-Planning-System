import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "node:path";

// The dev server proxies /api to Spring Boot so the session cookie is same-origin.
export default defineConfig({
  plugins: [react()],
  resolve: { alias: { "@": path.resolve(__dirname, "src") } },
  server: {
    port: 5173,
    proxy: {
      "/api": { target: "http://localhost:8080", changeOrigin: false },
    },
  },
});
