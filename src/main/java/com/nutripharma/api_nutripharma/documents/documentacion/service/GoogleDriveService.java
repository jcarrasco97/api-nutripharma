package com.nutripharma.api_nutripharma.documents.documentacion.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

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

    public String subirArchivo(MultipartFile archivo) throws IOException, GeneralSecurityException {
        File fileMetadata = new File();
        fileMetadata.setName(archivo.getOriginalFilename());
        fileMetadata.setParents(Collections.singletonList(folderId));

        InputStreamContent mediaContent = new InputStreamContent(archivo.getContentType(), archivo.getInputStream());

        File file = getDriveService().files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        return file.getId();
    }

    public byte[] descargarArchivo(String fileId) throws IOException, GeneralSecurityException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        getDriveService().files().get(fileId).executeMediaAndDownloadTo(outputStream);
        return outputStream.toByteArray();
    }

    // 👇 MÉTODO NUEVO: Orden de destrucción a Google Drive 👇
    public void eliminarArchivo(String fileId) throws IOException, GeneralSecurityException {
        getDriveService().files().delete(fileId).execute();
    }

    // 👇 NUEVO MÉTODO PARA FOTOS DE AGENDAS 👇
    public String subirEvidencia(MultipartFile archivo, Long consultaId) throws IOException, GeneralSecurityException {
        File fileMetadata = new File();
        // Le ponemos un prefijo para que en Drive no sea un caos de fotos genéricas
        fileMetadata.setName("EVIDENCIA_TURNO_" + consultaId + "_" + archivo.getOriginalFilename());
        fileMetadata.setParents(Collections.singletonList(folderId));

        InputStreamContent mediaContent = new InputStreamContent(archivo.getContentType(), archivo.getInputStream());

        File file = getDriveService().files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        return file.getId();
    }

    // 👇 NUEVO MÉTODO PARA FACTURAS DE GASTOS 👇
    public String subirFactura(MultipartFile archivo, String nombreGenerado) throws IOException, GeneralSecurityException {
        File fileMetadata = new File();
        fileMetadata.setName(nombreGenerado);
        fileMetadata.setParents(Collections.singletonList(folderId));

        InputStreamContent mediaContent = new InputStreamContent(archivo.getContentType(), archivo.getInputStream());

        File file = getDriveService().files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        return file.getId();
    }
}