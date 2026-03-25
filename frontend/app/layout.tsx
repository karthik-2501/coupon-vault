import type { Metadata } from "next";
import { Nav } from "@/components/Nav";
import "./globals.css";

export const metadata: Metadata = {
  title: "Coupon Vault",
  description: "Peer-to-peer validated coupon marketplace",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className="min-h-screen antialiased">
        <Nav />
        <main className="mx-auto max-w-6xl px-4 py-8">{children}</main>
        <footer className="border-t border-slate-200 py-8 text-center text-sm text-slate-500">
          Coupon Vault — academic demo. Mock payment &amp; validation.
        </footer>
      </body>
    </html>
  );
}
