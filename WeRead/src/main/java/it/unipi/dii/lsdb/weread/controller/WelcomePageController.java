package it.unipi.dii.lsdb.weread.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.model.User;
import it.unipi.dii.lsdb.weread.model.Session;
import it.unipi.dii.lsdb.weread.utils.Utils;


import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controller for the Welcome page
 */
public class WelcomePageController {
    @FXML private TextField loginUsernameTextField;
    @FXML private PasswordField loginPasswordField;
    @FXML private TextField registrationNameTextField;
    @FXML private TextField registrationSurnameTextField;
    @FXML private TextField registrationUsernameTextField;
    @FXML private TextField registrationEmailTextField;
    @FXML private PasswordField registrationPasswordField;
    @FXML private PasswordField registrationConfirmPasswordField;
    @FXML private ComboBox registrationInterestingCategories;
    @FXML private Button loginButton;
    @FXML private Button registrationButton;
    private final List<String> CATEGORIES = new ArrayList<>(Arrays.asList("Picture Book", "Easy Reader", "Comics",
            "Graphic Novels", "Fantasy","Paranormal",
            "History","Biography", "Mystery","Thriller","Crime", "Love Poems",
            "Anthology", "Poetry", "Erotic Romance", "Romantic Suspense", "Romance",
            "Adventures","Diary","Family and Relationship","Young Fiction"));
    private MongoDBDriver mongoDBDriver;


    /**
     * Method called when the controller is initialized
     */
    public void initialize()
    {
        mongoDBDriver = MongoDBDriver.getInstance();

        registrationInterestingCategories.setItems(FXCollections.observableArrayList(CATEGORIES));


        loginButton.setOnMouseClicked(mouseEvent -> handleLoginButtonAction(mouseEvent));
        //registrationButton.setOnMouseClicked(mouseEvent -> handleRegisterButtonAction(mouseEvent));
        mongoDBDriver = mongoDBDriver.getInstance();
    }


    /**
     * Method used to handle the Login button click event
     * @param actionEvent   The event that occurs when the user click the Login button
     */
    private void handleLoginButtonAction(MouseEvent actionEvent) {
        System.out.println("Sono dentro a LOGIN BUTTON");
        if (loginUsernameTextField.getText().equals("") || loginPasswordField.getText().equals(""))
        {
            Utils.showErrorAlert("You need to insert all the values!");
        }
        else
        {
            if (mongoDBDriver.checkLogin(loginUsernameTextField.getText(), loginPasswordField.getText()))
            {
                Session newSession = Session.getInstance();
                User user = new User(loginUsernameTextField.getText());
                newSession.setLoggedUser(user);
                System.out.println("Login Done:" + loginUsernameTextField.getText());
            }
            else
            {
                Utils.showErrorAlert("Login failed!");
            }
        }
    }









}