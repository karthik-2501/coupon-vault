"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { apiFetch, getToken } from "@/lib/api";

export default function NewCouponPage() {
  const router = useRouter();
  const [storeName, setStoreName] = useState("");
  const [storeWebsiteUrl, setStoreWebsiteUrl] = useState("");
  const [storeLogoUrl, setStoreLogoUrl] = useState("");
  const [code, setCode] = useState("");
  const [description, setDescription] = useState("");
  const [discountType, setDiscountType] = useState("PERCENTAGE");
  const [discountValue, setDiscountValue] = useState("10");
  const [minOrderValue, setMinOrderValue] = useState("");
  const [expiryDate, setExpiryDate] = useState("");
  const [askingPrice, setAskingPrice] = useState("5");
  const [msg, setMsg] = useState<string | null>(null);
  const [err, setErr] = useState<string | null>(null);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setErr(null);
    setMsg(null);
    const token = getToken();
    if (!token) {
      router.push("/auth/login");
      return;
    }
    const body: Record<string, unknown> = {
      storeName,
      storeWebsiteUrl,
      code,
      description,
      discountType,
      discountValue: parseFloat(discountValue),
      expiryDate,
      askingPrice: parseFloat(askingPrice),
    };
    if (storeLogoUrl.trim()) body.storeLogoUrl = storeLogoUrl.trim();
    if (minOrderValue.trim()) body.minOrderValue = parseFloat(minOrderValue);

    try {
      const res = await apiFetch<{ id: string; status: string; message: string }>(
        "/coupons",
        { method: "POST", body: JSON.stringify(body), token }
      );
      setMsg(`Created ${res.id} — status ${res.status}. ${res.message}`);
      router.push("/seller");
    } catch (ex) {
      setErr(ex instanceof Error ? ex.message : "Failed");
    }
  }

  return (
    <div className="mx-auto max-w-xl space-y-6">
      <Link href="/seller" className="text-sm text-vault-700 hover:underline">
        ← Seller dashboard
      </Link>
      <h1 className="text-2xl font-bold text-vault-900">List a coupon</h1>
      <p className="text-sm text-slate-600">
        Use a code starting with <code className="rounded bg-slate-100 px-1">VALID-</code> for the
        mock validator to mark it valid.
      </p>

      <form onSubmit={onSubmit} className="space-y-4 rounded-2xl border border-slate-200 bg-white p-6 shadow">
        <h2 className="font-semibold text-vault-900">Store</h2>
        <div>
          <label className="text-sm font-medium text-slate-700">Store name</label>
          <input
            required
            className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
            value={storeName}
            onChange={(e) => setStoreName(e.target.value)}
          />
        </div>
        <div>
          <label className="text-sm font-medium text-slate-700">Store website URL</label>
          <input
            required
            type="url"
            className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
            value={storeWebsiteUrl}
            onChange={(e) => setStoreWebsiteUrl(e.target.value)}
          />
        </div>
        <div>
          <label className="text-sm font-medium text-slate-700">Logo URL (optional)</label>
          <input
            type="url"
            className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
            value={storeLogoUrl}
            onChange={(e) => setStoreLogoUrl(e.target.value)}
          />
        </div>

        <h2 className="pt-2 font-semibold text-vault-900">Coupon</h2>
        <div>
          <label className="text-sm font-medium text-slate-700">Code</label>
          <input
            required
            className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2 font-mono"
            value={code}
            onChange={(e) => setCode(e.target.value)}
          />
        </div>
        <div>
          <label className="text-sm font-medium text-slate-700">Description</label>
          <textarea
            required
            rows={3}
            className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>
        <div>
          <label className="text-sm font-medium text-slate-700">Discount type</label>
          <select
            className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
            value={discountType}
            onChange={(e) => setDiscountType(e.target.value)}
          >
            <option value="PERCENTAGE">Percentage</option>
            <option value="FIXED">Fixed amount</option>
            <option value="FREE_SHIPPING">Free shipping</option>
          </select>
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label className="text-sm font-medium text-slate-700">Discount value</label>
            <input
              required
              type="number"
              step="0.01"
              min="0"
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
              value={discountValue}
              onChange={(e) => setDiscountValue(e.target.value)}
            />
          </div>
          <div>
            <label className="text-sm font-medium text-slate-700">Min order (optional)</label>
            <input
              type="number"
              step="0.01"
              min="0"
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
              value={minOrderValue}
              onChange={(e) => setMinOrderValue(e.target.value)}
            />
          </div>
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <label className="text-sm font-medium text-slate-700">Expiry date</label>
            <input
              required
              type="date"
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
              value={expiryDate}
              onChange={(e) => setExpiryDate(e.target.value)}
            />
          </div>
          <div>
            <label className="text-sm font-medium text-slate-700">Asking price ($)</label>
            <input
              required
              type="number"
              step="0.01"
              min="0.01"
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
              value={askingPrice}
              onChange={(e) => setAskingPrice(e.target.value)}
            />
          </div>
        </div>

        {err && <p className="text-sm text-red-600">{err}</p>}
        {msg && <p className="text-sm text-emerald-700">{msg}</p>}

        <button
          type="submit"
          className="w-full rounded-xl bg-vault-600 py-3 font-semibold text-white hover:bg-vault-700"
        >
          Submit listing
        </button>
      </form>
    </div>
  );
}
