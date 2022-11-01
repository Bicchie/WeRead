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
    private List<Book> favoriteBooks;

    public User(){}

    public User(String username, String name, String surname, String email, String password, boolean isAdministrator) {
        this.username = username;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.isAdministrator = isAdministrator;
        this.favoriteBooks = new ArrayList<>();
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

    public void addFavoriteBook(String isbn, String title, String author, String imageURL){
        Book toAdd = new Book(isbn, title, author, imageURL);
        favoriteBooks.add(toAdd);
    }

}
