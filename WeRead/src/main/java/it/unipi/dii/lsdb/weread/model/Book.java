package it.unipi.dii.lsdb.weread.model;

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

    public Book(String isbn, String title, String language, String category, String publisher, String description, String author, int numPages, int publicationYear, String imageURL) {
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



}
