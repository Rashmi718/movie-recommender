package com.example.Movie.controller;

import com.example.Movie.model.MovieData;
import com.example.Movie.service.MovieService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movie")
public class MovieController {

    private final MovieService movieService;
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/search")
    public List<MovieData> search(@RequestParam("query")  String query) {
        return movieService.search(query);
    }
}
