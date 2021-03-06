package controller;

import DAO.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Scanner;
import model.*;

public class GerenciaPassagem {

	private ArrayList<Helicoptero> helicopteros;
	private ArrayList<Aeroporto> aeroportos;
	private ArrayList<Aeronave> aeronaves;
	private ArrayList<Cliente> clientes;
	private ArrayList<Aviao> avioes;
	private ArrayList<Carro> carros;
	private ArrayList<Voo> voos;

	private Helicoptero helicoptero = new Helicoptero();
	private Aeroporto aeroporto = new Aeroporto();
	private Scanner ent = new Scanner(System.in);
	private Passagem passagem = new Passagem();
	private Cliente cliente = new Cliente();
	private Aviao aviao = new Aviao();
	private Carro carro = new Carro();
	private Voo voo = new Voo();

	private final HelicopteroDAO HelBd = new HelicopteroDAO();
	private final AeroportoDAO AerBd = new AeroportoDAO();
	private final Conexao con = Conexao.getInstance();
	private final ClienteDAO CliDB = new ClienteDAO();
	private final AviaoDAO AviBd = new AviaoDAO();
	private final CarroDAO CarBd = new CarroDAO();
	private final VooDAO VooBd = new VooDAO();
	private final PassagemDAO PassagemBd = new PassagemDAO();

	private int tipo, codCliente, codVoo, resp, lotacao;
	private String ident;
	private long identL;
	private String identS;
	private boolean verifAviao, vefCliente, vefVoo;
	private double carga, precoFinal, valorViagem, valorTotal;
	private String data, dataDiv[], hora, horaDiv[];

	public GerenciaPassagem() {
	}

	public void vendaPassagem() {

		voos = VooBd.relatorio();
		clientes = CliDB.relatorio();

		if (voos != null) {
			if (clientes != null) {
				System.out.println("\n----------- Inserir Passagem -----------");
				System.out.println("Inserir Voo: ");

				do {

					boolean lotado = false;

					System.out.println("Informe o codigo do voo: ");
					codVoo = ent.nextInt();
					ent.nextLine();

					voo = VooBd.consultar(codVoo);

					if (voo == null) {
						System.out.println("\n\t--- [Identificação Escolhida Inexistente!!!] ---\n");
						System.out.print("Deseja Visualizar Lista De Voos? \n\t1 - Sim\n\t2 - Não\nOpção: ");
						resp = ent.nextInt();
						ent.nextLine();
						if (resp == 1) {
							System.out.println("--------- Lista de Clientes disponíveis -------");
							System.out.println("Id             Nome:           ");
							for (Voo v : voos) {
								System.out.printf("%-15d%-50d\n", v.getId_voo(), v.getTipoAeronave());
							}
						}

						vefVoo = false;

					} else {

						lotado = passagem.lotacao(voo); // TODO realizar a verificação da lotação Código?

						if (!lotado) {

							vefVoo = true;

							System.out.println("\n\t Voo a ser Adicionado:");
							System.out.println("------------------------------");
							voo.consultar();
							System.out.println("------------------------------\n");
							System.out.println("Realmente deseja Adicionar (" + voo.getId_voo() + ")");
							System.out.println("\t1 = sim\n\t2 = não ");
							System.out.print("Opção:");
							resp = ent.nextInt();
							ent.nextLine();

							if (resp == 1) {
								passagem.setVoo(voo);
								vefVoo = true;
							} else {
								System.out.println("\n\t--- [Cadastro de Voo obrigatório!!!] ---");
							}
						} else {
							System.out.println("Voo lotado! Escolha outro voo! ");
							vefVoo = false;
						}
					}
				} while (!vefVoo);

				// CADASTRAR CLIENTE

				System.out.println("Inserir cliente: ");

				do {
					vefCliente = false;

					System.out.println("Informe o codigo do cliente: ");
					codCliente = ent.nextInt();
					ent.nextLine();

					// TODO Verificar se o cliente já está cadastrado no voo.

					cliente = CliDB.consultar(codCliente);

					if (cliente == null) {
						System.out.println("\n\t--- [Identificação Escolhida Inexistente!!!] ---\n");
						System.out.print("Deseja Visualizar Lista De Cliente? \n\t1 - Sim\n\t2 - Não\nOpção: ");
						resp = ent.nextInt();
						ent.nextLine();
						if (resp == 1) {
							System.out.println("--------- Lista de Clientes disponíveis -------");
							System.out.println("Id             Nome:           ");
							for (Cliente c : clientes) {
								System.out.printf("%-15d%-50s\n", c.getIdentificacao(), c.getNome());
							}
						}
					} else {
						System.out.println("\n\t Cliente a ser Adicionado:");
						System.out.println("------------------------------");
						cliente.consultar();
						System.out.println("------------------------------\n");
						System.out.println("Realmente deseja Adicionar (" + cliente.getNome() + ")");
						System.out.println("\t1 = sim\n\t2 = não ");
						System.out.print("Opção:");
						resp = ent.nextInt();
						ent.nextLine();

						if (resp == 1) {
							passagem.setCliente(cliente);
							vefCliente = true;
						} else {
							System.out.println("\n\t--- [Cadastro de Cliente obrigatório!!!] ---");
						}
					}
				} while (!vefCliente);

				LocalDate dia = LocalDate.now();
				LocalTime hora = LocalTime.now();
				passagem.setDataVenda(dia);
				System.out.println("Data de Venda: " + dia);
				passagem.setHoraVenda(LocalTime.now());
				System.out.print("Horário de Venda: " + hora);


				System.out.println("--------------------------------");

				boolean permitir = false;

				do {// TODO Comparar se a carga do cliente ultrapassa o limite
					// da aeronave.
					// Se ultrapassar, não permitir a carga.
					// Se não ultrapassar, permitir a carga e somar ao valor da
					// carga embarcada

					System.out.println("Qual o peso da carga levada pelo cliente?");
					carga = ent.nextDouble();
					permitir = passagem.verificaCarga(voo, carga);

				} while (!permitir);

				passagem.setCargaCliente(carga);

				valorViagem = voo.getPrecoViagem();
				valorTotal = passagem.calculaPrecoFinal(dia, valorViagem);

				passagem.setPrecoFinalViagem(valorTotal);

				System.out.println("Preço Final da viagem: " + valorTotal);

				PassagemBd.vendaPassagem(passagem);

				System.out.println("\n\t--- [Voo Adicionado com sucesso!!!] ---\n");

			} else {
				System.out.println("Não existem clientes cadastrados!");
			}
		} else {
			System.out.println("\n\t--- [Não Há Voo Cadastrado!!!] ----");
		}
	}

	public void cancelaPassagem() {

		int cod,res;

		System.out.println("==== Cancelamento de Passagens ====");

		System.out.println("Qual o codigo da passagem que você deseja cancelar? ");
		cod = ent.nextInt();
		ent.nextLine();

		Passagem passagem = PassagemBd.consultar(cod);

		if (passagem != null) {
			System.out.println("===== Dados da Passagem =====");
			passagem.consultar();
			
			System.out.println("Tem certeza que deseja cancelar essa passagem? 1-sim/2-nao");
			res = ent.nextInt();
			ent.hasNextLine();
			
			if(res == 1){
				PassagemBd.cancelaPassagem(cod);
				System.out.println("Passagem cancelada com sucesso!");
			}else{
				System.out.println("Passagem não cancelada!");
			}
			
		} else {
			System.out.println("Não existe passagens com este código.");
		}

	}

	public void relatorioPassageirosPorVoo() {
		
		int cod;

		ArrayList<Passagem> passagens = new ArrayList<>();

		System.out.println("==== Relatório de Passageiros em um Voo ====");
		
		System.out.println("Informe o código do voo que deseja consultar as passagens");
		cod = ent.nextInt();

		try {
			passagens = PassagemBd.relatorioPassageiroPorVoo(cod);

			if (passagens != null) {
				System.out.println("===== Lista de passageiros do Voo " + cod +" =====");
				
					for (Passagem p : passagens) {
						p.consultarPorVoo();
				}
			} else {
				System.out.println("\nNão existem passagens cadastradas para esse voo.");
			}
		} catch (Exception ex) {
			System.out.println("Erro: " + ex.getMessage());
		}
	}
	
	public void relatorioPassageirosPorVooPago() {
		
		int cod;

		ArrayList<Passagem> passagens = new ArrayList<>();

		System.out.println("==== Relatório de Passageiros e valores pagos em um Voo ====");
		
		System.out.println("Informe o código do voo que deseja consultar as passagens e o valor pago?");
		cod = ent.nextInt();

		try {
			passagens = PassagemBd.relatorioPassageiroPorVoo(cod);

			if (passagens != null) {
				System.out.println("===== Lista de passageiros do Voo " + cod +" =====");
				
					for (Passagem p : passagens) {
						p.consultarPorVooPago();
				}
			} else {
				System.out.println("\nNão existem passagens cadastradas para esse voo.");
			}
		} catch (Exception ex) {
			System.out.println("Erro: " + ex.getMessage());
		}
	}
	
	public void relatorioCargaPorVoo() {// TODO Cálculo para a carga incorreto. Corrigir!
		
		int cod;
		double totalCargaVoo = 0;
		double cargaMaximaVoo= 0;

		System.out.println("==== Relatório de Carga Disponível em um Voo ====");
		
		System.out.println("Informe o código do voo que deseja consultar as passagens e o valor pago?");
		cod = ent.nextInt();

		try {
			Voo voo = VooBd.consultar(cod);
			totalCargaVoo = voo.getPesoCargaEmbarcada();
			cargaMaximaVoo = voo.getAeronave().getCapacCarga();

			if (voo != null) {
				System.out.println("===== Carga disponível no Voo " + cod +" =====");
						voo.consultarCarga(totalCargaVoo, cargaMaximaVoo);
			} else {
				System.out.println("\nNão existem passagens cadastradas para esse voo.");
			}
		} catch (Exception ex) {
			System.out.println("Erro: " + ex.getMessage());
		}
	}
		
	public void relatorioLotacao() {

			int cod;
			
			System.out.println("==== Relatório de Carga Disponível em um Voo ====");
			
			System.out.println("Informe o código do voo que deseja consultar as passagens e o valor pago?");
			cod = ent.nextInt();

			try {
				Voo voo = VooBd.consultar(cod);

				if (voo != null) {
					System.out.println("===== Vagas disponíveis no Voo " + cod +" =====");
							voo.consultarLotacao();
				} else {
					System.out.println("\nNão existem passagens cadastradas para esse voo.");
				}
			} catch (Exception ex) {
				System.out.println("Erro: " + ex.getMessage());
			}
	}

}
