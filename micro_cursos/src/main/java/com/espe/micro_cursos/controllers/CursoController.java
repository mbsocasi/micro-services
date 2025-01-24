package com.espe.micro_cursos.controllers;

import com.espe.micro_cursos.models.Usuario;
import com.espe.micro_cursos.models.entities.Curso;
import com.espe.micro_cursos.services.CursoService;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    @Autowired
    private CursoService cursoService;

    // Obtener todos los cursos
    @GetMapping
    public ResponseEntity<List<Curso>> findAll() {
        List<Curso> cursos = cursoService.findAll();
        return new ResponseEntity<>(cursos, HttpStatus.OK);
    }

    // Buscar un curso por ID
    @GetMapping("/{id}")
    public ResponseEntity<Curso> findById(@PathVariable Long id) {
        Optional<Curso> curso = cursoService.findById(id);
        return curso.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Crear un nuevo curso
    @PostMapping
    public ResponseEntity<Curso> save(@RequestBody Curso curso) {
        Curso nuevoCurso = cursoService.save(curso);
        return new ResponseEntity<>(nuevoCurso, HttpStatus.CREATED);
    }

    // Actualizar un curso existente
    @PutMapping("/{id}")
    public ResponseEntity<Curso> update(@PathVariable Long id, @RequestBody Curso curso) {
        Optional<Curso> cursoExistente = cursoService.findById(id);
        if (cursoExistente.isPresent()) {
            curso.setId(id); // Asegurarse de que el ID no cambie
            Curso cursoActualizado = cursoService.save(curso);
            return new ResponseEntity<>(cursoActualizado, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Eliminar un curso por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        Optional<Curso> cursoExistente = cursoService.findById(id);
        if (cursoExistente.isPresent()) {
            cursoService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Método para matricular un usuario en un curso
    @PostMapping("/{id}/matricular")
    public ResponseEntity<?> addUser(@PathVariable Long id, @RequestBody Usuario usuario) {
        Optional<Usuario> optionalUsuario;
        try {
            // Validación de existencia de usuario en el microservicio de usuarios a través de Feign
            optionalUsuario = cursoService.addUser(usuario, id);
        } catch (FeignException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("error", "Usuario no encontrado: " + ex.getMessage()));
        }

        if (optionalUsuario.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(optionalUsuario.get());
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{cursoId}/desmatricular/{usuarioId}")
    public ResponseEntity<?> removeUser(@PathVariable Long cursoId, @PathVariable Long usuarioId) {
        Optional<Curso> optionalCurso = cursoService.findById(cursoId);

        if (optionalCurso.isPresent()) {
            Optional<Usuario> usuarioDesmatriculado = cursoService.removeUser(usuarioId, cursoId);

            if (usuarioDesmatriculado.isPresent()) {
                // Devuelve un mensaje de éxito si el usuario fue desmatriculado
                return ResponseEntity.ok("Usuario desmatriculado correctamente");
            } else {
                // Si no se encuentra al usuario, devuelve un mensaje de error
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado en este curso");
            }
        }

        // Si el curso no existe, devuelve un mensaje de error
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Curso no encontrado");
    }




}