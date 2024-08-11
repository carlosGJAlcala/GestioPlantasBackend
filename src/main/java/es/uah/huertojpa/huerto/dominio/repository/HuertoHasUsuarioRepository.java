package es.uah.huertojpa.huerto.dominio.repository;

import es.uah.huertojpa.huerto.dominio.entidades.HuertoHasUsuario;
import es.uah.huertojpa.huerto.dominio.entidades.HuertoHasUsuarioId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HuertoHasUsuarioRepository extends JpaRepository<HuertoHasUsuario, HuertoHasUsuarioId> {
    Optional<HuertoHasUsuario> findById_HuertoIdhuertoAndId_UsuarioPersonaId(Integer huertoIdhuerto, Integer usuarioPersonaId);

    List<HuertoHasUsuario> findById_UsuarioPersonaId(Integer usuarioPersonaId);


}