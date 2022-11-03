package it.unipi.dii.lsdb.weread.model;

import java.util.List;

public class Book {
    private String isbn;
    private String title;
    private String language;
    private String category;
    private String publisher;
    private String description;
    private String author;
    private int numPages;
    private int publicationYear;
    private String imageURL;
    private List<Review> reviews;

    public Book(String isbn, String title, String language, String category, String publisher, String description, String author, int numPages, int publicationYear, String imageURL, List<Review> reviews) {
        this.isbn = isbn;
        this.title = title;
        this.language = language;
        this.category = category;
        this.publisher = publisher;
        this.description = description;
        this.author = author;
        this.publicationYear = publicationYear;
        this.numPages = numPages;
        this.imageURL = imageURL;
        this.reviews = reviews;
    }

    //constructor for favorite books list information only
    public Book(String isbn, String title, String author, String imageURL){
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.imageURL = imageURL;
        this.numPages = -1; //in order to detect if all the information about the book are loaded or not
        this.category = "";
    }

    //constructor for list of books information only
    public Book(String isbn, String title, String author, String category, String imageURL){
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.imageURL = imageURL;
        this.category = category;
        this.numPages = -1; //in order to detect if all the information about the book are loaded or not
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getLanguage() {
        return language;
    }

    public String getCategory() {
        return category;
    }

    public String getPublisher(){ return publisher; }

    public String getAuthor(){ return author;}

    public String getDescription() {
        return description;
    }

    public int getPublicationYear(){ return publicationYear; }

    public int getNumPages() { return numPages;}

    public String getImageURL() {
        return imageURL;
    }

    public List<Review> getReviews() { return reviews; }

    public void addReview(Review rev) {reviews.add(rev);}

    public void removeReview(Review rev) {reviews.remove(rev);}
}
