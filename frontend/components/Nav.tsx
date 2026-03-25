"use client";

import Link from "next/link";
import { useEffect, useState } from "react";

export function Nav() {
  const [logged, setLogged] = useState(false);

  useEffect(() => {
    setLogged(!!localStorage.getItem("cv_token"));
  }, []);

  function logout() {
    localStorage.removeItem("cv_token");
    localStorage.removeItem("cv_name");
    setLogged(false);
    window.location.href = "/";
  }

  return (
    <header className="border-b border-slate-200 bg-white/80 backdrop-blur">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4">
        <Link href="/" className="text-xl font-bold text-vault-900">
          Coupon Vault
        </Link>
        <nav className="flex flex-wrap items-center gap-4 text-sm font-medium text-slate-700">
          <Link href="/marketplace" className="hover:text-vault-700">
            Marketplace
          </Link>
          {logged && (
            <Link href="/dashboard" className="hover:text-vault-700">
              Dashboard
            </Link>
          )}
          {!logged ? (
            <>
              <Link href="/auth/login" className="hover:text-vault-700">
                Login
              </Link>
              <Link
                href="/auth/register"
                className="rounded-lg bg-vault-600 px-3 py-1.5 text-white hover:bg-vault-700"
              >
                Register
              </Link>
            </>
          ) : (
            <button
              type="button"
              onClick={logout}
              className="text-slate-600 hover:text-vault-800"
            >
              Logout
            </button>
          )}
        </nav>
      </div>
    </header>
  );
}
