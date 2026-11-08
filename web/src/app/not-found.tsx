import Link from "next/link";
import { Container } from "@/components/common/Container";
import { ArrowLeft, AlertCircle } from "lucide-react";

export const dynamic = "force-dynamic";

export default function NotFound() {
  return (
    <div className="w-full min-h-[calc(100dvh-4rem)] flex items-center justify-center py-16">
      <Container className="text-center max-w-lg">
        <div className="w-16 h-16 rounded-3xl bg-blue-50 text-[#0066FF] flex items-center justify-center mx-auto mb-4 shadow-sm">
          <AlertCircle className="w-8 h-8" />
        </div>
        <h1 className="text-3xl font-black text-gray-900 tracking-tight mb-2">
          Página no encontrada (404)
        </h1>
        <p className="text-sm text-gray-600 mb-6">
          La ruta que intentas consultar no existe o ha sido movida en la plataforma GSM.
        </p>
        <Link
          href="/"
          className="inline-flex items-center gap-2 px-6 py-3 rounded-2xl bg-[#0066FF] text-white font-bold text-sm hover:bg-[#0052CC] transition-colors shadow-md"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Volver al Inicio</span>
        </Link>
      </Container>
    </div>
  );
}
