import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner ENTRADA = new Scanner(System.in);

    public static void main(String[] args) {
        Oficina oficina = criarDadosIniciais();
        boolean continuar = true;
        while (continuar) {
            exibirMenu();
            int opcao = lerInteiro("Escolha: ");
            try {
                switch (opcao) {
                    case 1 -> cadastrarOrdem(oficina);
                    case 2 -> associarMecanico(oficina);
                    case 3 -> atribuirOrdem(oficina);
                    case 4 -> exibirOrdensDoBox(oficina);
                    case 5 -> contarFinalizadasPorBox(oficina);
                    case 6 -> buscarPorStatus(oficina);
                    case 7 -> exibirDetalhesOrdem(oficina);
                    case 8 -> finalizarOrdem(oficina);
                    case 0 -> continuar = false;
                    default -> System.out.println("Opção inválida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Não foi possível concluir: " + e.getMessage());
            }
            System.out.println();
        }
        System.out.println("Sistema encerrado.");
    }

    private static Oficina criarDadosIniciais() {
        Oficina oficina = new Oficina();
        oficina.adicionarMecanico(new Mecanico("Ana Souza", "111.111.111-11", "Mecânica geral", "(31) 99999-1111"));
        oficina.adicionarMecanico(new Mecanico("Bruno Lima", "222.222.222-22", "Elétrica", "(31) 99999-2222"));
        oficina.adicionarMecanico(new Mecanico("Carla Alves", "333.333.333-33", "Freios", "(31) 99999-3333"));
        oficina.adicionarBox(new Box(1, "Mecânica", 3, "Galpão A"));
        oficina.adicionarBox(new Box(2, "Elétrica", 2, "Galpão B"));
        oficina.adicionarBox(new Box(3, "Freios", 2, "Galpão C"));
        return oficina;
    }

    private static void exibirMenu() {
        System.out.println("=== OFICINA MECÂNICA ===");
        System.out.println("1. Cadastrar ordem de serviço");
        System.out.println("2. Associar mecânico a um box");
        System.out.println("3. Atribuir ordem de serviço a um box");
        System.out.println("4. Exibir ordens atribuídas a um box");
        System.out.println("5. Quantidade de ordens finalizadas por box");
        System.out.println("6. Buscar ordens por status");
        System.out.println("7. Exibir detalhes de uma ordem");
        System.out.println("8. Finalizar ordem em execução");
        System.out.println("0. Sair");
    }

    private static void cadastrarOrdem(Oficina oficina) {
        System.out.println("=== Cadastro de ordem ===");
        String cliente = lerTexto("Nome do cliente: ");
        String modelo = lerTexto("Modelo do veículo: ");
        String placa = lerTexto("Placa: ");
        LocalDate data;
        try {
            data = LocalDate.parse(lerTexto("Data (AAAA-MM-DD): "));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data inválida. Use o formato AAAA-MM-DD.");
        }
        String nomeServico = lerTexto("Nome do serviço: ");
        String categoria = lerTexto("Categoria/tipo do serviço (Mecânica, Elétrica ou Freios): ");
        int tempo = lerInteiro("Tempo estimado em minutos: ");
        double valorServico = lerDecimal("Valor do serviço: R$ ");
        double valorEstimado = lerDecimal("Valor estimado da ordem: R$ ");
        Servico servico = new Servico(nomeServico, tempo, valorServico, categoria);
        int codigo = oficina.proximoCodigo();
        oficina.cadastrarOrdem(new OrdemServico(codigo, cliente, modelo, placa, data, servico, valorEstimado));
        System.out.println("Ordem cadastrada com código " + codigo + " e status aberta.");
    }

    private static void associarMecanico(Oficina oficina) {
        System.out.println("=== Mecânicos ===");
        List<Mecanico> mecanicos = oficina.getMecanicos();
        for (int i = 0; i < mecanicos.size(); i++) System.out.println((i + 1) + ". " + mecanicos.get(i));
        int indice = lerInteiro("Número do mecânico: ") - 1;
        listarBoxes(oficina);
        int numeroBox = lerInteiro("Número do box: ");
        oficina.associarMecanico(indice, numeroBox);
        System.out.println("Mecânico associado ao box " + numeroBox + ".");
    }

    private static void atribuirOrdem(Oficina oficina) {
        listarBoxes(oficina);
        int numeroBox = lerInteiro("Número do box: ");
        int codigo = lerInteiro("Código da ordem aberta: ");
        oficina.atribuirOrdem(codigo, numeroBox);
        System.out.println("Ordem atribuída. O status passou para em execução.");
    }

    private static void exibirOrdensDoBox(Oficina oficina) {
        int numero = lerInteiro("Número do box: ");
        Box box = oficina.buscarBox(numero);
        if (box == null) throw new IllegalArgumentException("Box não encontrado.");
        System.out.println(box);
        if (box.getOrdensEmExecucao().isEmpty()) System.out.println("Nenhuma ordem em execução neste box.");
        else box.getOrdensEmExecucao().forEach(ordem -> System.out.println(ordem.detalhes()));
        System.out.println("Total de ordens no box: " + box.getOrdensEmExecucao().size());
    }

    private static void contarFinalizadasPorBox(Oficina oficina) {
        System.out.println("=== Ordens finalizadas por box ===");
        for (Box box : oficina.getBoxes()) {
            long quantidade = oficina.getOrdens().stream()
                    .filter(ordem -> ordem.status == StatusOrdem.FINALIZADA && ordem.box == box).count();
            System.out.println("Box " + box.numero + ": " + quantidade);
        }
    }

    private static void buscarPorStatus(Oficina oficina) {
        System.out.println("Status: 1. aberta | 2. em execução | 3. finalizada");
        StatusOrdem status = switch (lerInteiro("Escolha o status: ")) {
            case 1 -> StatusOrdem.ABERTA;
            case 2 -> StatusOrdem.EM_EXECUCAO;
            case 3 -> StatusOrdem.FINALIZADA;
            default -> throw new IllegalArgumentException("Status inválido.");
        };
        List<OrdemServico> ordens = oficina.buscarPorStatus(status);
        if (ordens.isEmpty()) System.out.println("Nenhuma ordem com status " + status.descricao + ".");
        else ordens.forEach(ordem -> System.out.println(ordem.detalhes()));
    }

    private static void exibirDetalhesOrdem(Oficina oficina) {
        OrdemServico ordem = oficina.buscarOrdem(lerInteiro("Código da ordem: "));
        if (ordem == null) throw new IllegalArgumentException("Ordem não encontrada.");
        System.out.println(ordem.detalhes());
    }

    private static void finalizarOrdem(Oficina oficina) {
        oficina.finalizarOrdem(lerInteiro("Código da ordem em execução: "));
        System.out.println("Ordem finalizada. O box foi liberado e permanece registrado na ordem.");
    }

    private static void listarBoxes(Oficina oficina) {
        System.out.println("=== Boxes ===");
        oficina.getBoxes().forEach(System.out::println);
    }

    private static String lerTexto(String mensagem) {
        System.out.print(mensagem);
        return ENTRADA.nextLine().trim();
    }

    private static int lerInteiro(String mensagem) {
        try { return Integer.parseInt(lerTexto(mensagem)); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Digite um número inteiro válido."); }
    }

    private static double lerDecimal(String mensagem) {
        try { return Double.parseDouble(lerTexto(mensagem).replace(',', '.')); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Digite um valor numérico válido."); }
    }

    private enum StatusOrdem {
        ABERTA("aberta"), EM_EXECUCAO("em execução"), FINALIZADA("finalizada");
        private final String descricao;
        StatusOrdem(String descricao) { this.descricao = descricao; }
    }

    private static final class Mecanico {
        private final String nome, cpf, especialidade, telefone;
        private Box boxResponsavel;
        Mecanico(String nome, String cpf, String especialidade, String telefone) {
            this.nome = textoObrigatorio(nome, "nome");
            this.cpf = textoObrigatorio(cpf, "CPF");
            this.especialidade = textoObrigatorio(especialidade, "especialidade");
            this.telefone = textoObrigatorio(telefone, "telefone");
        }
        private static String textoObrigatorio(String valor, String campo) {
            if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Informe " + campo + ".");
            return valor.trim();
        }
        @Override public String toString() {
            return nome + " (CPF: " + cpf + ", especialidade: " + especialidade + ", telefone: " + telefone + ")";
        }
    }

    private static final class Servico {
        private final String nome, categoria;
        private final int tempoEstimadoMinutos;
        private final double valor;
        Servico(String nome, int tempoEstimadoMinutos, double valor, String categoria) {
            this.nome = Mecanico.textoObrigatorio(nome, "nome do serviço");
            this.categoria = Mecanico.textoObrigatorio(categoria, "categoria");
            if (tempoEstimadoMinutos <= 0) throw new IllegalArgumentException("O tempo deve ser maior que zero.");
            if (!Double.isFinite(valor) || valor < 0) throw new IllegalArgumentException("O valor deve ser positivo ou zero.");
            this.tempoEstimadoMinutos = tempoEstimadoMinutos;
            this.valor = valor;
        }
        @Override public String toString() {
            return nome + " | categoria: " + categoria + " | tempo: " + tempoEstimadoMinutos
                    + " min | valor: R$ " + String.format("%.2f", valor);
        }
    }

    private static final class Box {
        private final int numero, capacidadeMaximaVeiculos;
        private final String tipoServicoPermitido, localizacao;
        private Mecanico mecanicoResponsavel;
        private final List<OrdemServico> ordensEmExecucao = new ArrayList<>();
        Box(int numero, String tipoServicoPermitido, int capacidadeMaximaVeiculos, String localizacao) {
            if (numero <= 0 || capacidadeMaximaVeiculos <= 0) throw new IllegalArgumentException("Número e capacidade devem ser positivos.");
            this.numero = numero;
            this.tipoServicoPermitido = Mecanico.textoObrigatorio(tipoServicoPermitido, "tipo de serviço");
            this.localizacao = Mecanico.textoObrigatorio(localizacao, "localização");
            this.capacidadeMaximaVeiculos = capacidadeMaximaVeiculos;
        }
        List<OrdemServico> getOrdensEmExecucao() { return Collections.unmodifiableList(ordensEmExecucao); }
        void adicionarOrdem(OrdemServico ordem) { ordensEmExecucao.add(ordem); }
        void removerOrdem(OrdemServico ordem) { ordensEmExecucao.remove(ordem); }
        @Override public String toString() {
            return "Box " + numero + " | serviço: " + tipoServicoPermitido + " | capacidade: "
                    + capacidadeMaximaVeiculos + " | localização: " + localizacao + " | mecânico: "
                    + (mecanicoResponsavel == null ? "não associado" : mecanicoResponsavel.nome);
        }
    }

    private static final class OrdemServico {
        private final int codigo;
        private final String nomeCliente, modeloVeiculo, placaVeiculo;
        private final LocalDate data;
        private final Servico servico;
        private final double valorEstimado;
        private StatusOrdem status = StatusOrdem.ABERTA;
        private Box box;
        OrdemServico(int codigo, String nomeCliente, String modeloVeiculo, String placaVeiculo,
                     LocalDate data, Servico servico, double valorEstimado) {
            if (codigo <= 0) throw new IllegalArgumentException("O código deve ser positivo.");
            this.nomeCliente = Mecanico.textoObrigatorio(nomeCliente, "nome do cliente");
            this.modeloVeiculo = Mecanico.textoObrigatorio(modeloVeiculo, "modelo do veículo");
            this.placaVeiculo = Mecanico.textoObrigatorio(placaVeiculo, "placa").toUpperCase();
            if (data == null || servico == null) throw new IllegalArgumentException("Data e serviço são obrigatórios.");
            if (!Double.isFinite(valorEstimado) || valorEstimado < 0) throw new IllegalArgumentException("O valor estimado não pode ser negativo.");
            this.codigo = codigo;
            this.data = data;
            this.servico = servico;
            this.valorEstimado = valorEstimado;
        }
        String detalhes() {
            String responsavel = box == null ? "nenhum" : box.numero + " (mecânico: "
                    + (box.mecanicoResponsavel == null ? "não associado" : box.mecanicoResponsavel.nome) + ")";
            return "Ordem #" + codigo + " | cliente: " + nomeCliente + " | veículo: " + modeloVeiculo
                    + " | placa: " + placaVeiculo + " | data: " + data + " | status: " + status.descricao
                    + " | valor estimado: R$ " + String.format("%.2f", valorEstimado)
                    + "\n  Serviço: " + servico + "\n  Box: " + responsavel;
        }
    }

    private static final class Oficina {
        private final List<Mecanico> mecanicos = new ArrayList<>();
        private final List<Box> boxes = new ArrayList<>();
        private final List<OrdemServico> ordens = new ArrayList<>();
        void adicionarMecanico(Mecanico mecanico) { mecanicos.add(mecanico); }
        void adicionarBox(Box box) { boxes.add(box); }
        List<Mecanico> getMecanicos() { return Collections.unmodifiableList(mecanicos); }
        List<Box> getBoxes() { return Collections.unmodifiableList(boxes); }
        List<OrdemServico> getOrdens() { return Collections.unmodifiableList(ordens); }
        int proximoCodigo() { return ordens.stream().mapToInt(ordem -> ordem.codigo).max().orElse(0) + 1; }
        void cadastrarOrdem(OrdemServico ordem) {
            if (buscarOrdem(ordem.codigo) != null) throw new IllegalArgumentException("Já existe ordem com esse código.");
            ordens.add(ordem);
        }
        OrdemServico buscarOrdem(int codigo) {
            return ordens.stream().filter(ordem -> ordem.codigo == codigo).findFirst().orElse(null);
        }
        Box buscarBox(int numero) { return boxes.stream().filter(box -> box.numero == numero).findFirst().orElse(null); }
        void associarMecanico(int indice, int numeroBox) {
            if (indice < 0 || indice >= mecanicos.size()) throw new IllegalArgumentException("Mecânico não encontrado.");
            Mecanico mecanico = mecanicos.get(indice);
            Box box = buscarBox(numeroBox);
            if (box == null) throw new IllegalArgumentException("Box não encontrado.");
            if (mecanico.boxResponsavel != null && mecanico.boxResponsavel != box)
                throw new IllegalStateException("Esse mecânico já é responsável por outro box.");
            if (box.mecanicoResponsavel != null && box.mecanicoResponsavel != mecanico)
                throw new IllegalStateException("Esse box já possui um mecânico responsável.");
            mecanico.boxResponsavel = box;
            box.mecanicoResponsavel = mecanico;
        }
        void atribuirOrdem(int codigo, int numeroBox) {
            OrdemServico ordem = buscarOrdem(codigo);
            if (ordem == null) throw new IllegalArgumentException("Ordem não encontrada.");
            Box box = buscarBox(numeroBox);
            if (box == null) throw new IllegalArgumentException("Box não encontrado.");
            if (ordem.status != StatusOrdem.ABERTA) throw new IllegalStateException("Somente ordens abertas podem ser atribuídas.");
            if (box.mecanicoResponsavel == null) throw new IllegalStateException("Associe um mecânico ao box antes de atribuir ordens.");
            if (!box.tipoServicoPermitido.equalsIgnoreCase(ordem.servico.categoria))
                throw new IllegalStateException("O serviço da ordem não é permitido nesse box.");
            if (box.ordensEmExecucao.size() >= box.capacidadeMaximaVeiculos)
                throw new IllegalStateException("Box sem capacidade disponível.");
            if (box.ordensEmExecucao.stream().anyMatch(o -> !o.servico.categoria.equalsIgnoreCase(ordem.servico.categoria)))
                throw new IllegalStateException("O box já possui ordens de outro tipo de serviço.");
            box.adicionarOrdem(ordem);
            ordem.box = box;
            ordem.status = StatusOrdem.EM_EXECUCAO;
        }
        void finalizarOrdem(int codigo) {
            OrdemServico ordem = buscarOrdem(codigo);
            if (ordem == null) throw new IllegalArgumentException("Ordem não encontrada.");
            if (ordem.status != StatusOrdem.EM_EXECUCAO) throw new IllegalStateException("Somente ordens em execução podem ser finalizadas.");
            ordem.box.removerOrdem(ordem);
            ordem.status = StatusOrdem.FINALIZADA;
        }
        List<OrdemServico> buscarPorStatus(StatusOrdem status) {
            return ordens.stream().filter(ordem -> ordem.status == status).toList();
        }
    }
}
