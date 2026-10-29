package dmit2015.service;


import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import dmit2015.entity.Movie;
import dmit2015.repository.MovieRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MovieService {
    @Inject
    private SecurityContext securityContext;

    @Inject
    private MovieRepository movieRepository;

    public void addMovie(Movie movie) {

        if(!securityContext.isCallerInRole("Sales")){
            throw new RuntimeException("Access denied, Only sales can create movies");
        }

        LocalDateTime now = LocalDateTime.now();

        movie.setCreateTime(now);
        movie.setUpdateTime(now);

        String username= securityContext.getCallerPrincipal().getName();
        movie.setUsername(username);


        movieRepository.insert(movie);
    }

    public void updateMovie(Movie movie) {

        if (!securityContext.isCallerInRole("Sales")) {
            throw new RuntimeException(
                    "Access denied. Only Sales can edit movies."
            );
        }

        if (movie == null || movie.getId() == null) {
            throw new RuntimeException("Invalid movie.");
        }

        Movie storedMovie = movieRepository
                .findOptionalById(movie.getId())
                .orElseThrow(() ->
                        new RuntimeException("Movie not found.")
                );

        String username =
                securityContext.getCallerPrincipal().getName();

        if (!username.equals(storedMovie.getUsername())) {
            throw new RuntimeException(
                    "Access denied. You cannot edit this movie."
            );
        }

        movie.setUsername(storedMovie.getUsername());
        movie.setCreateTime(storedMovie.getCreateTime());
        movie.setUpdateTime(LocalDateTime.now());

        movieRepository.update(movie);
    }







    public void deleteMovie(Movie movie) {
        movieRepository.delete(movie);
    }

    public Optional<Movie> findMovieById(Long movieId) {

        boolean isAdmin = securityContext.isCallerInRole("Administration");

        boolean isSales = securityContext.isCallerInRole("Sales");

        if(!isAdmin && !isSales){
            throw  new RuntimeException("Access Denied");
        }

        if(movieId==null){
            return  Optional.empty();
        }

        Optional<Movie> result = movieRepository.findOptionalById(movieId);

        if(result.isPresent() && !isAdmin){

            String username= securityContext.getCallerPrincipal().getName();
            if (!username.equals(result.get().getUsername())){
                throw  new RuntimeException("Access denied, not your movie");
            }
        }

        return result;


    }

    public List<Movie> getAllMovies() {
        String username =
                securityContext.getCallerPrincipal().getName();

        if (username.equalsIgnoreCase("anonymous")) {
            throw new RuntimeException(
                    "Access Denied. Anonymous users do not have " +
                            "permission to access this method."
            );
        }

        boolean hasRequiredRoles =
                securityContext.isCallerInRole("Sales")
                        || securityContext.isCallerInRole("Administration");

        if (!hasRequiredRoles) {
            throw new RuntimeException(
                    "Access denied. You role does not have " +
                            "permission to access this method."
            );
        }

        boolean hasSalesRole =
                securityContext.isCallerInRole("Sales");

        if (hasSalesRole) {
            return movieRepository.findAllByUsername(username);
        }

        return movieRepository.findAll();
    }






    public long countMovies() {
        return movieRepository.count();
    }
}
