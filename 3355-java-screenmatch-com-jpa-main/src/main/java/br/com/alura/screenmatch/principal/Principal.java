package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = System.getenv("OMDB_APIKEY");
    List<DadosSerie> dadosSerie = new ArrayList<>();
    private List<Serie> series = new ArrayList<>();

    SerieRepository repositorio;

    Optional<Serie> serieBuscada;

    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {

        var opcao = -1;

        while(opcao != 0) {

            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries
                    4 - Buscar série pelo título
                    5 - Buscar série por ator
                    6 - Listar top 5 séries
                    7 - Listar séries por gênero
                    8 - Listar séries por total de temporadas e avaliação
                    9 - Buscar episódio por trecho
                    10 - Buscar top episódio por série
                    11 - Buscar episodios após uma data
                    
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeries();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    listarTop5Series();
                    break;
                case 7:
                    listarSeriesPorGenero();
                    break;
                case 8:
                    listarSeriesTotalTemporadas();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    buscarTopEpisodiosSerie();
                    break;
                case 11:
                    buscarEpisodiosAposData();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        dadosSerie.add(dados);
        Serie serie = new Serie(dados);
        repositorio.save(serie);
        series.add(serie);
        System.out.println(dados);

    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listarSeries();
        System.out.println("qual série deseja buscar os episódios?");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);
//        Optional<Serie> serie = series.stream()
//                .filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
//                .findFirst();

        if(serie.isPresent()){

            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);


        } else {
            System.out.println("série não encontrada");
        }


        //DadosSerie dadosSerie = getDadosSerie();

    }

    public void listarSeries(){
        series =  repositorio.findAll();

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome");
        var nomeSerie = leitura.nextLine();
        serieBuscada = repositorio.findByTituloContainingIgnoreCase(nomeSerie);
        System.out.println(serieBuscada);
    }

    private void buscarSeriePorAtor() {
        System.out.println("Qual o ator pesquisado? ");
        var ator = leitura.nextLine();
        System.out.println("Listar séries a partir de qual nota? ");
        var nota = leitura.nextDouble();
        List<Serie> seriesEncontradas = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(ator, nota);

        seriesEncontradas.stream()
                .forEach(s ->
                        System.out.println("série: " + s.getTitulo() + " nota: " + s.getAvaliacao()));
    }

    private void listarTop5Series() {
        List<Serie> seriesTop5 = repositorio.findTop5ByOrderByAvaliacaoDesc();
        seriesTop5.stream()
                .forEach(s ->
                        System.out.println("série: " + s.getTitulo() + " nota: " + s.getAvaliacao()));
    }

    private void listarSeriesPorGenero() {
        System.out.println("qual gênero deseja buscar? ");
        var generoBusca = leitura.nextLine();
        Categoria categoria = Categoria.fromStringPortugues(generoBusca);
        List<Serie> seriesPorGenero = repositorio.findByGenero(categoria);
        seriesPorGenero.stream()
                .forEach(s ->
                        System.out.println("série: " + s.getTitulo() + " nota: " + s.getAvaliacao()));

    }

    private void listarSeriesTotalTemporadas() {
        System.out.println("digite o máximo de temporadas: ");
        var totalTemporadas = leitura.nextInt();
        System.out.println("digite a nota mínima: ");
        var notaMinima = leitura.nextDouble();
        //List<Serie> seriesTempAvaliacao = repositorio.findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(totalTemporadas,notaMinima);
        List<Serie> seriesTempAvaliacao = repositorio.seriesPorTemporadaEAvaliacao(totalTemporadas,notaMinima);
        seriesTempAvaliacao.stream()
                .forEach(s ->
                        System.out.println("série: " + s.getTitulo() + " nota: " + s.getAvaliacao()));
    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("digite o trecho do episódio buscado");
        var trechoEpisodio = leitura.nextLine();
        List<Episodio> episodiosEncontrados =  repositorio.episodiosPorTrecho(trechoEpisodio);
        episodiosEncontrados.forEach(e ->
                System.out.printf("Série: %s Temporada %s - Episódio %s - %s\n",
                        e.getSerie().getTitulo(), e.getTemporada(),
                        e.getNumeroEpisodio(), e.getTitulo()));

    }

    private void buscarTopEpisodiosSerie() {
        buscarSeriePorTitulo();
        if(serieBuscada.isPresent()){
            Serie serie = serieBuscada.get();
            List<Episodio> topEpisodios = repositorio.topEpisodiosSerie(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Série: %s Temporada %s - Episódio %s - %s - Avaliação %s\n",
                            e.getSerie().getTitulo(), e.getTemporada(),
                            e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao()));


        }
    }


    private void buscarEpisodiosAposData() {
        buscarSeriePorTitulo();
        if (serieBuscada.isPresent()) {
            Serie serie = serieBuscada.get();
            System.out.println("digite o ano a partir do qual deseja pesquisar");
            var ano = leitura.nextInt();
            List<Episodio> episodiosAposData = repositorio.episodiosAposData(serie, ano);
            episodiosAposData.forEach(System.out::println);

            }
        }

    }
