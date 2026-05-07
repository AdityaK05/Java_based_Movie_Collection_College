package cinema.model;

import java.io.Serializable;
import java.time.Year;

public class MovieData implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private final String title, genre;
    private final double rating;
    private final int year;

    public MovieData(int id, String title, String genre, double rating, int year) {
        if (title == null || title.trim().isEmpty())
            throw new IllegalArgumentException("Title cannot be empty.");
        if (genre == null || genre.trim().isEmpty())
            throw new IllegalArgumentException("Genre cannot be empty.");
        if (rating < 0 || rating > 10)
            throw new IllegalArgumentException("Rating must be between 0 and 10.");
        int maxYear = Year.now().getValue() + 1;
        if (year < 1888 || year > maxYear)
            throw new IllegalArgumentException("Year must be between 1888 and " + maxYear + ".");
        this.id = id;
        this.title = title.trim();
        this.genre = genre.trim();
        this.rating = rating;
        this.year = year;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public double getRating() { return rating; }
    public int getYear() { return year; }
}
