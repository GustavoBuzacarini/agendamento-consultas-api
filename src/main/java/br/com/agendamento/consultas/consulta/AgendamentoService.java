package br.com.agendamento.consultas.consulta;

import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Persistência e mudanças de status das consultas (cada operação é uma transação). */
@Service
public class AgendamentoService {

    private final ConsultaRepository repository;

    public AgendamentoService(ConsultaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Consulta criar(Consulta consulta) {
        try {
            return repository.saveAndFlush(consulta);
        } catch (DataIntegrityViolationException e) {
            // última barreira: a restrição do banco barrou um horário sobreposto (pedidos simultâneos)
            throw new RegraDeNegocioException(HttpStatus.CONFLICT, "horário indisponível");
        }
    }

    @Transactional(readOnly = true)
    public Consulta buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException(HttpStatus.NOT_FOUND, "consulta não encontrada"));
    }

    @Transactional
    public Consulta confirmar(Long id) {
        Consulta consulta = buscar(id);
        consulta.confirmar();
        return consulta;
    }

    @Transactional
    public Consulta cancelar(Long id) {
        Consulta consulta = buscar(id);
        consulta.cancelar();
        return consulta;
    }

    @Transactional
    public Consulta realizar(Long id) {
        Consulta consulta = buscar(id);
        consulta.realizar();
        return consulta;
    }

    @Transactional
    public Consulta marcarFalta(Long id) {
        Consulta consulta = buscar(id);
        consulta.marcarFalta();
        return consulta;
    }
}
