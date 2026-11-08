import { Suspense } from "react";
import { SimuladorFlowView } from "@/components/planes/SimuladorFlowView";

export const dynamic = "force-dynamic";

export default function SimuladorPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen flex items-center justify-center bg-[#F2F2F7]">
          <div className="w-8 h-8 border-3 border-[#0066FF] border-t-transparent rounded-full animate-spin" />
        </div>
      }
    >
      <SimuladorFlowView />
    </Suspense>
  );
}
