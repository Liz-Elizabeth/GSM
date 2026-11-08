import { Suspense } from "react";
import { BandejaPrincipal } from "@/components/tickets/BandejaPrincipal";
import { TicketSkeletonList } from "@/components/common/Skeletons";

export const dynamic = "force-dynamic";

export default function TicketsPage() {
  return (
    <Suspense fallback={<TicketSkeletonList count={4} />}>
      <BandejaPrincipal />
    </Suspense>
  );
}
