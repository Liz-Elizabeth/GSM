import { Suspense } from "react";
import { RetornoPagoView } from "@/components/planes/RetornoPagoView";

export const dynamic = "force-dynamic";

export default function RetornoPagoPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-[70vh] flex items-center justify-center">
          <div className="w-8 h-8 border-3 border-[#0066FF] border-t-transparent rounded-full animate-spin" />
        </div>
      }
    >
      <RetornoPagoView />
    </Suspense>
  );
}
