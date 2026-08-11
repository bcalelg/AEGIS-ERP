package com.aegis.erp.modules.seguridad.menu.controller;
import com.aegis.erp.modules.seguridad.menu.dto.ModuloMenuResponse;
import com.aegis.erp.modules.seguridad.menu.service.MenuService;
import java.util.List;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/security") public class MenuController {
private final MenuService menuService;public MenuController(MenuService menuService){this.menuService=menuService;}
@GetMapping("/menu") public List<ModuloMenuResponse> menu(JwtAuthenticationToken authentication){return menuService.findMenuForUser(authentication.getName());}}
