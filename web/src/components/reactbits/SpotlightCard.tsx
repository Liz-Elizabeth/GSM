"use client";

import React, { useRef, useState, useCallback } from "react";

interface SpotlightCardProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  className?: string;
  spotlightColor?: string;
  borderColor?: string;
}

/**
 * Componente React Bits: SpotlightCard
 * Efecto de iluminación interactiva de alta fidelidad basado en coordenadas del cursor.
 * Aplica GPU acceleration, contraste accesible y fallback táctil.
 */
export function SpotlightCard({
  children,
  className = "",
  spotlightColor = "rgba(0, 102, 255, 0.14)",
  borderColor = "rgba(0, 102, 255, 0.35)",
  ...props
}: SpotlightCardProps) {
  const cardRef = useRef<HTMLDivElement>(null);
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [opacity, setOpacity] = useState(0);

  const handleMouseMove = useCallback((e: React.MouseEvent<HTMLDivElement>) => {
    if (!cardRef.current) return;
    const rect = cardRef.current.getBoundingClientRect();
    setPosition({
      x: e.clientX - rect.left,
      y: e.clientY - rect.top,
    });
  }, []);

  const handleMouseEnter = useCallback(() => {
    setOpacity(1);
  }, []);

  const handleMouseLeave = useCallback(() => {
    setOpacity(0);
  }, []);

  return (
    <div
      ref={cardRef}
      onMouseMove={handleMouseMove}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
      className={`relative overflow-hidden rounded-2xl border border-gray-200/80 bg-white transition-all duration-300 hover:shadow-xl hover:-translate-y-1 ${className}`}
      {...props}
    >
      {/* Resplandor del Spotlight interno */}
      <div
        className="pointer-events-none absolute -inset-px transition-opacity duration-300 z-10"
        style={{
          opacity,
          background: `radial-gradient(600px circle at ${position.x}px ${position.y}px, ${spotlightColor}, transparent 40%)`,
        }}
        aria-hidden="true"
      />

      {/* Borde brillante iluminado por el cursor */}
      <div
        className="pointer-events-none absolute -inset-px rounded-2xl transition-opacity duration-300 z-10"
        style={{
          opacity,
          border: `1.5px solid ${borderColor}`,
          maskImage: `radial-gradient(300px circle at ${position.x}px ${position.y}px, black, transparent)`,
          WebkitMaskImage: `radial-gradient(300px circle at ${position.x}px ${position.y}px, black, transparent)`,
        }}
        aria-hidden="true"
      />

      <div className="relative z-20 h-full">{children}</div>
    </div>
  );
}
