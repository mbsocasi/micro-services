package com.espe.micro_cursos.services;

import com.espe.micro_cursos.clients.UsuarioClientRest;
import com.espe.micro_cursos.models.Usuario;
import com.espe.micro_cursos.models.entities.Curso;
import com.espe.micro_cursos.models.entities.CursoUsuario;
import com.espe.micro_cursos.repositories.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CursoServiceImpl implements CursoService{

    @Autowired
    private CursoRepository repository;

    @Autowired
    private UsuarioClientRest clientRest;

    @Override
    public List<Curso> findAll() {
        return (List<Curso>) repository.findAll();
    }

    @Override
    public Optional<Curso> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Curso save(Curso curso) {
        return repository.save(curso);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<Usuario> addUser(Usuario usuario, Long id) {
        Optional<Curso> optionalCurso = repository.findById(id);
        if (optionalCurso.isPresent()) {
            Curso curso = optionalCurso.get();

            // Verificar si el usuario ya está matriculado en el curso
            for (CursoUsuario cursoUsuario : curso.getCursoUsuarios()) {
                if (cursoUsuario.getUsuarioId().equals(usuario.getId())) {
                    return Optional.empty(); // El usuario ya está matriculado
                }
            }

            // Si el usuario no está matriculado, añadirlo
            Usuario usuarioTemp = clientRest.findById(usuario.getId());
            CursoUsuario cursoUsuario = new CursoUsuario();
            cursoUsuario.setUsuarioId(usuarioTemp.getId());
            curso.addCursoUsuario(cursoUsuario); // Añadir la relación
            repository.save(curso);

            return Optional.of(usuarioTemp);
        }
        return Optional.empty();
    }


    @Override
    public Optional<Usuario> removeUser(Long usuarioId, Long cursoId) {
        Optional<Curso> optionalCurso = repository.findById(cursoId);
        if (optionalCurso.isPresent()) {
            Curso curso = optionalCurso.get();

            // Buscar y eliminar la relación de usuario con el curso
            for (CursoUsuario cursoUsuario : curso.getCursoUsuarios()) {
                if (cursoUsuario.getUsuarioId().equals(usuarioId)) {
                    curso.removeCursoUsuario(cursoUsuario); // Eliminar la relación
                    repository.save(curso);
                    // Devolver el usuario desmatriculado
                    return Optional.of(new Usuario(usuarioId));
                }
            }
        }
        return Optional.empty(); // El usuario no estaba matriculado
    }


}
