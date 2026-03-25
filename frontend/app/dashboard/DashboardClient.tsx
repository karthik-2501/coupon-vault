"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { useCallback, useEffect, useMemo, useState } from "react";
import { apiFetch, getToken } from "@/lib/api";
import type { CouponDetail } from "@/lib/types";

type Tab = "profile" | "purchases" | "selling" | "sold";

interface Me {
  authenticated: boolean;
  id?: string;
  name?: string;
  email?: string;
}

interface PurchaseRow {
  id: string;
  status: string;
  amount: number;
  createdAt: string;
  coupon: {
    id: string;
    code: string;
    description: string;
    storeName: string;
    expiryDate: string;
  };
}

interface SaleRow {
  id: string;
  status: string;
  amount: number;
  createdAt: string;
  buyer: { id: string; name: string };
  coupon: {
    id: string;
    code: string;
    description: string;
    storeName: string;
    expiryDate: string;
    price: number;
  };
}

const TABS: { id: Tab; label: string; desc: string }[] = [
  { id: "profile", label: "Profile", desc: "Your account" },
  { id: "purchases", label: "Purchased", desc: "Coupons you bought" },
  { id: "selling", label: "Selling", desc: "Listings still for sale" },
  { id: "sold", label: "Sold", desc: "Sales you completed" },
];

function isTab(s: string | null): s is Tab {
  return s === "profile" || s === "purchases" || s === "selling" || s === "sold";
}

export function DashboardClient() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const tabParam = searchParams.get("tab");
  const tab: Tab = isTab(tabParam) ? tabParam : "profile";

  const [me, setMe] = useState<Me | null>(null);
  const [purchases, setPurchases] = useState<PurchaseRow[]>([]);
  const [sellerCoupons, setSellerCoupons] = useState<
    (CouponDetail & { latestValidationMessage?: string })[]
  >([]);
  const [sales, setSales] = useState<SaleRow[]>([]);
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    const token = getToken();
    if (!token) {
      router.replace("/auth/login");
      return;
    }
    setErr(null);
    setLoading(true);
    try {
      const [meRes, buyRes, sellRes, saleRes] = await Promise.all([
        apiFetch<Me>("/auth/me", { token }),
        apiFetch<PurchaseRow[]>("/buyer/transactions", { token }),
        apiFetch<(CouponDetail & { latestValidationMessage?: string })[]>("/seller/coupons", {
          token,
        }),
        apiFetch<SaleRow[]>("/seller/transactions", { token }),
      ]);
      if (!meRes.authenticated) {
        localStorage.removeItem("cv_token");
        localStorage.removeItem("cv_name");
        router.replace("/auth/login");
        return;
      }
      setMe(meRes);
      setPurchases(buyRes);
      setSellerCoupons(sellRes);
      setSales(saleRes);
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Failed to load dashboard");
    } finally {
      setLoading(false);
    }
  }, [router]);

  useEffect(() => {
    load();
  }, [load]);

  const sellingListings = useMemo(
    () => sellerCoupons.filter((c) => c.status !== "SOLD"),
    [sellerCoupons]
  );

  const completedPurchases = useMemo(
    () => purchases.filter((p) => p.status === "COMPLETED"),
    [purchases]
  );

  const completedSales = useMemo(
    () => sales.filter((s) => s.status === "COMPLETED"),
    [sales]
  );

  function setTab(next: Tab) {
    router.replace(`/dashboard?tab=${next}`, { scroll: false });
  }

  async function removeListing(id: string) {
    if (!confirm("Remove this listing?")) return;
    const token = getToken();
    if (!token) return;
    try {
      await apiFetch(`/seller/coupons/${id}`, { method: "DELETE", token });
      await load();
    } catch (e) {
      alert(e instanceof Error ? e.message : "Delete failed");
    }
  }

  function statusBadge(status: string) {
    const colors: Record<string, string> = {
      PENDING_VALIDATION: "bg-amber-100 text-amber-900",
      VALID: "bg-emerald-100 text-emerald-900",
      INVALID: "bg-red-100 text-red-900",
      SOLD: "bg-slate-200 text-slate-800",
      EXPIRED: "bg-slate-100 text-slate-600",
      COMPLETED: "bg-emerald-100 text-emerald-900",
      PENDING: "bg-amber-100 text-amber-900",
      FAILED: "bg-red-100 text-red-900",
    };
    return (
      <span
        className={`rounded-full px-2 py-0.5 text-xs font-medium ${colors[status] || "bg-slate-100"}`}
      >
        {status}
      </span>
    );
  }

  if (loading && !me) {
    return (
      <div className="flex min-h-[40vh] items-center justify-center text-slate-500">
        Loading dashboard…
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-vault-900">My dashboard</h1>
          <p className="text-slate-600">
            Profile, purchases, listings for sale, and completed sales.
          </p>
        </div>
        <Link
          href="/seller/new"
          className="rounded-xl bg-vault-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-vault-700"
        >
          + New listing
        </Link>
      </div>

      {err && (
        <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
          {err}
        </div>
      )}

      <div className="flex flex-col gap-8 lg:flex-row">
        <nav className="flex shrink-0 flex-row flex-wrap gap-2 lg:w-56 lg:flex-col">
          {TABS.map((t) => (
            <button
              key={t.id}
              type="button"
              onClick={() => setTab(t.id)}
              className={`rounded-xl border px-4 py-3 text-left text-sm transition ${
                tab === t.id
                  ? "border-vault-600 bg-vault-50 text-vault-900"
                  : "border-slate-200 bg-white text-slate-700 hover:border-slate-300"
              }`}
            >
              <span className="font-semibold">{t.label}</span>
              <span className="mt-0.5 block text-xs text-slate-500">{t.desc}</span>
            </button>
          ))}
        </nav>

        <div className="min-h-[320px] flex-1 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          {tab === "profile" && me?.authenticated && (
            <div className="space-y-6">
              <h2 className="text-xl font-semibold text-vault-900">Profile</h2>
              <dl className="grid gap-4 sm:grid-cols-2">
                <div>
                  <dt className="text-xs font-medium uppercase text-slate-500">Name</dt>
                  <dd className="mt-1 text-lg text-slate-900">{me.name}</dd>
                </div>
                <div>
                  <dt className="text-xs font-medium uppercase text-slate-500">Email</dt>
                  <dd className="mt-1 text-lg text-slate-900">{me.email}</dd>
                </div>
                <div className="sm:col-span-2">
                  <dt className="text-xs font-medium uppercase text-slate-500">User ID</dt>
                  <dd className="mt-1 font-mono text-sm text-slate-600">{me.id}</dd>
                </div>
              </dl>
              <p className="text-sm text-slate-500">
                One account for buying and selling. Edit profile can be added later via a PATCH{" "}
                <code className="rounded bg-slate-100 px-1">/auth/me</code> endpoint.
              </p>
            </div>
          )}

          {tab === "purchases" && (
            <div className="space-y-4">
              <h2 className="text-xl font-semibold text-vault-900">Coupons you bought</h2>
              {completedPurchases.length === 0 ? (
                <p className="text-slate-500">
                  No completed purchases yet.{" "}
                  <Link href="/marketplace" className="text-vault-700 underline">
                    Browse marketplace
                  </Link>
                </p>
              ) : (
                <ul className="space-y-4">
                  {completedPurchases.map((r) => (
                    <li
                      key={r.id}
                      className="rounded-xl border border-slate-100 bg-slate-50/80 p-4"
                    >
                      <div className="flex flex-wrap items-start justify-between gap-4">
                        <div>
                          <p className="font-semibold text-vault-900">{r.coupon.storeName}</p>
                          <p className="text-sm text-slate-600">{r.coupon.description}</p>
                          <p className="mt-2 text-xs text-slate-500">
                            ${Number(r.amount).toFixed(2)} · {r.createdAt}
                          </p>
                        </div>
                        <div className="text-right">
                          <p className="text-xs uppercase text-slate-500">Code</p>
                          <p className="font-mono text-lg">{r.coupon.code}</p>
                          <p className="text-xs text-slate-500">Expires {r.coupon.expiryDate}</p>
                        </div>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}

          {tab === "selling" && (
            <div className="space-y-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <h2 className="text-xl font-semibold text-vault-900">Coupons you&apos;re selling</h2>
                <span className="text-sm text-slate-500">
                  {sellingListings.length} active listing
                  {sellingListings.length !== 1 ? "s" : ""}
                </span>
              </div>
              {sellingListings.length === 0 ? (
                <p className="text-slate-500">
                  No active listings.{" "}
                  <Link href="/seller/new" className="text-vault-700 underline">
                    Create one
                  </Link>
                </p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full min-w-[640px] text-left text-sm">
                    <thead>
                      <tr className="border-b border-slate-200 text-slate-500">
                        <th className="pb-2 pr-2">Code</th>
                        <th className="pb-2 pr-2">Store</th>
                        <th className="pb-2 pr-2">Status</th>
                        <th className="pb-2 pr-2">Price</th>
                        <th className="pb-2">Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {sellingListings.map((c) => (
                        <tr key={c.id} className="border-b border-slate-100">
                          <td className="py-3 font-mono text-xs">{c.code}</td>
                          <td className="py-3">{c.store.name}</td>
                          <td className="py-3">{statusBadge(c.status)}</td>
                          <td className="py-3">${Number(c.price).toFixed(2)}</td>
                          <td className="py-3">
                            <div className="flex flex-wrap gap-2">
                              <Link
                                href={`/marketplace/${c.id}`}
                                className="text-vault-700 hover:underline"
                              >
                                View
                              </Link>
                              <button
                                type="button"
                                onClick={() => removeListing(c.id)}
                                className="text-red-600 hover:underline"
                              >
                                Remove
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {tab === "sold" && (
            <div className="space-y-4">
              <h2 className="text-xl font-semibold text-vault-900">Coupons you sold</h2>
              <p className="text-sm text-slate-500">
                Completed sales (mock payment). Buyer name and sale amount are shown below.
              </p>
              {completedSales.length === 0 ? (
                <p className="text-slate-500">No completed sales yet.</p>
              ) : (
                <ul className="space-y-4">
                  {completedSales.map((s) => (
                    <li
                      key={s.id}
                      className="rounded-xl border border-slate-100 bg-slate-50/80 p-4"
                    >
                      <div className="flex flex-wrap items-start justify-between gap-4">
                        <div>
                          <p className="font-semibold text-vault-900">{s.coupon.storeName}</p>
                          <p className="text-sm text-slate-600">{s.coupon.description}</p>
                          <p className="mt-2 text-xs text-slate-500">
                            Buyer: {s.buyer.name} · ${Number(s.amount).toFixed(2)} ·{" "}
                            {String(s.createdAt)}
                          </p>
                        </div>
                        <div className="text-right">
                          <p className="text-xs uppercase text-slate-500">Your code</p>
                          <p className="font-mono">{s.coupon.code}</p>
                        </div>
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
