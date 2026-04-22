package br.com.alura.screenmatch.controller;

import br.com.alura.screenmatch.dto.EpisodioDTO;
import br.com.alura.screenmatch.dto.SerieDTO;
import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.service.SerieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/series")
public class SerieController {

    @Autowired
    private SerieService servico;

    @GetMapping
    public List<SerieDTO> obterSeries(){
        return servico.obterTodasAsSeries();
    }

    @GetMapping("/top5")
    public List<SerieDTO> obterTop5Series(){
        return servico.obterTop5Series();
    }

    @GetMapping("/lancamentos")
    public List<SerieDTO> obterLancamentosSeries(){
        return servico.obterLancamentosSeries();
    }

    @GetMapping("/{id}")
    public SerieDTO obterPorId(@PathVariable Long id){
        return servico.obterSeriePorId(id);
    }

    //http://localhost:8080/series/20/temporadas/todas
    @GetMapping("/{id}/temporadas/todas")
    public List<EpisodioDTO> obterEpisodios(@PathVariable Long id){
        return servico.obterEpisodios(id);
    }

    @GetMapping("/{id}/temporadas/{temp}")
    public List<EpisodioDTO> obterEpisodiosTemporada(@PathVariable Long id,@PathVariable Long temp){
        return servico.obterEpisodiosTemporada(id,temp);
    }

    @GetMapping("/categoria/{categoria}")
    public List<SerieDTO> obterSeriesCategoria(@PathVariable String categoria){

        return servico.obterSeriesCategoria(categoria);
    }
}
