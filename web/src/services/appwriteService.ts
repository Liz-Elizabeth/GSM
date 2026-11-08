import { ID } from "appwrite";
import { appwriteStorage, APPWRITE_CONFIG } from "@/config/appwrite";
import { Evidencia } from "@/types/solicitud";

export const TAMANO_MAXIMO_BYTES = 15 * 1024 * 1024; // 15 MB (OWASP Input Validation)

/**
 * Sube un archivo real a Appwrite Storage y retorna la estructura Evidencia lista para Firestore.
 */
export async function subirEvidenciaAAppwrite(
  file: File,
  descripcion?: string
): Promise<Evidencia> {
  if (file.size > TAMANO_MAXIMO_BYTES) {
    throw new Error("El archivo supera el tamaño máximo permitido de 15 MB.");
  }

  const fileId = ID.unique();

  // Subir al bucket oficial de Appwrite
  const uploaded = await appwriteStorage.createFile(
    APPWRITE_CONFIG.bucketId,
    fileId,
    file
  );

  // Construir la URL directa de visualización
  const uriRemota = `${APPWRITE_CONFIG.endpoint}/storage/buckets/${APPWRITE_CONFIG.bucketId}/files/${uploaded.$id}/view?project=${APPWRITE_CONFIG.projectId}`;

  const evidencia: Evidencia = {
    id: uploaded.$id,
    nombreArchivo: file.name,
    tipoArchivo: file.type || "application/octet-stream",
    tamanoBytes: file.size,
    uriRemota: uriRemota,
    fechaIncorporacion: Date.now(),
    descripcion: descripcion?.trim() || undefined,
  };

  return evidencia;
}

/**
 * Retorna la URL directa de descarga o visualización de un archivo en Appwrite Storage.
 */
export function obtenerUrlDescargaAppwrite(fileId: string): string {
  return `${APPWRITE_CONFIG.endpoint}/storage/buckets/${APPWRITE_CONFIG.bucketId}/files/${fileId}/view?project=${APPWRITE_CONFIG.projectId}`;
}
