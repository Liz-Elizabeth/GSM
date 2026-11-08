import { Suspense } from "react";
import { TransaccionesView } from "@/components/planes/TransaccionesView";

export const dynamic = "force-dynamic";

export default function TransaccionesPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-[#F2F2F7]" />}>
      <TransaccionesView />
    </Suspense>
  );
}
