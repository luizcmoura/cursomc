package com.moura.cursomc.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moura.cursomc.domain.Cidade;
import com.moura.cursomc.domain.Cliente;
import com.moura.cursomc.domain.Endereco;
import com.moura.cursomc.domain.enums.TipoCliente;
import com.moura.cursomc.dto.ClienteDTO;
import com.moura.cursomc.dto.ClienteNewDTO;
import com.moura.cursomc.repositories.ClienteRepository;
import com.moura.cursomc.repositories.EnderecoRepository;
import com.moura.cursomc.services.exceptions.DataIntegrityException;
import com.moura.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private EnderecoRepository enderecoRepository;

	public Cliente find(Integer id) {

		Optional<Cliente> cli = clienteRepository.findById(id);
		return cli.orElseThrow(() -> new ObjectNotFoundException("Objeto não encontrado! Id: " + id + ", Tipo: " + Cliente.class.getName()));
	}

	@Transactional
	public Cliente insert(Cliente cliente) {
		cliente.setId(null);
		cliente = clienteRepository.save(cliente);
		enderecoRepository.saveAll(cliente.getEnderecos());
		return cliente;
	}

	public Cliente update(Cliente clienteOld) {
		Cliente clienteNew = find(clienteOld.getId());
		updateData(clienteNew, clienteOld);
		return clienteRepository.save(clienteNew);
	}

	public void delete(Integer id) {
		find(id);
		try {
			clienteRepository.deleteById(id);
		} catch (DataIntegrityException e) {
			throw new DataIntegrityException("Não é possível excluir um Cliente porque há pedidos relacionados");
		}
	}

	public List<Cliente> findAll() {
		return clienteRepository.findAll();
	}

	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);

		return clienteRepository.findAll(pageRequest);

	}

	private void updateData(Cliente clienteNew, Cliente clienteOld) {
		clienteNew.setNome(clienteOld.getNome());
		clienteNew.setEmail(clienteOld.getEmail());
	}

	public Cliente fromDTO(ClienteDTO clienteDTO) {
		return new Cliente(clienteDTO.getId(), clienteDTO.getNome(), clienteDTO.getEmail(), null, null, null);
	}

	public Cliente fromDTO(ClienteNewDTO clienteNewDTO) {
		Cliente cliente = new Cliente(null, clienteNewDTO.getNome(), clienteNewDTO.getEmail(), clienteNewDTO.getCpfOuCnpj(), TipoCliente.toEnum(clienteNewDTO.getTipo()), bCryptPasswordEncoder.encode(clienteNewDTO.getSenha()));

		Cidade cidade = new Cidade(clienteNewDTO.getCidadeId(), null, null);

		Endereco endereco = new Endereco(null, clienteNewDTO.getLogradouro(), clienteNewDTO.getNumero(), clienteNewDTO.getComplemento(), clienteNewDTO.getBairro(), clienteNewDTO.getCep(), cliente, cidade);

		cliente.getEnderecos().add(endereco);

		cliente.getTelefones().add(clienteNewDTO.getTelefone1());

		if (clienteNewDTO.getTelefone2() != null) {
			cliente.getTelefones().add(clienteNewDTO.getTelefone2());
		}

		if (clienteNewDTO.getTelefone3() != null) {
			cliente.getTelefones().add(clienteNewDTO.getTelefone3());
		}

		return cliente;
	}
}
