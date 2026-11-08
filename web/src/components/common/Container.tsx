import React from "react";

export interface ContainerProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  className?: string;
  as?: React.ElementType;
  clean?: boolean;
}

/**
 * Componente unificado de Contenedor de Ancho Adaptable.
 * - En pantallas móviles: px-4
 * - En tablets: sm:px-6
 * - En desktops estándar: lg:px-10
 * - En monitores anchos (1440px - 1920px): 2xl:px-16
 * - En ultra-wides (2560px+): max-w-[1920px] centrado con fondo siempre borde a borde
 */
export function Container({
  children,
  className = "",
  as: Component = "div",
  clean = false,
  ...props
}: ContainerProps) {
  return (
    <Component
      className={`w-full mx-auto max-w-[1920px] ${clean ? "" : "px-4 sm:px-6 lg:px-10 2xl:px-16"} ${className}`}
      {...props}
    >
      {children}
    </Component>
  );
}
