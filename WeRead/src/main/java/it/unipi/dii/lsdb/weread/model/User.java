package it.unipi.dii.lsdb.weread.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String name;
    private String surname;
    private String email;
    private String password;
    private boolean isAdministrator;
    private List<Book> favourite;
    private List<Review> reviews;
    private List<ReadingList> readingList;

    public User(){}

    public User(String username, String name, String surname, String email, String password, boolean isAdministrator, List<Book> favourite, List<ReadingList> readingLists, List<Review> reviews) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.isAdministrator = isAdministrator;
        this.favourite = favourite;
        this.readingList = readingLists;
        this.reviews = reviews;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getIsAdministrator() {
        return isAdministrator;
    }

    public void setSurname(String username) {
        this.username = username;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Book> getFavourite(){return favourite;}

    public List<ReadingList> getReadingLists() {return readingList;}

    public List<Review> getReviewList() { return reviews; }

    public void addFavoriteBook(String isbn, String title, String author, String imageURL){
        Book toAdd = new Book(isbn, title, author, imageURL);
        favourite.add(toAdd);
    }

}
