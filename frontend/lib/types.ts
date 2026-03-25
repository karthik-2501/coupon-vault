import type { ReactNode } from "react";

export type DiscountType = "PERCENTAGE" | "FIXED" | "FREE_SHIPPING";

export type CouponStatus =
  | "PENDING_VALIDATION"
  | "VALID"
  | "INVALID"
  | "SOLD"
  | "EXPIRED";

export interface StoreSummary {
  id: string;
  name: string;
  logoUrl?: string | null;
}

export interface PublicCoupon {
  id: string;
  description: string;
  discountType: DiscountType;
  discountValue: number;
  minOrderValue?: number | null;
  expiryDate: string;
  price: number;
  store: StoreSummary;
  seller: { id: string; name: string };
}

export interface CouponDetail extends PublicCoupon {
  code: string;
  status: CouponStatus;
  store: StoreSummary & { websiteUrl?: string };
}

export interface AuthResponse {
  token: string;
  userId: string;
  name: string;
  email: string;
}

export interface LayoutProps {
  children: ReactNode;
}
