package com.aegis.erp.modules.seguridad.profile.controller;

import com.aegis.erp.modules.seguridad.profile.dto.ProfileResponse;
import com.aegis.erp.modules.seguridad.profile.dto.ProfileUpdateRequest;
import com.aegis.erp.modules.seguridad.profile.service.ProfilePhoto;
import com.aegis.erp.modules.seguridad.profile.service.ProfileService;

import jakarta.validation.Valid;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/security/profile")
public class ProfileController {
    private final ProfileService profiles;

    public ProfileController(ProfileService profiles) {
        this.profiles = profiles;
    }

    @GetMapping
    public ProfileResponse get(JwtAuthenticationToken authentication) {
        return profiles.get(authentication.getName());
    }

    @PutMapping
    public ProfileResponse update(
            @Valid @RequestBody ProfileUpdateRequest request,
            JwtAuthenticationToken authentication) {
        return profiles.update(authentication.getName(), request);
    }

    @GetMapping("/photo")
    public ResponseEntity<byte[]> photo(JwtAuthenticationToken authentication) {
        return profiles.photo(authentication.getName())
                .map(photo -> image(photo))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> updatePhoto(
            @RequestPart("file") MultipartFile file,
            JwtAuthenticationToken authentication) {
        return image(profiles.updatePhoto(authentication.getName(), file));
    }

    @DeleteMapping("/photo")
    public ResponseEntity<Void> deletePhoto(JwtAuthenticationToken authentication) {
        profiles.deletePhoto(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<byte[]> image(ProfilePhoto photo) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .body(photo.content());
    }
}
