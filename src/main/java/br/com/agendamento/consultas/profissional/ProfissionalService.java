package br.com.agendamento.consultas.profissional;

import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfissionalService {

    private final ProfissionalRepository repository;

    public ProfissionalService(ProfissionalRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Profissional cadastrar(String nome, String especialidade, int duracaoConsultaMin,
                                  List<Expediente> expediente) {
        boolean faixaInvalida = expediente.stream().anyMatch(e -> !e.horaFim().isAfter(e.horaInicio()));
        if (faixaInvalida) {
            throw new RegraDeNegocioException(HttpStatus.BAD_REQUEST,
                    "no expediente, a hora final deve ser depois da hora inicial");
        }
        return repository.saveAndFlush(
                new Profissional(nome.trim(), especialidade.trim(), duracaoConsultaMin, expediente));
    }

    @Transactional(readOnly = true)
    public Profissional buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException(HttpStatus.NOT_FOUND, "profissional não encontrado"));
    }
}
