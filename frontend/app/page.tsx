import Link from "next/link";

export default function HomePage() {
  return (
    <div className="space-y-12 text-center">
      <section className="rounded-3xl bg-gradient-to-br from-vault-100 to-white px-6 py-16 shadow-sm">
        <h1 className="text-4xl font-extrabold tracking-tight text-vault-900 md:text-5xl">
          Buy &amp; sell verified coupon codes
        </h1>
        <p className="mx-auto mt-4 max-w-2xl text-lg text-slate-600">
          Coupon Vault is a store‑centric marketplace. Every listing is checked
          against a pluggable validation engine before it goes live. Same account
          can act as buyer and seller.
        </p>
        <div className="mt-8 flex flex-wrap justify-center gap-4">
          <Link
            href="/marketplace"
            className="rounded-xl bg-vault-600 px-8 py-3 font-semibold text-white shadow hover:bg-vault-700"
          >
            Browse coupons
          </Link>
          <Link
            href="/seller/new"
            className="rounded-xl border-2 border-vault-600 px-8 py-3 font-semibold text-vault-800 hover:bg-vault-50"
          >
            Sell a coupon
          </Link>
        </div>
      </section>
      <section className="grid gap-6 text-left md:grid-cols-3">
        {[
          {
            title: "Automated validation",
            body: "Codes start as pending; mock rules mark VALID- prefixes as valid.",
          },
          {
            title: "Secure checkout",
            body: "Mock payment gateway with a clean service you can swap later.",
          },
          {
            title: "Hidden until purchase",
            body: "Full code is never public until a completed transaction.",
          },
        ].map((c) => (
          <div
            key={c.title}
            className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
          >
            <h2 className="font-semibold text-vault-900">{c.title}</h2>
            <p className="mt-2 text-slate-600">{c.body}</p>
          </div>
        ))}
      </section>
    </div>
  );
}
