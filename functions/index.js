/**
 * Cloud Functions v2 para disparar notificaciones Push FCM reales cuando:
 * 1) Se asigna un técnico a un ticket.
 * 2) Se registra un nuevo mensaje público en el historial/chat del ticket.
 *
 * Despliegue: `firebase deploy --only functions` (requiere Firebase CLI autenticado).
 */
const { onDocumentUpdated, onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();

exports.notificarAsignacionTicket = onDocumentUpdated("solicitudes/{codigo}", async (event) => {
  const antes = event.data.before.data();
  const despues = event.data.after.data();
  if (!antes || !despues || !despues.tecnicoId || antes.tecnicoId === despues.tecnicoId) return;

  const token = despues.tecnicoFcmToken;
  if (!token) return;

  await admin.messaging().send({
    token,
    notification: {
      title: `Ticket #${despues.codigoSeguimiento || event.params.codigo} asignado`,
      body: despues.titulo || "Tienes un nuevo ticket asignado.",
    },
    data: {
      tipo: "ASIGNACION",
      codigoSeguimiento: String(despues.codigoSeguimiento || event.params.codigo),
    },
  });
});

exports.notificarNuevoMensajeChat = onDocumentCreated("solicitudes/{codigo}/historial/{eventoId}", async (event) => {
  const msg = event.data.data();
  if (!msg || msg.esComentarioInterno) return;

  const ticketSnap = await admin.firestore().doc(`solicitudes/${event.params.codigo}`).get();
  const ticket = ticketSnap.data();
  if (!ticket) return;

  // Si el mensaje es del cliente, notifica al técnico; si es de soporte, notifica al cliente
  const tokenDestino = msg.perfilUsuario === "CLIENTE" ? ticket.tecnicoFcmToken : ticket.clienteFcmToken;
  if (!tokenDestino) return;

  const textoClaro =
    msg.comentario && !String(msg.comentario).startsWith("ENC:")
      ? String(msg.comentario)
      : "Tienes un nuevo mensaje en tu ticket.";

  await admin.messaging().send({
    token: tokenDestino,
    notification: {
      title: `Nuevo mensaje en #${ticket.codigoSeguimiento || event.params.codigo}`,
      body: textoClaro,
    },
    data: {
      tipo: "CHAT_MENSAJE",
      codigoSeguimiento: String(ticket.codigoSeguimiento || event.params.codigo),
    },
  });
});
