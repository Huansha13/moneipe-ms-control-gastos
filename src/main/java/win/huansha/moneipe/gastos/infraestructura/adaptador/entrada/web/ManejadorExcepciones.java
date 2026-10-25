package win.huansha.moneipe.gastos.infraestructura.adaptador.entrada.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import reactor.core.publisher.Mono;

import win.huansha.moneipe.gastos.dominio.excepcion.RecursoNoEncontradoExcepcion;

@RestControllerAdvice
public class ManejadorExcepciones {

    @ExceptionHandler(RecursoNoEncontradoExcepcion.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<Map<String, String>> noEncontrado(RecursoNoEncontradoExcepcion ex) {
        return Mono.just(Map.of("error", ex.getMessage()));
    }
}
