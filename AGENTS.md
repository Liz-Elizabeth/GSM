# Agent Guidelines & Workflow (Elephant-Goldfish Model - EGM)

Este proyecto utiliza el flujo de trabajo **Elephant-Goldfish Model (EGM)** para el desarrollo guiado por especificaciones (Spec-Driven Development).

## Ciclo de Desarrollo y Reglas

1. **Nuevas Funcionalidades y Cambios Arquitectónicos**:
   - **Exploración**: Utiliza `/egm:egm-brainstorm <descripción>` para definir requerimientos y generar `concepts_brief.md`.
   - **Especificación**: Utiliza `/egm:egm-draft` para estructurar la especificación técnica en `docs/designs/`.
   - **Revisión Goldfish**: Utiliza `/egm:egm-review <ruta_al_borrador>` para someter el diseño a subagentes independientes sin contexto previo que evalúen coherencia, riesgos y límites de implementación.
   - **Resolución**: Utiliza `/egm:egm-resolve` para incorporar correcciones.
   - **Implementación**: La IA solo debe escribir código tras la aprobación explícita del diseño por parte del usuario.

2. **Correcciones Menores (Bug Fixes)**:
   - Parches pequeños y correcciones de errores puntuales pueden implementarse directamente sin requerir un documento de diseño completo.

3. **Control de Desviación (Drift Detection)**:
   - Utiliza `/egm:egm-drift-report` para contrastar el código implementado contra los documentos de diseño vigentes.

4. **Directorio de Diseños**:
   - Los documentos aprobados se preservan en `docs/designs/`.

## Habilidades de Diseño y Frontend (UI Skills - ibelick)

Este proyecto cuenta con las directrices y habilidades de **UI Skills** para mantener una interfaz limpia, accesible y de alto rendimiento:

- **`/baseline-ui [archivo]`**: Limpieza rápida de código UI eliminando patrones deficientes (espaciados, jerarquía, tipografía y Tailwind CSS).
- **`/improve-ui`**: Auditoría de la superficie del producto contra los patrones existentes para proponer mejoras concretas.
- **`/fixing-accessibility`**: Detección y corrección de accesibilidad (ARIA, navegación por teclado, contraste y WCAG).
- **`/fixing-motion-performance`**: Corrección de problemas de rendimiento en animaciones, jank y layout thrashing (forzando animación solo en propiedades del compositor: `transform`, `opacity`).
- **`/create-design-md`**: Extracción o generación de `DESIGN.md` para documentar el sistema de diseño del proyecto.

## Habilidades Especializadas Disponibles (SkillMD & Agentes)

El proyecto cuenta con las siguientes habilidades integradas en `.agents/skills/`:

### Frontend & UI
- **`frontend-design`**: Diseño de interfaces de nivel producción con tipografía intencional, jerarquía y motion design.
- **`senior-frontend`**: Arquitectura avanzada de frontend, optimización de renderizado, descomposición de componentes y estado reactivo.
- **`expo-ui`**: Desarrollo UI multiplataforma con Expo y React Native.
- **`baseline-ui`**: Deslop rápido de interfaces eliminando inconsistencias en CSS y espaciados.

### Arquitectura Móvil & Android
- **`mvvm`**: Patrones de arquitectura Model-View-ViewModel, separación de responsabilidades y flujo unidireccional de datos.
- **`navigation-3`**: Buenas prácticas de navegación modular y tipado seguro en Android Jetpack Navigation / Compose.

### Aseguramiento de Calidad (QA & Testing)
- **`senior-qa`**: Estrategia integral de testing, cobertura de edge cases y pruebas de integración/E2E.
- **`mobile-qa`**: Matriz de pruebas para dispositivos móviles, testing de ciclo de vida y rendimiento en Android/iOS.

### Flujo Guiado por Especificaciones (SDD)
- **`spec-driven-development`**: Metodología de desarrollo basado en especificaciones técnicas previas al código.
- **`spec-driven-workflow`**: Orquestación del ciclo de vida spec -> review -> build -> verify.
