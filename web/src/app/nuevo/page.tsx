import { Suspense } from "react";
import { CrearSolicitudView } from "@/components/tickets/CrearSolicitudView";

export const dynamic = "force-dynamic";

export default function NuevoTicketPage() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-[#F2F2F7]" />}>
      <CrearSolicitudView />
    </Suspense>
  );
}
