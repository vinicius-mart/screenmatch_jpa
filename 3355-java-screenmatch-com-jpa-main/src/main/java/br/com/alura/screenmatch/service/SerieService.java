package br.com.alura.screenmatch.service;

import br.com.alura.screenmatch.dto.EpisodioDTO;
import br.com.alura.screenmatch.dto.SerieDTO;
import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SerieService {

    @Autowired
    private SerieRepository repositorio;

    public List<SerieDTO> obterTodasAsSeries() {
        return converteDados(repositorio.findAll());
    }

    public List<SerieDTO> obterTop5Series() {
        return converteDados(repositorio.findTop5ByOrderByAvaliacaoDesc());
    }

    private List<SerieDTO> converteDados(List<Serie> series) {
        return series.stream()
                .map(s -> new SerieDTO(s.getId(), s.getTitulo(), s.getTotalTemporadas(), s.getAvaliacao(), s.getGenero(), s.getAtores(), s.getPoster()))
                .collect(Collectors.toList());
    }

    public List<SerieDTO> obterLancamentosSeries() {
        return converteDados(repositorio.encontrarEpisodiosMaisRecentes());
    }

    public SerieDTO obterSeriePorId(Long id) {
        Optional<Serie> serieOptional = repositorio.findById(id);
        if (serieOptional.isPresent()) {
            Serie s = serieOptional.get();
            return new SerieDTO(s.getId(), s.getTitulo(), s.getTotalTemporadas(), s.getAvaliacao(), s.getGenero(), s.getAtores(), s.getPoster());
        }
        return null;
    }

    public List<EpisodioDTO> obterEpisodios(Long id) {
        Optional<Serie> serieOptional = repositorio.findById(id);
        return serieOptional.map(serie -> serie.getEpisodios().stream()
                .map(e -> new EpisodioDTO(e.getId(), e.getTemporada(), e.getTitulo(), e.getNumeroEpisodio(), e.getAvaliacao(), e.getDataLancamento()))
                .collect(Collectors.toList())).orElse(null);

    }

    public List<EpisodioDTO> obterEpisodiosTemporada(Long id, Long temp) {
        return repositorio.obterEpisodiosPorTemporada(id,temp)
                .stream()
                .map(e -> new EpisodioDTO(e.getId(), e.getTemporada(), e.getTitulo(), e.getNumeroEpisodio(), e.getAvaliacao(), e.getDataLancamento()))
                .collect(Collectors.toList());
    }

    public List<SerieDTO> obterSeriesCategoria(String categoria) {
        Categoria categoriaC = Categoria.fromStringPortugues(categoria);
        return converteDados(repositorio.findByGenero(categoriaC));
    }
}
