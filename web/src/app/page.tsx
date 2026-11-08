import { Suspense } from "react";
import { LandingPageView } from "@/components/landing/LandingPageView";

export const dynamic = "force-dynamic";

export default function HomePage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-[#F2F2F7]" />}>
      <LandingPageView />
    </Suspense>
  );
}
