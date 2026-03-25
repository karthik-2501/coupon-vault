"use client";

import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { apiFetch, getToken } from "@/lib/api";
import type { CouponDetail } from "@/lib/types";

export default function CouponDetailPage() {
  const params = useParams();
  const router = useRouter();
  const id = params.id as string;
  const [c, setC] = useState<CouponDetail | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [buyMsg, setBuyMsg] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const token = getToken();
        const data = await apiFetch<CouponDetail>(`/coupons/${id}`, { token });
        setC(data);
      } catch (e) {
        setErr(e instanceof Error ? e.message : "Not found");
      }
    })();
  }, [id]);

  async function buy() {
    setBuyMsg(null);
    const token = getToken();
    if (!token) {
      router.push("/auth/login");
      return;
    }
    try {
      await apiFetch<{ status: string }>("/transactions", {
        method: "POST",
        body: JSON.stringify({ couponId: id }),
        token,
      });
      setBuyMsg("Purchase complete! Code revealed below.");
      const data = await apiFetch<CouponDetail>(`/coupons/${id}`, { token });
      setC(data);
    } catch (e) {
      setBuyMsg(e instanceof Error ? e.message : "Purchase failed");
    }
  }

  if (err || !c) {
    return (
      <div className="text-center">
        <p className="text-red-600">{err || "Loading…"}</p>
        <Link href="/marketplace" className="mt-4 inline-block text-vault-700 underline">
          Back to marketplace
        </Link>
      </div>
    );
  }

  const hidden = c.code === "[hidden]";

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <Link href="/marketplace" className="text-sm text-vault-700 hover:underline">
        ← Marketplace
      </Link>
      <div className="rounded-2xl border border-slate-200 bg-white p-8 shadow">
        <div className="flex items-center gap-4">
          <div className="flex h-16 w-16 items-center justify-center rounded-xl bg-vault-100 text-2xl font-bold text-vault-800">
            {c.store.name.slice(0, 1)}
          </div>
          <div>
            <h1 className="text-2xl font-bold text-vault-900">{c.store.name}</h1>
            <p className="text-sm text-slate-500">{c.store.websiteUrl}</p>
          </div>
        </div>
        <p className="mt-6 text-slate-700">{c.description}</p>
        <dl className="mt-6 grid gap-3 text-sm sm:grid-cols-2">
          <div>
            <dt className="text-slate-500">Status</dt>
            <dd className="font-medium">{c.status}</dd>
          </div>
          <div>
            <dt className="text-slate-500">Discount</dt>
            <dd className="font-medium">
              {c.discountType === "PERCENTAGE"
                ? `${c.discountValue}%`
                : c.discountType === "FIXED"
                  ? `$${c.discountValue}`
                  : "Free shipping"}
            </dd>
          </div>
          <div>
            <dt className="text-slate-500">Min order</dt>
            <dd>{c.minOrderValue != null ? `$${c.minOrderValue}` : "—"}</dd>
          </div>
          <div>
            <dt className="text-slate-500">Expires</dt>
            <dd>{c.expiryDate}</dd>
          </div>
          <div>
            <dt className="text-slate-500">Price</dt>
            <dd className="text-lg font-semibold">${Number(c.price).toFixed(2)}</dd>
          </div>
          <div>
            <dt className="text-slate-500">Seller</dt>
            <dd>{c.seller.name}</dd>
          </div>
        </dl>

        <div className="mt-8 rounded-xl bg-slate-50 p-4">
          <p className="text-xs font-medium uppercase text-slate-500">Coupon code</p>
          <p className="mt-1 font-mono text-xl tracking-wider">
            {hidden ? "••••••••" : c.code}
          </p>
        </div>

        {c.status === "VALID" && (
          <button
            type="button"
            onClick={buy}
            className="mt-6 w-full rounded-xl bg-vault-600 py-3 font-semibold text-white hover:bg-vault-700"
          >
            Buy now (mock payment)
          </button>
        )}
        {buyMsg && <p className="mt-4 text-center text-sm text-slate-700">{buyMsg}</p>}
      </div>
    </div>
  );
}
