package com.aegis.erp.modules.seguridad.menu.service;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import com.aegis.erp.modules.seguridad.menu.dto.*;
import com.aegis.erp.modules.seguridad.menu.repository.RoleOpcionRepository;
import com.aegis.erp.modules.seguridad.usuario.repository.UsuarioRepository;
import java.util.*;
import org.junit.jupiter.api.*;
class MenuServiceTest {
UsuarioRepository usuarios=mock(UsuarioRepository.class);RoleOpcionRepository rolesOpciones=mock(RoleOpcionRepository.class);MenuService service=new MenuService(usuarios,rolesOpciones);
@Test void administradorReturnsTenAssignedOptionsInOrder(){when(usuarios.findRoleIdByIdUsuario("Administrador")).thenReturn(Optional.of(1L));List<MenuPermissionRow> rows=new ArrayList<>();for(int i=1;i<=8;i++)rows.add(row(1L,"Seguridad",1,1L,"Parametros Generales",1,(long)i,"Opcion "+i,"opcion"+i+".php",i,1,1,1,1,1,1));for(int i=1;i<=2;i++)rows.add(row(1L,"Seguridad",1,2L,"Acciones",2,(long)(8+i),"Accion "+i,"accion"+i+".php",i,1,0,0,1,0,1));when(rolesOpciones.findMenuRowsByRoleId(1L)).thenReturn(rows);List<ModuloMenuResponse> result=service.findMenuForUser("Administrador");assertThat(result).hasSize(1);assertThat(result.getFirst().nombre()).isEqualTo("Seguridad");assertThat(result.getFirst().menus()).extracting(MenuResponse::ordenMenu).containsExactly(1,2);assertThat(result.getFirst().menus().stream().flatMap(menu->menu.opciones().stream())).hasSize(10);OpcionResponse action=result.getFirst().menus().get(1).opciones().getFirst();assertThat(action.permisos()).isEqualTo(new PermisosResponse(true,false,false,true,false,true));}
@Test void roleWithoutOptionsReturnsEmpty(){when(usuarios.findRoleIdByIdUsuario("system")).thenReturn(Optional.of(2L));when(rolesOpciones.findMenuRowsByRoleId(2L)).thenReturn(List.of());assertThat(service.findMenuForUser("system")).isEmpty();verify(rolesOpciones).findMenuRowsByRoleId(2L);verify(rolesOpciones,never()).findMenuRowsByRoleId(1L);}
@Test void missingUserReturnsEmptyWithoutQueryingAnotherRole(){when(usuarios.findRoleIdByIdUsuario("removed")).thenReturn(Optional.empty());assertThat(service.findMenuForUser("removed")).isEmpty();verifyNoInteractions(rolesOpciones);}
private MenuPermissionRow row(Long idModulo,String modulo,Integer ordenModulo,Long idMenu,String menu,Integer ordenMenu,Long idOpcion,String opcion,String pagina,Integer ordenOpcion,Integer consultar,Integer alta,Integer baja,Integer cambio,Integer imprimir,Integer exportar){return new MenuPermissionRow(idModulo,modulo,ordenModulo,idMenu,menu,ordenMenu,idOpcion,opcion,pagina,ordenOpcion,consultar,alta,baja,cambio,imprimir,exportar);}}
