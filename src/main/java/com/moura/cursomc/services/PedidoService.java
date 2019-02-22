package com.moura.cursomc.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moura.cursomc.domain.Cliente;
import com.moura.cursomc.domain.ItemPedido;
import com.moura.cursomc.domain.PagamentoComBoleto;
import com.moura.cursomc.domain.Pedido;
import com.moura.cursomc.domain.enums.EstadoPagamento;
import com.moura.cursomc.repositories.ItemPedidoRepository;
import com.moura.cursomc.repositories.PagamentoRepository;
import com.moura.cursomc.repositories.PedidoRepository;
import com.moura.cursomc.security.UserSS;
import com.moura.cursomc.services.exceptions.AuthorizationException;
import com.moura.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {
	
	@Autowired	
	private PedidoRepository pedidoRepository;
	
	@Autowired
	private BoletoService boletoService;

	@Autowired
	private PagamentoRepository pagamentoRepository;
	
	@Autowired
	private ProdutoService produtoService;
	
	@Autowired
	private ItemPedidoRepository itemPedidoRepository;
	
	@Autowired
	private ClienteService clienteService;
	
	public Pedido find(Integer id) {
		Optional<Pedido> pedido = pedidoRepository.findById(id);
		return pedido.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Pedido.class.getName()));
	}
	
	@Transactional
	public Pedido insert(Pedido pedido) {
		pedido.setId(null);
		pedido.setInstante(new Date());
		pedido.setCliente(clienteService.find(pedido.getCliente().getId()));
		pedido.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		pedido.getPagamento().setPedido(pedido);
		
		if(pedido.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pgtoComBoleto = (PagamentoComBoleto)pedido.getPagamento();
			boletoService.preencherPagamentoComBoleto(pgtoComBoleto, pedido.getInstante());
		}
		
		pedido = pedidoRepository.save(pedido);
		pagamentoRepository.save(pedido.getPagamento());
		
		for(ItemPedido ip : pedido.getItens()) {
			ip.setDesconto(0.0);
			ip.setProduto(produtoService.find(ip.getProduto().getId()));
			ip.setPreco(ip.getProduto().getPreco());
			ip.setPedido(pedido);
		}
		
		itemPedidoRepository.saveAll(pedido.getItens());
		
		System.out.println(pedido);
		
		return pedido;
	}
	
	public Page<Pedido> findPage(Integer page, Integer linesPerPage, String orderBy, String direction){
		
		UserSS userSS = UserService.authenticated();
		
		if(userSS == null) {
			throw new AuthorizationException("Acesso Negado");		
		}
		
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		
		Cliente cliente = clienteService.find(userSS.getId());
		
		return pedidoRepository.findByCliente(cliente, pageRequest);
	}
}
