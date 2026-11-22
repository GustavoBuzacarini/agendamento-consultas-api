package br.com.agendamento.consultas.conta;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Só a recepção chega aqui (regra na SegurancaConfig). A resposta com senha nunca vai para cache. */
@RestController
@RequestMapping("/pacientes/{pacienteId}/conta")
public class ContaPacienteController {

    private final ContaPacienteService service;

    public ContaPacienteController(ContaPacienteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ContaPacienteService.ContaCriada> criar(@PathVariable Long pacienteId) {
        return ResponseEntity.status(HttpStatus.CREATED).cacheControl(CacheControl.noStore())
                .body(service.criar(pacienteId));
    }

    @PostMapping("/senha-provisoria")
    public ResponseEntity<ContaPacienteService.ContaCriada> redefinirSenha(@PathVariable Long pacienteId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.redefinirSenha(pacienteId));
    }
}
