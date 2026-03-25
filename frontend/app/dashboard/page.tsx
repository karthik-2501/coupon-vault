import { Suspense } from "react";
import { DashboardClient } from "./DashboardClient";

export default function DashboardPage() {
  return (
    <Suspense
      fallback={
        <div className="flex min-h-[40vh] items-center justify-center text-slate-500">
          Loading dashboard…
        </div>
      }
    >
      <DashboardClient />
    </Suspense>
  );
}
