package com.aegis.erp.modules.seguridad.profile.service;

import com.aegis.erp.common.exception.BusinessConflictException;
import com.aegis.erp.common.exception.InvalidProfilePhotoException;
import com.aegis.erp.common.exception.ResourceNotFoundException;
import com.aegis.erp.modules.seguridad.profile.dto.ProfileResponse;
import com.aegis.erp.modules.seguridad.profile.dto.ProfileUpdateRequest;
import com.aegis.erp.modules.seguridad.usuario.entity.Usuario;
import com.aegis.erp.modules.seguridad.usuario.repository.UsuarioRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

@Service
public class ProfileService {
    public static final long MAX_PHOTO_BYTES = 2L * 1024 * 1024;

    private final UsuarioRepository usuarios;
    private final Clock clock;

    public ProfileService(UsuarioRepository usuarios, Clock clock) {
        this.usuarios = usuarios;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ProfileResponse get(String authenticatedUser) {
        return response(find(authenticatedUser));
    }

    @Transactional
    public ProfileResponse update(String authenticatedUser, ProfileUpdateRequest request) {
        Usuario usuario = find(authenticatedUser);
        String email = request.correoElectronico().trim().toLowerCase(Locale.ROOT);
        String phone = nullable(request.telefonoMovil());
        if (usuarios.existsByCorreoElectronicoAndIdUsuarioNot(email, authenticatedUser)) {
            throw new BusinessConflictException(
                    "Ya existe un usuario con ese correo electrónico.");
        }
        if (phone != null && usuarios.existsByTelefonoMovilAndIdUsuarioNot(phone, authenticatedUser)) {
            throw new BusinessConflictException("Ya existe un usuario con ese teléfono móvil.");
        }
        usuario.modificarPerfil(
                email,
                phone,
                LocalDateTime.now(clock));
        try {
            return response(usuarios.saveAndFlush(usuario));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessConflictException(
                    "El correo electrónico o teléfono móvil ya pertenece a otro usuario.");
        }
    }

    @Transactional
    public ProfilePhoto updatePhoto(String authenticatedUser, MultipartFile file) {
        byte[] bytes = validatedBytes(file);
        String contentType = detectContentType(bytes);
        Usuario usuario = find(authenticatedUser);
        usuario.cambiarFotografia(Arrays.copyOf(bytes, bytes.length), LocalDateTime.now(clock));
        usuarios.saveAndFlush(usuario);
        return new ProfilePhoto(bytes, contentType);
    }

    @Transactional(readOnly = true)
    public Optional<ProfilePhoto> photo(String authenticatedUser) {
        byte[] bytes = find(authenticatedUser).getFotografia();
        if (bytes == null || bytes.length == 0) return Optional.empty();
        return Optional.of(new ProfilePhoto(bytes, detectContentType(bytes)));
    }

    @Transactional
    public void deletePhoto(String authenticatedUser) {
        Usuario usuario = find(authenticatedUser);
        usuario.cambiarFotografia(null, LocalDateTime.now(clock));
        usuarios.saveAndFlush(usuario);
    }

    private byte[] validatedBytes(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidProfilePhotoException("Debe seleccionar una fotografía.");
        }
        if (file.getSize() > MAX_PHOTO_BYTES) {
            throw new InvalidProfilePhotoException("La fotografía no puede exceder 2 MB.");
        }
        String declared = file.getContentType();
        if (declared == null
                || !java.util.Set.of("image/jpeg", "image/png", "image/webp")
                        .contains(declared.toLowerCase(Locale.ROOT))) {
            throw new InvalidProfilePhotoException("Use una fotografía JPEG, PNG o WebP.");
        }
        try {
            byte[] bytes = file.getBytes();
            String detected = detectContentType(bytes);
            if (!detected.equalsIgnoreCase(declared)) {
                throw new InvalidProfilePhotoException(
                        "El contenido del archivo no coincide con su formato declarado.");
            }
            return bytes;
        } catch (IOException exception) {
            throw new UncheckedIOException("No fue posible leer la fotografía.", exception);
        }
    }

    static String detectContentType(byte[] bytes) {
        if (bytes != null
                && bytes.length >= 3
                && (bytes[0] & 0xff) == 0xff
                && (bytes[1] & 0xff) == 0xd8
                && (bytes[2] & 0xff) == 0xff) {
            return "image/jpeg";
        }
        byte[] png = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
        if (bytes != null && bytes.length >= png.length) {
            boolean matches = true;
            for (int index = 0; index < png.length; index++) matches &= bytes[index] == png[index];
            if (matches) return "image/png";
        }
        if (bytes != null
                && bytes.length >= 12
                && ascii(bytes, 0, "RIFF")
                && ascii(bytes, 8, "WEBP")) {
            return "image/webp";
        }
        throw new InvalidProfilePhotoException("El archivo no contiene una imagen JPEG, PNG o WebP válida.");
    }

    private static boolean ascii(byte[] bytes, int offset, String expected) {
        for (int index = 0; index < expected.length(); index++) {
            if (bytes[offset + index] != (byte) expected.charAt(index)) return false;
        }
        return true;
    }

    private Usuario find(String authenticatedUser) {
        return usuarios.findForMaintenance(authenticatedUser)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado."));
    }

    private ProfileResponse response(Usuario usuario) {
        boolean hasPhoto = usuario.getFotografia() != null && usuario.getFotografia().length > 0;
        return new ProfileResponse(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreoElectronico(),
                usuario.getTelefonoMovil(),
                usuario.getFechaNacimiento(),
                usuario.getGenero().getNombre(),
                usuario.getStatus().getNombre(),
                usuario.getSucursal().getEmpresa().getNombre(),
                usuario.getSucursal().getNombre(),
                usuario.getRole().getNombre(),
                hasPhoto,
                hasPhoto ? "/api/security/profile/photo" : null);
    }

    private String nullable(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
