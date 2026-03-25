"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { apiFetch } from "@/lib/api";
import type { PublicCoupon } from "@/lib/types";

export default function MarketplacePage() {
  const [items, setItems] = useState<PublicCoupon[]>([]);
  const [store, setStore] = useState("");
  const [discountType, setDiscountType] = useState("");
  const [priceMin, setPriceMin] = useState("");
  const [priceMax, setPriceMax] = useState("");
  const [minOrderMin, setMinOrderMin] = useState("");
  const [minOrderMax, setMinOrderMax] = useState("");
  const [err, setErr] = useState<string | null>(null);

  async function load() {
    setErr(null);
    const q = new URLSearchParams();
    if (store) q.set("store", store);
    if (discountType) q.set("discountType", discountType);
    if (priceMin) q.set("priceMin", priceMin);
    if (priceMax) q.set("priceMax", priceMax);
    if (minOrderMin) q.set("minOrderMin", minOrderMin);
    if (minOrderMax) q.set("minOrderMax", minOrderMax);
    const qs = q.toString();
    try {
      const data = await apiFetch<PublicCoupon[]>(
        `/coupons${qs ? `?${qs}` : ""}`,
        { token: null }
      );
      setItems(data);
    } catch (e) {
      setErr(e instanceof Error ? e.message : "Failed to load");
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-vault-900">Marketplace</h1>
        <p className="text-slate-600">
          Only coupons with status VALID and not expired are listed.
        </p>
      </div>

      <div className="flex flex-col gap-6 lg:flex-row">
        <aside className="w-full shrink-0 space-y-4 rounded-2xl border border-slate-200 bg-white p-4 lg:w-64">
          <h2 className="font-semibold text-vault-900">Filters</h2>
          <div>
            <label className="text-xs font-medium text-slate-600">Store name</label>
            <input
              className="mt-1 w-full rounded border border-slate-300 px-2 py-1.5 text-sm"
              placeholder="Typeahead search"
              value={store}
              onChange={(e) => setStore(e.target.value)}
            />
          </div>
          <div>
            <label className="text-xs font-medium text-slate-600">Discount type</label>
            <select
              className="mt-1 w-full rounded border border-slate-300 px-2 py-1.5 text-sm"
              value={discountType}
              onChange={(e) => setDiscountType(e.target.value)}
            >
              <option value="">Any</option>
              <option value="PERCENTAGE">Percentage</option>
              <option value="FIXED">Fixed</option>
              <option value="FREE_SHIPPING">Free shipping</option>
            </select>
          </div>
          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="text-xs text-slate-600">Price min</label>
              <input
                type="number"
                step="0.01"
                className="mt-1 w-full rounded border border-slate-300 px-2 py-1 text-sm"
                value={priceMin}
                onChange={(e) => setPriceMin(e.target.value)}
              />
            </div>
            <div>
              <label className="text-xs text-slate-600">Price max</label>
              <input
                type="number"
                step="0.01"
                className="mt-1 w-full rounded border border-slate-300 px-2 py-1 text-sm"
                value={priceMax}
                onChange={(e) => setPriceMax(e.target.value)}
              />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="text-xs text-slate-600">Min order min</label>
              <input
                type="number"
                step="0.01"
                className="mt-1 w-full rounded border border-slate-300 px-2 py-1 text-sm"
                value={minOrderMin}
                onChange={(e) => setMinOrderMin(e.target.value)}
              />
            </div>
            <div>
              <label className="text-xs text-slate-600">Min order max</label>
              <input
                type="number"
                step="0.01"
                className="mt-1 w-full rounded border border-slate-300 px-2 py-1 text-sm"
                value={minOrderMax}
                onChange={(e) => setMinOrderMax(e.target.value)}
              />
            </div>
          </div>
          <button
            type="button"
            onClick={load}
            className="w-full rounded-lg bg-vault-600 py-2 text-sm font-medium text-white hover:bg-vault-700"
          >
            Apply filters
          </button>
        </aside>

        <div className="flex-1">
          {err && <p className="text-red-600">{err}</p>}
          <div className="grid gap-4 sm:grid-cols-2">
            {items.map((c) => (
              <article
                key={c.id}
                className="flex flex-col rounded-2xl border border-slate-200 bg-white p-5 shadow-sm"
              >
                <div className="flex items-start gap-3">
                  <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-vault-100 text-lg font-bold text-vault-800">
                    {c.store.name.slice(0, 1)}
                  </div>
                  <div>
                    <h3 className="font-semibold text-vault-900">{c.store.name}</h3>
                    <p className="text-sm text-slate-600 line-clamp-2">{c.description}</p>
                  </div>
                </div>
                <dl className="mt-4 grid grid-cols-2 gap-2 text-sm">
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
                    <dt className="text-slate-500">Price</dt>
                    <dd className="font-medium">${Number(c.price).toFixed(2)}</dd>
                  </div>
                  <div className="col-span-2">
                    <dt className="text-slate-500">Expires</dt>
                    <dd>{c.expiryDate}</dd>
                  </div>
                </dl>
                <div className="mt-4 flex items-center justify-between text-sm text-slate-500">
                  <span>Seller: {c.seller.name}</span>
                  <Link
                    href={`/marketplace/${c.id}`}
                    className="rounded-lg bg-slate-900 px-3 py-1.5 text-white hover:bg-slate-800"
                  >
                    View details
                  </Link>
                </div>
              </article>
            ))}
          </div>
          {items.length === 0 && !err && (
            <p className="text-slate-500">No coupons match your filters.</p>
          )}
        </div>
      </div>
    </div>
  );
}
