"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import {
  Receipt,
  Search,
  Filter,
  CheckCircle2,
  XCircle,
  Clock,
  ArrowUpRight,
  RefreshCw,
  CreditCard,
  Building2,
  Calendar,
} from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { TransaccionPago } from "@/types/plan";
import { suscribirTransacciones } from "@/services/transaccionesService";
import { Container } from "@/components/common/Container";

export function TransaccionesView() {
  const { usuarioActual, isAdmin } = useAuth();
  const [transacciones, setTransacciones] = useState<TransaccionPago[]>([]);
  const [cargando, setCargando] = useState(true);
  const [filtroEstado, setFiltroEstado] = useState<string>("TODAS");
  const [busqueda, setBusqueda] = useState<string>("");

  useEffect(() => {
    const unsub = suscribirTransacciones(
      (data) => {
        setTransacciones(data);
        setCargando(false);
      },
      (err) => {
        console.warn("Aviso escuchando transacciones:", err);
        setCargando(false);
      }
    );

    return () => unsub();
  }, []);

  const formatearMonto = (monto?: number) => {
    return new Intl.NumberFormat("es-CL", { style: "currency", currency: "CLP" }).format(
      monto || 0
    );
  };

  const formatearFecha = (epoch?: number | null) => {
    if (!epoch) return "Reciente";
    return new Intl.DateTimeFormat("es-CL", {
      dateStyle: "short",
      timeStyle: "short",
    }).format(new Date(epoch));
  };

  const transaccionesPorRol = isAdmin
    ? transacciones
    : transacciones.filter((t) => t.clienteId === usuarioActual.id);

  const transaccionesFiltradas = transaccionesPorRol.filter((t) => {
    const coincideEstado =
      filtroEstado === "TODAS" ? true : t.estado.toUpperCase() === filtroEstado.toUpperCase();
    const texto = `${t.commerceOrder} ${t.token} ${t.flowOrder || ""} ${t.planNombre} ${t.clienteNombre}`.toLowerCase();
    const coincideBusqueda = busqueda.trim() === "" || texto.includes(busqueda.toLowerCase());
    return coincideEstado && coincideBusqueda;
  });

  const totalPagadas = transaccionesPorRol.filter((t) => t.estado === "PAGADA");
  const totalMontoPagado = totalPagadas.reduce((acc, curr) => acc + (curr.montoClp || 0), 0);

  return (
    <Container className="py-10 sm:py-14">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-blue-50 text-[#0066FF] border border-blue-100 mb-2">
            <Receipt className="w-3.5 h-3.5" />
            Registro Oficial de Transacciones Flow Chile
          </div>
          <h1 className="text-2xl sm:text-3xl font-black text-gray-900 tracking-tight">
            Historial de Pagos y Facturación
          </h1>
          <p className="text-xs sm:text-sm text-gray-500 mt-1">
            Auditoría de identificadores de transacción, órdenes Flow y vouchers de servicio.
          </p>
        </div>

        <Link
          href="/planes"
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl font-bold text-xs text-white bg-[#0066FF] hover:bg-[#0052CC] transition-colors shadow-xs"
        >
          <CreditCard className="w-3.5 h-3.5" />
          <span>Ver Planes & SLA</span>
        </Link>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
        <div className="bg-white rounded-2xl p-5 border border-gray-200/80 shadow-xs">
          <span className="text-xs font-semibold text-gray-500 block mb-1">
            Total Transacciones Registradas
          </span>
          <span className="text-2xl font-black text-gray-900">{transaccionesPorRol.length}</span>
          <span className="block text-[11px] text-gray-400 mt-1">
            Almacenadas en Cloud Firestore
          </span>
        </div>

        <div className="bg-white rounded-2xl p-5 border border-gray-200/80 shadow-xs">
          <span className="text-xs font-semibold text-emerald-600 block mb-1">
            Pagos Aprobados (Webpay Plus)
          </span>
          <span className="text-2xl font-black text-emerald-700">{totalPagadas.length}</span>
          <span className="block text-[11px] text-emerald-600/80 mt-1">
            Conformidad bancaria Flow
          </span>
        </div>

        <div className="bg-white rounded-2xl p-5 border border-gray-200/80 shadow-xs">
          <span className="text-xs font-semibold text-[#0066FF] block mb-1">
            Monto Total Procesado
          </span>
          <span className="text-2xl font-black text-gray-900">
            {formatearMonto(totalMontoPagado)}
          </span>
          <span className="block text-[11px] text-blue-600 mt-1">Facturación verificada</span>
        </div>
      </div>

      <div className="bg-white rounded-2xl p-4 border border-gray-200/80 shadow-xs mb-6 flex flex-col sm:flex-row gap-3 items-center justify-between">
        <div className="relative w-full sm:w-96">
          <input
            type="text"
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            placeholder="Buscar por orden, ID Flow, token o cliente..."
            className="w-full pl-9 pr-4 py-2 rounded-xl border border-gray-200 text-xs font-medium focus:outline-hidden focus:ring-2 focus:ring-[#0066FF] bg-gray-50/50"
          />
          <Search className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto justify-end">
          <span className="text-xs font-semibold text-gray-500 flex items-center gap-1">
            <Filter className="w-3.5 h-3.5" />
            Estado:
          </span>
          <select
            value={filtroEstado}
            onChange={(e) => setFiltroEstado(e.target.value)}
            className="px-3 py-2 rounded-xl border border-gray-200 text-xs font-semibold text-gray-700 bg-white focus:outline-hidden focus:ring-2 focus:ring-[#0066FF]"
          >
            <option value="TODAS">Todas</option>
            <option value="PAGADA">Pagadas</option>
            <option value="PENDIENTE">Pendientes</option>
            <option value="RECHAZADA">Rechazadas</option>
          </select>
        </div>
      </div>

      {cargando ? (
        <div className="bg-white rounded-2xl p-12 border border-gray-200 text-center">
          <RefreshCw className="w-6 h-6 animate-spin text-[#0066FF] mx-auto mb-2" />
          <p className="text-xs font-semibold text-gray-500">Cargando transacciones desde Firestore...</p>
        </div>
      ) : transaccionesFiltradas.length === 0 ? (
        <div className="bg-white rounded-2xl p-12 border border-gray-200 text-center">
          <Receipt className="w-10 h-10 text-gray-300 mx-auto mb-2" />
          <h3 className="text-sm font-bold text-gray-800">No se encontraron transacciones</h3>
          <p className="text-xs text-gray-500 mt-1">
            No hay registros que coincidan con los filtros aplicados.
          </p>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-gray-200/80 shadow-xs overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-gray-50/70 border-b border-gray-200 text-gray-500 font-semibold uppercase tracking-wider text-[10px]">
                <tr>
                  <th className="py-3 px-4">Orden / ID Flow</th>
                  <th className="py-3 px-4">Plan & Beneficiario</th>
                  <th className="py-3 px-4">Monto CLP</th>
                  <th className="py-3 px-4">Medio de Pago</th>
                  <th className="py-3 px-4">Fecha</th>
                  <th className="py-3 px-4">Estado</th>
                  <th className="py-3 px-4 text-right">Acción</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {transaccionesFiltradas.map((t) => (
                  <tr key={t.id || t.token} className="hover:bg-gray-50/80 transition-colors">
                    <td className="py-3.5 px-4">
                      <div className="font-mono font-bold text-gray-900">{t.commerceOrder}</div>
                      <div className="text-[11px] text-gray-500 flex items-center gap-1 font-mono">
                        Flow #{t.flowOrder || "N/A"}
                      </div>
                    </td>

                    <td className="py-3.5 px-4">
                      <div className="font-bold text-gray-900">{t.planNombre}</div>
                      <div className="text-[11px] text-gray-500 flex items-center gap-1">
                        <Building2 className="w-3 h-3 text-gray-400" />
                        <span>{t.clienteNombre}</span>
                      </div>
                    </td>

                    <td className="py-3.5 px-4 font-bold text-gray-900 text-sm">
                      {formatearMonto(t.montoClp)}
                    </td>

                    <td className="py-3.5 px-4 text-gray-600">
                      <span className="inline-flex items-center gap-1 font-medium">
                        <CreditCard className="w-3.5 h-3.5 text-gray-400" />
                        {t.medioPago || "Webpay Plus"}
                      </span>
                    </td>

                    <td className="py-3.5 px-4 text-gray-500">
                      <div className="flex items-center gap-1">
                        <Calendar className="w-3.5 h-3.5 text-gray-400" />
                        <span>{formatearFecha(t.fechaPago || t.fechaCreacion)}</span>
                      </div>
                    </td>

                    <td className="py-3.5 px-4">
                      {t.estado === "PAGADA" ? (
                        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold bg-emerald-100 text-emerald-800">
                          <CheckCircle2 className="w-3 h-3" />
                          PAGADA
                        </span>
                      ) : t.estado === "RECHAZADA" ? (
                        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold bg-rose-100 text-rose-800">
                          <XCircle className="w-3 h-3" />
                          RECHAZADA
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-[11px] font-bold bg-amber-100 text-amber-800">
                          <Clock className="w-3 h-3" />
                          PENDIENTE
                        </span>
                      )}
                    </td>

                    <td className="py-3.5 px-4 text-right">
                      <Link
                        href={`/planes/retorno?token=${t.token}`}
                        className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg text-xs font-semibold text-[#0066FF] hover:bg-blue-50 transition-colors"
                      >
                        <span>Comprobante</span>
                        <ArrowUpRight className="w-3.5 h-3.5" />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </Container>
  );
}
