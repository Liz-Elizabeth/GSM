import { Suspense } from "react";
import { RegistroView } from "@/components/auth/RegistroView";

export const dynamic = "force-dynamic";

export default function RegistroPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-[#F2F2F7]" />}>
      <RegistroView />
    </Suspense>
  );
}
