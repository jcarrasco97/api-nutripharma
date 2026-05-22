package com.nutripharma.api_nutripharma.documents.documentacion.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {

    @Value("${google.drive.folder.id}")
    private String folderId;

    @Value("${google.drive.client.id}")
    private String clientId;

    @Value("${google.drive.client.secret}")
    private String clientSecret;

    @Value("${google.drive.refresh.token}")
    private String refreshToken;

    private static final String APPLICATION_NAME = "NutriPharma_App";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String FOLDER_MIME = "application/vnd.google-apps.folder";

    private Drive getDriveService() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // =========================================================================
    // 📂 MOTOR DE CARPETAS (lazy creation)
    // =========================================================================

    /**
     * Busca una subcarpeta por nombre dentro de un padre.
     * Si no existe, la crea automáticamente.
     */
    private String findOrCreateFolder(Drive drive, String folderName, String parentId) throws IOException {
        // 1. Buscar carpeta existente
        String query = String.format(
                "mimeType='%s' and name='%s' and '%s' in parents and trashed=false",
                FOLDER_MIME, folderName.replace("'", "\\'"), parentId
        );

        FileList result = drive.files().list()
                .setQ(query)
                .setFields("files(id, name)")
                .setPageSize(1)
                .execute();

        List<File> files = result.getFiles();
        if (files != null && !files.isEmpty()) {
            return files.get(0).getId();
        }

        // 2. No existe → crearla
        File folderMetadata = new File();
        folderMetadata.setName(folderName);
        folderMetadata.setMimeType(FOLDER_MIME);
        folderMetadata.setParents(Collections.singletonList(parentId));

        File created = drive.files().create(folderMetadata)
                .setFields("id")
                .execute();

        return created.getId();
    }

    /**
     * Resuelve una ruta multi-nivel de carpetas (ej: "Documentos", "Todos")
     * creando las intermedias si no existen. Devuelve el folderId de la hoja.
     */
    private String resolveNestedFolder(Drive drive, String... path) throws IOException {
        String currentParent = folderId; // raíz configurada
        for (String segment : path) {
            currentParent = findOrCreateFolder(drive, segment, currentParent);
        }
        return currentParent;
    }

    // =========================================================================
    // 📄 SUBIDA DE DOCUMENTOS
    // =========================================================================

    public String subirArchivo(MultipartFile archivo, String... folderPath) throws IOException, GeneralSecurityException {
        Drive drive = getDriveService();

        String targetFolderId = (folderPath != null && folderPath.length > 0)
                ? resolveNestedFolder(drive, folderPath)
                : folderId;

        File fileMetadata = new File();
        fileMetadata.setName(archivo.getOriginalFilename());
        fileMetadata.setParents(Collections.singletonList(targetFolderId));

        InputStreamContent mediaContent = new InputStreamContent(archivo.getContentType(), archivo.getInputStream());

        File file = drive.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        return file.getId();
    }

    // =========================================================================
    // 📸 SUBIDA DE EVIDENCIAS (Fotos de Agendas)
    // =========================================================================

    public String subirEvidencia(MultipartFile archivo, String nombreArchivo, String... folderPath) throws IOException, GeneralSecurityException {
        Drive drive = getDriveService();

        String targetFolderId = (folderPath != null && folderPath.length > 0)
                ? resolveNestedFolder(drive, folderPath)
                : folderId;

        File fileMetadata = new File();
        fileMetadata.setName(nombreArchivo);
        fileMetadata.setParents(Collections.singletonList(targetFolderId));

        InputStreamContent mediaContent = new InputStreamContent(archivo.getContentType(), archivo.getInputStream());

        File file = drive.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        return file.getId();
    }

    // =========================================================================
    // 🧾 SUBIDA DE FACTURAS
    // =========================================================================

    public String subirFactura(MultipartFile archivo, String nombreGenerado, String... folderPath) throws IOException, GeneralSecurityException {
        Drive drive = getDriveService();

        String targetFolderId = (folderPath != null && folderPath.length > 0)
                ? resolveNestedFolder(drive, folderPath)
                : folderId;

        File fileMetadata = new File();
        fileMetadata.setName(nombreGenerado);
        fileMetadata.setParents(Collections.singletonList(targetFolderId));

        InputStreamContent mediaContent = new InputStreamContent(archivo.getContentType(), archivo.getInputStream());

        File file = drive.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        return file.getId();
    }

    // =========================================================================
    // ⬇️ DESCARGA Y 🗑️ ELIMINACIÓN (no cambian — usan fileId directamente)
    // =========================================================================

    public byte[] descargarArchivo(String fileId) throws IOException, GeneralSecurityException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        getDriveService().files().get(fileId).executeMediaAndDownloadTo(outputStream);
        return outputStream.toByteArray();
    }

    public void eliminarArchivo(String fileId) throws IOException, GeneralSecurityException {
        getDriveService().files().delete(fileId).execute();
    }
}