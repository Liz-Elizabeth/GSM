package com.example.gsm.ui.view.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.gsm.BuildConfig
import com.example.gsm.ui.view.screens.usuario.componentes.TeamworkBlue
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Diálogo modal para escaneo de códigos QR mediante CameraX (Jetpack) y ML Kit.
 * Cumple con el principio KISS: rápido, confiable y con fallback para emulador.
 */
@Composable
fun QrScannerModal(
    onDismiss: () -> Unit,
    onQrDetectado: (String) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var linternaActiva by remember { mutableStateOf(false) }
    var camaraDisponible by remember { mutableStateOf(true) }
    var cameraRef by remember { mutableStateOf<Camera?>(null) }
    var codigoDetectadoPendiente by remember { mutableStateOf<String?>(null) }
    val colorMarco by androidx.compose.animation.animateColorAsState(
        targetValue = if (codigoDetectadoPendiente != null) Color(0xFF22C55E) else Color.White,
        label = "colorMarcoQr"
    )

    LaunchedEffect(codigoDetectadoPendiente) {
        val codigo = codigoDetectadoPendiente ?: return@LaunchedEffect
        kotlinx.coroutines.delay(240)
        onQrDetectado(codigo)
    }

    var tienePermisoCamara by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcherPermiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        tienePermisoCamara = concedido
    }

    LaunchedEffect(Unit) {
        if (!tienePermisoCamara) {
            launcherPermiso.launch(Manifest.permission.CAMERA)
        }
    }

    // Dialog a pantalla completa para que el visor de CameraX ocupe toda la pantalla sin bordes de alerta
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (tienePermisoCamara) {
                if (camaraDisponible) {
                    CameraXPreview(
                        onCamaraLista = { camera -> cameraRef = camera },
                        onCamaraNoDisponible = { camaraDisponible = false },
                        onQrDetectado = { codigo ->
                            if (codigoDetectadoPendiente == null) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                codigoDetectadoPendiente = codigo
                            }
                        }
                    )

                    // Marco guía centrado + indicación breve
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(3.dp, colorMarco),
                            modifier = Modifier.size(240.dp)
                        ) {}

                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.55f)
                        ) {
                            Text(
                                text = "Apunta al rótulo QR de la solicitud",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Cámara trasera no disponible en este dispositivo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Se necesita permiso de cámara para escanear códigos QR.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { launcherPermiso.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = TeamworkBlue)
                    ) {
                        Text("Conceder Permiso")
                    }
                }
            }

            // Barra superior translúcida: Cerrar + Título + Linterna
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cerrar escáner",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Escanear QR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (tienePermisoCamara && camaraDisponible) {
                    IconButton(
                        onClick = {
                            val nuevoEstado = !linternaActiva
                            linternaActiva = nuevoEstado
                            cameraRef?.cameraControl?.enableTorch(nuevoEstado)
                        }
                    ) {
                        Icon(
                            imageVector = if (linternaActiva) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = if (linternaActiva) "Apagar linterna" else "Encender linterna",
                            tint = if (linternaActiva) Color(0xFFFACC15) else Color.White
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            // Botones de prueba rápida visibles solo en builds DEBUG (nunca en producción)
            if (BuildConfig.DEBUG) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Prueba rápida en Emulador (1 Clic):",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onQrDetectado("SOL-2026-1001") },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("1001", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = { onQrDetectado("SOL-2026-1002") },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("1002", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = { onQrDetectado("SOL-2026-9999") },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFCA5A5)),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Text("No Existe", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraXPreview(
    onCamaraLista: (Camera) -> Unit,
    onCamaraNoDisponible: () -> Unit,
    onQrDetectado: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    // AtomicBoolean evita disparos múltiples si llegan dos frames seguidos en el analizador
    val yaDetectado = remember { AtomicBoolean(false) }
    // Restringe ML Kit solo a códigos QR para acelerar la detección por frame
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    // Libera cámara, analizador ML Kit e hilo al cerrar el diálogo para evitar fugas
    DisposableEffect(Unit) {
        onDispose {
            cameraProviderRef?.unbindAll()
            scanner.close()
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                cameraProviderRef = cameraProvider

                if (!cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                    onCamaraNoDisponible()
                    return@addListener
                }

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null && !yaDetectado.get()) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    val valor = barcode.rawValue
                                    if (!valor.isNullOrBlank() && yaDetectado.compareAndSet(false, true)) {
                                        onQrDetectado(valor)
                                        break
                                    }
                                }
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    onCamaraLista(camera)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onCamaraNoDisponible()
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}
