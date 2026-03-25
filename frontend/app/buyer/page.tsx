"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function BuyerDashboardPage() {
  const router = useRouter();

  useEffect(() => {
    router.replace("/dashboard?tab=purchases");
  }, [router]);

  return (
    <p className="text-center text-slate-500">Redirecting to dashboard…</p>
  );
}
