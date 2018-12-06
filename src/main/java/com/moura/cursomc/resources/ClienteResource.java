package com.moura.cursomc.resources;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.moura.cursomc.domain.Cliente;
import com.moura.cursomc.dto.ClienteDTO;
import com.moura.cursomc.dto.ClienteNewDTO;
import com.moura.cursomc.services.ClienteService;

@RestController
@RequestMapping(value = "/clientes")
public class ClienteResource {

	@Autowired
	private ClienteService clienteService;

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public ResponseEntity<Cliente> find(@PathVariable Integer id) {

		Cliente cliente = clienteService.find(id);
		return ResponseEntity.ok().body(cliente);
	}

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Void> insert(@Valid @RequestBody ClienteNewDTO clienteNewDTO) {
		Cliente cliente = clienteService.fromDTO(clienteNewDTO);
		cliente = clienteService.insert(cliente);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(cliente.getId()).toUri();
		return ResponseEntity.created(uri).build();
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public ResponseEntity<Void> update(@Valid @RequestBody ClienteDTO clienteDTO, @PathVariable Integer id) {
		Cliente cliente = clienteService.fromDTO(clienteDTO);
		cliente.setId(id);
		cliente = clienteService.update(cliente);
		return ResponseEntity.noContent().build();
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		clienteService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@RequestMapping(method = RequestMethod.GET)
	public ResponseEntity<List<ClienteDTO>> findAll() {
		List<Cliente> listCliente = clienteService.findAll();
		List<ClienteDTO> listClienteDTO = listCliente.stream().map(obj -> new ClienteDTO(obj)).collect(Collectors.toList());
		return ResponseEntity.ok().body(listClienteDTO);
	}

	@RequestMapping(value = "/page", method = RequestMethod.GET)
	public ResponseEntity<Page<ClienteDTO>> findPage(@RequestParam(value = "page", defaultValue = "0") Integer page, @RequestParam(value = "linesPerPage", defaultValue = "24") Integer linesPerPage,
			@RequestParam(value = "orderBy", defaultValue = "nome") String orderBy, @RequestParam(value = "direction", defaultValue = "ASC") String direction) {

		Page<Cliente> pageCliente = clienteService.findPage(page, linesPerPage, orderBy, direction);
		Page<ClienteDTO> listClienteDTO = pageCliente.map(obj -> new ClienteDTO(obj));
		return ResponseEntity.ok().body(listClienteDTO);
	}

	/*
	 * 
	 * public Cliente fromDTO(ClienteDTO clienteDTO) { return new
	 * Cliente(clienteDTO.getId(), clienteDTO.getNome(), clienteDTO.getEmail(),
	 * null, null, null);
	 * 
	 * }
	 * 
	 * public Cliente fromDTO(ClienteNewDTO clienteNewDTO) { Cliente cliente = new
	 * Cliente(null, clienteNewDTO.getNome(), clienteNewDTO.getEmail(),
	 * clienteNewDTO.getCpfOuCnpj(), TipoCliente.toEnum(clienteNewDTO.getTipo()));
	 * Cidade cidade = new Cidade(clienteNewDTO.getCidadeId(), null, null); Endereco
	 * endereco = new Endereco(null, clienteNewDTO.getLogradouro(),
	 * clienteNewDTO.getNumero(), clienteNewDTO.getComplemento(),
	 * clienteNewDTO.getBairro(), clienteNewDTO.getCep(), cliente, cidade);
	 * cliente.getEnderecos().add(endereco);
	 * cliente.getTelefones().add(clienteNewDTO.getTelefone1());
	 * if(clienteNewDTO.getTelefone2() != null){
	 * cliente.getTelefones().add(clienteNewDTO.getTelefone2()); }
	 * if(clienteNewDTO.getTelefone3() != null){
	 * cliente.getTelefones().add(clienteNewDTO.getTelefone3()); }
	 * 
	 * return cliente; }
	 */
}
