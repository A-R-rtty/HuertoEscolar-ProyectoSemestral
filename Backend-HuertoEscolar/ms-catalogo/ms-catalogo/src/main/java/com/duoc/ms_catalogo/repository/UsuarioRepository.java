package com.duoc.ms_catalogo.repository;

import com.duoc.ms_catalogo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {}