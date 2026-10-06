package com.example.Movie.service;

import com.example.Movie.model.Movie;
import com.example.Movie.model.MovieData;
import com.example.Movie.model.MovieMatch;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MovieService {

    private final EmbeddingModel embeddingModel;
    private final JsonMapper jsonMapper;
    private List<Movie> movies;


    public MovieService(EmbeddingModel embeddingModel, JsonMapper jsonMapper) {
        this.embeddingModel = embeddingModel;
        this.jsonMapper = jsonMapper;
        movies = new ArrayList<>();
    }

    @PostConstruct
    public void initializeMovies() throws IOException {
        ClassPathResource resource = new ClassPathResource("movie.json");
        InputStream inputStream = resource.getInputStream();

        List<MovieData> movieDataList =
                jsonMapper.readValue(inputStream , new TypeReference<>() {});

        for (MovieData movieData : movieDataList) {
            float[] embedding = embeddingModel.embed(movieData.getDescription());
            Movie movie = new Movie(movieData.getTitle() , movieData.getDescription() , embedding);
            movies.add(movie);
        }

        inputStream.close();

        for(Movie movie : movies){
            System.out.println(Arrays.toString(movie.getEmbeddings()));
        }
        System.out.println("Movies loaded successfully");
    }

    public List<MovieData> search(String query) {
        float[] userEmbedding = embeddingModel.embed(query);

        List<MovieMatch> matches = new ArrayList<>();
        for(Movie movie : movies){
            double similarity = cosineSimilarity(userEmbedding , movie.getEmbeddings());
            MovieMatch match = new MovieMatch(movie.getTitle(), movie.getDescription() , similarity);
            matches.add(match);
        }

        sortBySimilarity(matches);
        return topKMatches(matches , 3);
    }

    public double cosineSimilarity(float[] a, float[] b) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for(int i = 0; i < a.length; i++){
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if(normA == 0 || normB == 0){
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private void sortBySimilarity(List<MovieMatch> matches){
        matches.sort((m1, m2) -> Double.compare(m2.getMatch(), m1.getMatch()));
    }

    private List<MovieData> topKMatches(List<MovieMatch> matches, int k){
        List<MovieData> top = new ArrayList<>();
        for(int i = 0; i < k; i++){
            top.add(new MovieData(matches.get(i).getTitle() , movies.get(i).getDescription()));
        }

        return top;
    }
}
