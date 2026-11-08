"use client";

import React, { useRef } from "react";
import Link from "next/link";
import { useGSAP } from "@gsap/react";
import gsap from "gsap";
import {
  Shield,
  Zap,
  Clock,
  CheckCircle2,
  Sparkles,
  ArrowRight,
  Ticket,
  Lock,
  Headphones,
  Server,
  Code2,
  Mail,
  CreditCard,
  Building2,
  Users,
  Award,
  ChevronRight,
  Plane,
  Radio,
  FileCheck,
  Check,
} from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { PLANES_SOPORTE } from "@/types/plan";
import { Container } from "@/components/common/Container";
import { SpotlightCard } from "@/components/reactbits/SpotlightCard";
import { InteractiveHeroShowcase } from "./InteractiveHeroShowcase";
import { SlaCalculatorWidget } from "./SlaCalculatorWidget";

export function LandingPageView() {
  const { usuarioActual, estaAutenticado } = useAuth();
  const heroRef = useRef<HTMLDivElement>(null);

  // Animaciones GSAP de entrada fluida y suave
  useGSAP(
    () => {
      const prefersReducedMotion = window.matchMedia(
        "(prefers-reduced-motion: reduce)"
      ).matches;
      if (prefersReducedMotion) return;

      gsap.from(".gsap-hero-badge", {
        opacity: 0,
        y: -15,
        duration: 0.7,
        ease: "power3.out",
      });

      gsap.from(".gsap-hero-title", {
        opacity: 0,
        y: 28,
        duration: 0.85,
        delay: 0.1,
        ease: "power3.out",
      });

      gsap.from(".gsap-hero-desc", {
        opacity: 0,
        y: 20,
        duration: 0.85,
        delay: 0.22,
        ease: "power3.out",
      });

      gsap.from(".gsap-hero-actions", {
        opacity: 0,
        y: 18,
        duration: 0.85,
        delay: 0.32,
        ease: "power3.out",
      });

      gsap.from(".gsap-hero-showcase", {
        opacity: 0,
        y: 35,
        duration: 1,
        delay: 0.42,
        ease: "power3.out",
      });
    },
    { scope: heroRef }
  );

  return (
    <div className="w-full min-h-dvh flex flex-col bg-[#F2F2F7] overflow-x-clip">
      {/* 1. HERO SECTION CON EFECTO WOW & SHOWCASE INTERACTIVO */}
      <section
        ref={heroRef}
        className="w-full relative overflow-hidden pt-12 pb-20 sm:pt-16 sm:pb-28 bg-gradient-to-b from-white via-[#FAFBFD] to-[#F2F2F7] border-b border-gray-200/80"
      >
        {/* Fondo con Patrón de Cuadrícula Sutil y Resplandor Radial */}
        <div className="absolute inset-0 bg-grid-pattern opacity-60 mask-radial-hero pointer-events-none" />

        <Container className="text-center relative z-10">
          {/* Banner de Sesión Activa o Propuesta de Valor */}
          <div className="gsap-hero-badge">
            {estaAutenticado ? (
              <div className="mb-6 inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-blue-50/90 backdrop-blur-md border border-blue-200 text-xs font-bold text-[#0066FF] shadow-2xs">
                <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
                <span>
                  Sesión activa como {usuarioActual.nombre} ({usuarioActual.rol})
                </span>
                <span className="text-gray-300">|</span>
                <Link
                  href="/tickets"
                  className="font-black hover:text-[#0052CC] flex items-center gap-1"
                >
                  <span>Ir a mi Bandeja</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>
            ) : (
              <div className="inline-flex items-center gap-2 px-4 py-1.5 rounded-full bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-200/80 text-xs font-bold text-[#0066FF] mb-6 shadow-2xs">
                <Radio className="w-3.5 h-3.5 text-emerald-500 animate-pulse" />
                <span>Mesa de Ayuda Empresarial & Continuidad TI en Chile</span>
              </div>
            )}
          </div>

          {/* Título Principal con Efecto Tipográfico Fluido (Clamp) */}
          <h1 className="gsap-hero-title text-[clamp(2.25rem,4.5vw,4.5rem)] font-black text-gray-900 tracking-tight max-w-5xl mx-auto leading-[1.08]">
            Soporte Técnico de Misión Crítica con{" "}
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#0066FF] via-indigo-600 to-[#0047AB]">
              SLA Garantizado
            </span>
          </h1>

          <p className="gsap-hero-desc mt-6 text-[clamp(1rem,1.35vw,1.25rem)] text-gray-600 max-w-3xl mx-auto leading-relaxed">
            Plataforma corporativa de continuidad operacional con ingenieros senior dedicados,
            pases de avión QR digitales, evidencias seguras en la nube y pasarela de pago Flow con Webpay Plus.
          </p>

          {/* Botones de Acción */}
          <div className="gsap-hero-actions mt-8 flex flex-col sm:flex-row items-center justify-center gap-3.5">
            {estaAutenticado ? (
              <Link
                href="/tickets"
                className="w-full sm:w-auto px-8 py-4 rounded-2xl font-bold text-sm text-white bg-[#0066FF] hover:bg-[#0052CC] active:scale-98 transition-all shadow-lg hover:shadow-xl shadow-blue-500/20 flex items-center justify-center gap-2"
              >
                <Ticket className="w-4 h-4" />
                <span>Acceder a la Bandeja de Tickets</span>
                <ArrowRight className="w-4 h-4" />
              </Link>
            ) : (
              <>
                <Link
                  href="/registro"
                  className="w-full sm:w-auto px-8 py-4 rounded-2xl font-bold text-sm text-white bg-[#0066FF] hover:bg-[#0052CC] active:scale-98 transition-all shadow-lg hover:shadow-xl shadow-blue-500/20 flex items-center justify-center gap-2"
                >
                  <span>Registrar mi Empresa</span>
                  <ArrowRight className="w-4 h-4" />
                </Link>

                <Link
                  href="/login"
                  className="w-full sm:w-auto px-7 py-4 rounded-2xl font-bold text-sm text-gray-800 bg-white hover:bg-gray-50 border border-gray-300 shadow-xs transition-colors flex items-center justify-center gap-2"
                >
                  <span>Iniciar Sesión</span>
                </Link>
              </>
            )}

            <Link
              href="/planes"
              className="w-full sm:w-auto px-6 py-4 rounded-2xl font-bold text-sm text-[#0066FF] hover:bg-blue-50/80 transition-colors flex items-center justify-center gap-1.5"
            >
              <span>Ver Planes & Precios</span>
              <ChevronRight className="w-4 h-4" />
            </Link>
          </div>

          {/* ELEMENTO PROTAGONISTA FIRMA: SHOWCASE INTERACTIVO */}
          <div className="gsap-hero-showcase w-full">
            <InteractiveHeroShowcase />
          </div>
        </Container>
      </section>

      {/* 2. SECCIÓN QUIÉNES SOMOS & PILARES FUNDAMENTALES */}
      <section id="quienes-somos" className="w-full py-[clamp(3.5rem,6vw,6rem)] bg-white border-b border-gray-200/80">
        <Container>
          <div className="text-center max-w-3xl mx-auto mb-16">
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-blue-50 text-[#0066FF] mb-2">
              <Award className="w-3.5 h-3.5" />
              Quiénes Somos
            </div>
            <h2 className="text-[clamp(1.75rem,3.2vw,3rem)] font-black text-gray-900 tracking-tight">
              Ingeniería Chilena Dedicada a Blindar tu Infraestructura
            </h2>
            <p className="mt-4 text-[clamp(0.95rem,1.2vw,1.1rem)] text-gray-600 leading-relaxed">
              En <strong>GSM Tech Solutions</strong> entendemos que cada minuto de caída en un portal
              o servidor significa pérdidas comerciales inmediatas. Eliminamos la lentitud de las
              mesas de ayuda tradicionales mediante trazabilidad en tiempo real, chat técnico directo
              y acuerdos contractuales de tiempo de respuesta (SLA).
            </p>
          </div>

          {/* 4 Pilares Fundamentales con React Bits SpotlightCard */}
          <div className="grid grid-cols-[repeat(auto-fit,minmax(min(100%,18rem),1fr))] gap-6">
            <SpotlightCard className="p-7">
              <div className="w-12 h-12 rounded-2xl bg-blue-100 text-[#0066FF] flex items-center justify-center mb-4">
                <Users className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-gray-900 text-base mb-2">Ingenieros Senior Dedicados</h3>
              <p className="text-xs text-gray-600 leading-relaxed">
                Sin intermediarios ni bots genéricos: serás atendido por ingenieros especialistas en
                Linux, NGINX, Docker, AWS, bases de datos y Node.js.
              </p>
            </SpotlightCard>

            <SpotlightCard className="p-7" spotlightColor="rgba(147, 51, 234, 0.14)" borderColor="rgba(147, 51, 234, 0.35)">
              <div className="w-12 h-12 rounded-2xl bg-purple-100 text-purple-700 flex items-center justify-center mb-4">
                <Clock className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-gray-900 text-base mb-2">SLA Rígido y Medible</h3>
              <p className="text-xs text-gray-600 leading-relaxed">
                Garantías de primera respuesta desde 24 horas en plan PLUS hasta menos de 15 minutos
                en nivel MAX para emergencias críticas con penalización contractual.
              </p>
            </SpotlightCard>

            <SpotlightCard className="p-7" spotlightColor="rgba(16, 185, 129, 0.14)" borderColor="rgba(16, 185, 129, 0.35)">
              <div className="w-12 h-12 rounded-2xl bg-emerald-100 text-emerald-700 flex items-center justify-center mb-4">
                <Ticket className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-gray-900 text-base mb-2">Pases de Avión QR</h3>
              <p className="text-xs text-gray-600 leading-relaxed">
                Trazabilidad inspirada en tarjetas de embarque aéreas con código QR para escanear y
                sincronizar el ticket en segundos desde el celular o mostrador técnico.
              </p>
            </SpotlightCard>

            <SpotlightCard className="p-7" spotlightColor="rgba(245, 158, 11, 0.14)" borderColor="rgba(245, 158, 11, 0.35)">
              <div className="w-12 h-12 rounded-2xl bg-amber-100 text-amber-700 flex items-center justify-center mb-4">
                <CreditCard className="w-6 h-6" />
              </div>
              <h3 className="font-bold text-gray-900 text-base mb-2">Alianza Flow Chile</h3>
              <p className="text-xs text-gray-600 leading-relaxed">
                Cobro transparente con Webpay Plus, tarjetas de crédito en cuotas sin interés y
                emisión automática de factura electrónica inmediata para tu empresa.
              </p>
            </SpotlightCard>
          </div>
        </Container>
      </section>

      {/* 3. SIMULADOR INTERACTIVO DE SLA */}
      <section className="w-full py-[clamp(3.5rem,6vw,6rem)] bg-gradient-to-b from-white to-[#F2F2F7] border-b border-gray-200/70">
        <Container>
          <SlaCalculatorWidget />
        </Container>
      </section>

      {/* 4. SERVICIOS Y COBERTURA TÉCNICA */}
      <section className="w-full py-[clamp(3.5rem,6vw,6rem)] bg-[#F8FAFC] border-b border-gray-200/80">
        <Container>
          <div className="text-center max-w-2xl mx-auto mb-16">
            <span className="text-xs font-bold uppercase tracking-wider text-[#0066FF] block mb-1">
              Catálogo de Especialidades
            </span>
            <h2 className="text-[clamp(1.75rem,3.2vw,3rem)] font-black text-gray-900 tracking-tight">
              Soporte Integral para tu Ecosistema Tecnológico
            </h2>
          </div>

          <div className="grid grid-cols-[repeat(auto-fit,minmax(min(100%,20rem),1fr))] gap-6">
            <SpotlightCard className="p-7" spotlightColor="rgba(239, 68, 68, 0.12)" borderColor="rgba(239, 68, 68, 0.3)">
              <div className="w-11 h-11 rounded-2xl bg-red-50 text-red-600 flex items-center justify-center mb-4">
                <Zap className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-gray-900 text-base mb-2">Incidentes Críticos & Caídas</h3>
              <p className="text-xs text-gray-600 leading-relaxed">
                Diagnóstico de emergencia, restauración de servicios caídos y mitigación inmediata
                ante fallos de hardware o software.
              </p>
            </SpotlightCard>

            <SpotlightCard className="p-7">
              <div className="w-11 h-11 rounded-2xl bg-blue-50 text-[#0066FF] flex items-center justify-center mb-4">
                <Code2 className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-gray-900 text-base mb-2">Desarrollo Web & APIs</h3>
              <p className="text-xs text-gray-600 leading-relaxed">
                Integraciones REST, pasarelas de pago (Transbank, Flow), corrección de bugs frontend y
                optimización de carritos de compra.
              </p>
            </SpotlightCard>

            <SpotlightCard className="p-7" spotlightColor="rgba(6, 182, 212, 0.12)" borderColor="rgba(6, 182, 212, 0.3)">
              <div className="w-11 h-11 rounded-2xl bg-cyan-50 text-cyan-700 flex items-center justify-center mb-4">
                <Server className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-gray-900 text-base mb-2">Servidores, VPS & Hosting</h3>
              <p className="text-xs text-gray-600 leading-relaxed">
                Administración de entornos Linux, servidores web Apache/NGINX, Docker, balanceadores
                y copias de seguridad automáticas.
              </p>
            </SpotlightCard>

            <SpotlightCard className="p-7" spotlightColor="rgba(16, 185, 129, 0.12)" borderColor="rgba(16, 185, 129, 0.3)">
              <div className="w-11 h-11 rounded-2xl bg-emerald-50 text-emerald-700 flex items-center justify-center mb-4">
                <Lock className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-gray-900 text-base mb-2">Ciberseguridad & Certificados SSL</h3>
              <p className="text-xs text-gray-600 leading-relaxed">
                Renovación de certificados TLS/SSL Let&apos;s Encrypt, cortafuegos web (WAF), auditorías de
                vulnerabilidades y control de acceso.
              </p>
            </SpotlightCard>

            <SpotlightCard className="p-7" spotlightColor="rgba(147, 51, 234, 0.12)" borderColor="rgba(147, 51, 234, 0.3)">
              <div className="w-11 h-11 rounded-2xl bg-purple-50 text-purple-700 flex items-center justify-center mb-4">
                <Mail className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-gray-900 text-base mb-2">Correo Corporativo & DNS</h3>
              <p className="text-xs text-gray-600 leading-relaxed">
                Configuración y solución de entregabilidad SPF, DKIM, DMARC en Google Workspace,
                Microsoft 365 y cPanel.
              </p>
            </SpotlightCard>

            <SpotlightCard className="p-7" spotlightColor="rgba(245, 158, 11, 0.12)" borderColor="rgba(245, 158, 11, 0.3)">
              <div className="w-11 h-11 rounded-2xl bg-amber-50 text-amber-700 flex items-center justify-center mb-4">
                <Headphones className="w-5 h-5" />
              </div>
              <h3 className="font-bold text-gray-900 text-base mb-2">Mesa de Ayuda 24/7</h3>
              <p className="text-xs text-gray-600 leading-relaxed">
                Atención omnicanal vía WhatsApp corporativo, aplicación web, portal móvil y línea
                telefónica prioritaria.
              </p>
            </SpotlightCard>
          </div>
        </Container>
      </section>

      {/* 5. VISTA PREVIA DE PLANES COMERCIALES */}
      <section className="w-full py-[clamp(3.5rem,6vw,6rem)] bg-white border-b border-gray-200/60">
        <Container>
          <div className="text-center max-w-3xl mx-auto mb-16">
            <span className="text-xs font-bold uppercase tracking-wider text-[#0066FF] block mb-1">
              Precios Transparentes
            </span>
            <h2 className="text-[clamp(1.75rem,3.2vw,3rem)] font-black text-gray-900 tracking-tight">
              Elige el Nivel de SLA Adecuado para tu Empresa
            </h2>
            <p className="text-sm text-gray-500 mt-2">
              Suscripciones mensuales o anuales con 20% de descuento y pago seguro con Flow Chile.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 lg:gap-8 items-stretch">
            {PLANES_SOPORTE.map((p) => (
              <div
                key={p.id}
                className={`p-7 rounded-3xl border flex flex-col justify-between transition-all ${
                  p.esRecomendado
                    ? "border-[#0066FF] shadow-xl ring-2 ring-[#0066FF]/20 bg-white md:-translate-y-2"
                    : "border-gray-200 bg-gray-50/50 hover:bg-white hover:shadow-md"
                }`}
              >
                <div>
                  <div className="flex justify-between items-center mb-3">
                    <span className="text-xs font-black uppercase text-gray-400">{p.nivel}</span>
                    <span className="px-2.5 py-0.5 rounded text-[10px] font-bold bg-gray-100 text-gray-700">
                      {p.insignia}
                    </span>
                  </div>
                  <h3 className="text-2xl font-black text-gray-900">{p.nombre}</h3>
                  <div className="text-2xl font-black text-gray-900 mt-2">{p.precioTexto}</div>
                  <div className="text-xs font-semibold text-blue-600 mt-1">SLA: {p.slaRespuesta}</div>

                  <ul className="mt-5 space-y-2.5 text-xs text-gray-600">
                    {p.beneficiosClave.slice(0, 4).map((b, i) => (
                      <li key={i} className="flex items-start gap-2">
                        <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600 shrink-0 mt-0.5" />
                        <span>{b}</span>
                      </li>
                    ))}
                  </ul>
                </div>

                <div className="mt-7 pt-4 border-t border-gray-200/60">
                  <Link
                    href="/planes"
                    className={`w-full py-3 rounded-xl font-bold text-xs flex items-center justify-center gap-1.5 transition-colors ${
                      p.esRecomendado
                        ? "bg-[#0066FF] text-white hover:bg-[#0052CC] shadow-md shadow-blue-500/20"
                        : "bg-white text-gray-800 border border-gray-300 hover:bg-gray-100"
                    }`}
                  >
                    <span>Ver Detalles & Contratar</span>
                    <ArrowRight className="w-3 h-3" />
                  </Link>
                </div>
              </div>
            ))}
          </div>
        </Container>
      </section>

      {/* 6. TESTIMONIOS */}
      <section className="w-full py-[clamp(3.5rem,6vw,6rem)] bg-[#F2F2F7] border-b border-gray-200/80">
        <Container>
          <div className="text-center max-w-3xl mx-auto mb-12">
            <span className="text-xs font-bold uppercase tracking-wider text-gray-400 block mb-2">
              Casos de Éxito
            </span>
            <h2 className="text-[clamp(1.75rem,3.2vw,3rem)] font-black text-gray-900 tracking-tight">
              Empresas que Confían su Soporte en GSM
            </h2>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="bg-white p-7 sm:p-8 rounded-3xl border border-gray-200/80 shadow-xs">
              <p className="text-xs sm:text-sm text-gray-600 italic leading-relaxed mb-6">
                &ldquo;Tuvimos un fallo crítico con el certificado SSL en nuestro portal de pagos un
                viernes a las 18:00 hrs. El equipo de GSM respondió en menos de 20 minutos y dejó el
                sitio operando sin pérdida de transacciones.&rdquo;
              </p>
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-blue-100 text-[#0066FF] flex items-center justify-center font-bold text-xs">
                  CR
                </div>
                <div>
                  <div className="text-xs font-bold text-gray-900">Camila Rojas</div>
                  <div className="text-[11px] text-gray-500">Directora TI - Innovatech SpA</div>
                </div>
              </div>
            </div>

            <div className="bg-white p-7 sm:p-8 rounded-3xl border border-gray-200/80 shadow-xs">
              <p className="text-xs sm:text-sm text-gray-600 italic leading-relaxed mb-6">
                &ldquo;El sistema de pases de avión QR hace que cualquier persona de mi equipo pueda
                escanear y saber en qué estado está un requerimiento. El pago con Flow y factura
                automática simplificó todo el proceso administrativo.&rdquo;
              </p>
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-emerald-100 text-emerald-700 flex items-center justify-center font-bold text-xs">
                  MG
                </div>
                <div>
                  <div className="text-xs font-bold text-gray-900">Matías González</div>
                  <div className="text-[11px] text-gray-500">Gerente de Operaciones - Logística Austral</div>
                </div>
              </div>
            </div>
          </div>
        </Container>
      </section>

      {/* 7. CALL TO ACTION FINAL: 100% BORDE A BORDE SIN CONTENEDOR REDONDEADO NI MÁRGENES LATERALES */}
      <section className="w-full bg-gradient-to-r from-[#003B95] via-[#0052CC] to-[#0066FF] text-white text-center py-[clamp(4.5rem,7vw,7.5rem)] relative overflow-hidden">
        {/* Fondo con patrones luminosos */}
        <div className="absolute inset-0 bg-grid-pattern opacity-10 pointer-events-none" />
        <div className="absolute -top-32 -left-32 w-96 h-96 bg-white/10 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute -bottom-32 -right-32 w-96 h-96 bg-cyan-400/20 rounded-full blur-3xl pointer-events-none" />

        <Container className="relative z-10 text-center">
          <h2 className="text-[clamp(2rem,3.8vw,3.75rem)] font-black tracking-tight text-white mb-4 leading-tight max-w-4xl mx-auto">
            ¿Listo para Blindar la Continuidad Tecnológica de tu Empresa?
          </h2>
          <p className="text-[clamp(1rem,1.4vw,1.25rem)] text-blue-100 max-w-2xl mx-auto mb-8 leading-relaxed">
            Crea tu cuenta de cliente en segundos o inicia sesión para solicitar asistencia técnica inmediata con SLA garantizado.
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-3.5">
            <Link
              href="/registro"
              className="w-full sm:w-auto px-8 py-4 rounded-2xl font-bold text-sm text-[#003B95] bg-white hover:bg-gray-100 active:scale-98 transition-all shadow-xl hover:shadow-2xl"
            >
              Registrar mi Empresa Gratis
            </Link>
            <Link
              href="/login"
              className="w-full sm:w-auto px-7 py-4 rounded-2xl font-semibold text-sm text-white bg-white/10 hover:bg-white/20 border border-white/20 backdrop-blur-md transition-colors"
            >
              Ingresar al Portal
            </Link>
          </div>
        </Container>
      </section>

      {/* 8. FOOTER: 100% BORDE A BORDE */}
      <footer className="w-full bg-[#0A0D14] text-gray-400 py-16 border-t border-slate-800 text-xs">
        <Container>
          <div className="flex flex-col sm:flex-row justify-between items-center gap-6">
            <div className="flex items-center gap-3">
              <div className="w-9 h-9 rounded-xl bg-[#0066FF] text-white flex items-center justify-center font-black text-base shadow-sm">
                GSM
              </div>
              <div>
                <span className="font-bold text-white text-sm">GSM Tech Solutions SpA</span>
                <p className="text-[11px] text-gray-500">Santiago de Chile · Mesa de Ayuda Corporativa</p>
              </div>
            </div>

            <div className="flex flex-wrap justify-center gap-6 text-gray-400">
              <Link href="/#quienes-somos" className="hover:text-white transition-colors">
                Quiénes Somos
              </Link>
              <Link href="/planes" className="hover:text-white transition-colors">
                Planes SLA
              </Link>
              <Link href="/tickets" className="hover:text-white transition-colors">
                Portal de Tickets
              </Link>
              <Link href="/transacciones" className="hover:text-white transition-colors">
                Pagos Flow
              </Link>
            </div>

            <div className="text-[11px] text-gray-500 text-center sm:text-right">
              © {new Date().getFullYear()} GSM Soporte. Todos los derechos reservados.
            </div>
          </div>
        </Container>
      </footer>
    </div>
  );
}
