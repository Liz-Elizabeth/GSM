import type { Metadata } from "next";
import "./globals.css";
import { AuthProvider } from "@/context/AuthContext";
import { Header } from "@/components/layout/Header";
import { SmoothScrollProvider } from "@/components/providers/SmoothScrollProvider";

export const metadata: Metadata = {
  title: "GSM - Plataforma de Gestión de Solicitudes y Soporte Técnico",
  description:
    "Sistema web de gestión de tickets, mesa de ayuda, soporte técnico y wallet de pases de abordaje en tiempo real.",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="es" className="min-h-dvh overflow-x-clip">
      <body className="min-h-dvh flex flex-col bg-[#F2F2F7] text-[#1C1C1E] antialiased w-full overflow-x-clip">
        <AuthProvider>
          <SmoothScrollProvider>
            <Header />
            <main className="flex-1 w-full flex flex-col">
              {children}
            </main>
          </SmoothScrollProvider>
        </AuthProvider>
      </body>
    </html>
  );
}
