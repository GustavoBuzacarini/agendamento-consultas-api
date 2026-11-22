package br.com.agendamento.consultas.agenda;

import br.com.agendamento.consultas.consulta.StatusConsulta;
import br.com.agendamento.consultas.profissional.ProfissionalService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AgendaService {

    @PersistenceContext
    private EntityManager em;

    private final ProfissionalService profissionais;

    public AgendaService(ProfissionalService profissionais) {
        this.profissionais = profissionais;
    }

    /** Consultas ativas do profissional no dia, da mais prioritária para a menos; empates por horário. */
    @Transactional(readOnly = true)
    public List<ItemDaAgenda> doDia(Long profissionalId, LocalDate dia) {
        profissionais.buscar(profissionalId); // 404 se o profissional não existir

        List<StatusConsulta> ativas =
                Arrays.stream(StatusConsulta.values()).filter(StatusConsulta::ativa).toList();

        List<ItemDaAgenda> itens = em.createQuery("""
                        select new br.com.agendamento.consultas.agenda.ItemDaAgenda(
                            c.id, c.inicio, c.fim, p.nome, m.descricao, m.gravidade, c.prioridade, c.status)
                        from Consulta c
                        join Paciente p on p.id = c.pacienteId
                        join MotivoAtendimento m on m.id = c.motivoId
                        where c.profissionalId = :profissionalId
                          and c.status in :ativas
                          and c.inicio >= :de and c.inicio < :ate""", ItemDaAgenda.class)
                .setParameter("profissionalId", profissionalId)
                .setParameter("ativas", ativas)
                .setParameter("de", dia.atStartOfDay())
                .setParameter("ate", dia.plusDays(1).atStartOfDay())
                .getResultList();

        return itens.stream()
                .sorted(Comparator.comparing(ItemDaAgenda::prioridade, Comparator.reverseOrder())
                        .thenComparing(ItemDaAgenda::inicio))
                .toList();
    }
}
