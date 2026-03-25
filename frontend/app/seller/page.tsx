"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function SellerDashboardPage() {
  const router = useRouter();

  useEffect(() => {
    router.replace("/dashboard?tab=selling");
  }, [router]);

  return (
    <p className="text-center text-slate-500">Redirecting to dashboard…</p>
  );
}
