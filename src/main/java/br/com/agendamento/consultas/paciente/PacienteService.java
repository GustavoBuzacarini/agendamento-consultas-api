package br.com.agendamento.consultas.paciente;

import br.com.agendamento.consultas.cep.CepFacade;
import br.com.agendamento.consultas.cep.Endereco;
import br.com.agendamento.consultas.erro.RegraDeNegocioException;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacienteService {

    private final PacienteRepository repository;
    private final CepFacade cep;

    public PacienteService(PacienteRepository repository, CepFacade cep) {
        this.repository = repository;
        this.cep = cep;
    }

    /** @param cepInformado opcional; se vier, o endereço é buscado no serviço de CEP (US-006) */
    @Transactional
    public Paciente cadastrar(String nome, String cpf, LocalDate dataNascimento, boolean gestante,
                              boolean pessoaComDeficiencia, String cepInformado) {
        String cpfSomenteDigitos = cpf.replaceAll("\\D", "");
        if (repository.existsByCpf(cpfSomenteDigitos)) {
            throw new RegraDeNegocioException(HttpStatus.CONFLICT, "CPF já cadastrado");
        }
        // a chamada externa só acontece depois das validações baratas; se falhar, nada é gravado
        Endereco endereco = (cepInformado == null || cepInformado.isBlank()) ? null : cep.buscar(cepInformado);
        return repository.saveAndFlush(new Paciente(nome.trim(), cpfSomenteDigitos, dataNascimento, gestante,
                pessoaComDeficiencia, endereco));
    }

    @Transactional(readOnly = true)
    public Paciente buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RegraDeNegocioException(HttpStatus.NOT_FOUND, "paciente não encontrado"));
    }
}
