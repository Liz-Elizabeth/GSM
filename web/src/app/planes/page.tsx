import { Suspense } from "react";
import { PlanesView } from "@/components/planes/PlanesView";

export const dynamic = "force-dynamic";

export default function PlanesPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-[#F2F2F7]" />}>
      <PlanesView />
    </Suspense>
  );
}
