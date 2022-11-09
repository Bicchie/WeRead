package it.unipi.dii.lsdb.weread.controller;

import it.unipi.dii.lsdb.weread.persistence.Neo4jDriver;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import it.unipi.dii.lsdb.weread.persistence.MongoDBDriver;
import it.unipi.dii.lsdb.weread.model.User;
import it.unipi.dii.lsdb.weread.model.Session;
import it.unipi.dii.lsdb.weread.utils.Utils;


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
    @FXML private Button loginButton;
    @FXML private TextField registrationNameTextField;
    @FXML private TextField registrationSurnameTextField;
    @FXML private TextField registrationUsernameTextField;
    @FXML private TextField registrationEmailTextField;
    @FXML private PasswordField registrationPasswordField;
    @FXML private PasswordField registrationConfirmPasswordField;
    @FXML private CheckBox registrationPictureBook;
    @FXML private CheckBox registrationEasyReader;
    @FXML private CheckBox registrationComics;
    @FXML private CheckBox registrationGraphicNovels;
    @FXML private CheckBox registrationFantasy;
    @FXML private CheckBox registrationParanormal;
    @FXML private CheckBox registrationHistory;
    @FXML private CheckBox registrationBiography;
    @FXML private CheckBox registrationMystery;
    @FXML private CheckBox registrationThriller;
    @FXML private CheckBox registrationCrime;
    @FXML private CheckBox registrationLovePoems;
    @FXML private CheckBox registrationAnthology;
    @FXML private CheckBox registrationPoetry;
    @FXML private CheckBox registrationSuspense;
    @FXML private CheckBox registrationRomance;
    @FXML private CheckBox registrationAdventures;
    @FXML private CheckBox registrationDiary;
    @FXML private CheckBox registrationFamilyandRelationship;
    @FXML private CheckBox registrationYoungFiction;
    @FXML private Button registrationButton;
    private MongoDBDriver mongoDBDriver;
    private Neo4jDriver neo4jDriver;

    /**
     * Method called when the controller is initialized
     */
    public void initialize()
    {
        mongoDBDriver = MongoDBDriver.getInstance();
        neo4jDriver = Neo4jDriver.getInstance();
        loginButton.setOnMouseClicked(mouseEvent -> handleLoginButtonAction(mouseEvent));
        registrationButton.setOnMouseClicked(mouseEvent -> handleRegisterButtonAction(mouseEvent));
    }


    /**
     * Method used to handle the Login button click event
     * @param actionEvent   The event that occurs when the user click the Login button
     */
    private void handleLoginButtonAction(MouseEvent actionEvent) {
        if (loginUsernameTextField.getText().equals("") || loginPasswordField.getText().equals(""))
        {
            Utils.showErrorAlert("You need to insert all the values!");
        }
        else
        {
            if (mongoDBDriver.checkLogin(loginUsernameTextField.getText(), loginPasswordField.getText()))
            {
                Session newSession = Session.getInstance();
                User user = mongoDBDriver.getUserInfo(loginUsernameTextField.getText());
                newSession.setLoggedUser(user);
                System.out.println("Login Done:" + loginUsernameTextField.getText());

                UserPageController userPageController = (UserPageController) Utils.changeScene("/userPage.fxml", actionEvent);
                userPageController.setUser(newSession.getLoggedUser());
            }
            else
            {
                Utils.showErrorAlert("Login failed!");
            }
        }
    }

    /**
     * Method used to handle the Register button click event
     * @param actionEvent   The event that occurs when the user click the Register button
     */
    private void handleRegisterButtonAction(MouseEvent actionEvent) {
        if ((registrationNameTextField.getText().equals("") ||
                registrationSurnameTextField.getText().equals("") ||
                registrationPasswordField.getText().equals("") ||
                registrationEmailTextField.getText().equals("") ||
                registrationUsernameTextField.getText().equals("")) ||
                registrationConfirmPasswordField.getText().equals("")
                || (!registrationPasswordField.getText().equals(registrationConfirmPasswordField.getText())))

        {
            Utils.showErrorAlert("You need to insert all the values! Pay attention that the passwords must be equals!");
        }
        else
        {
            if (!mongoDBDriver.userExists(registrationUsernameTextField.getText()))
            {
                Session newSession = Session.getInstance();
                User registered = new User(registrationUsernameTextField.getText(),registrationNameTextField.getText(),
                        registrationSurnameTextField.getText(), registrationEmailTextField.getText(), registrationPasswordField.getText(),false);
                newSession.setLoggedUser(registered);
                // Add User in neo4j
                if(neo4jDriver.newUser(registered)){
                    System.out.println("User Created in neo4j!");
                } else{
                    System.out.println("PROBLEM in creating User in neo4j!");
                }
                setInterestedCategories(registrationUsernameTextField.getText());

                // add User in MongoDB
                if(mongoDBDriver.addUser(registered)){
                    System.out.println("User Created in mongoDB!");
                } else {
                    System.out.println("PROBLEM in creating User in mongoDB!");
                }

                // CAMBIO SCENA
            }
            else
            {
                Utils.showErrorAlert("Registration failed! Username not available.");
            }
        }
    }

    private void setInterestedCategories(String username){
        if(registrationPictureBook.isSelected())
            neo4jDriver.isInterestedTo(username,"Picture Book");
        if(registrationEasyReader.isSelected())
            neo4jDriver.isInterestedTo(username,"Easy Reader");
        if(registrationComics.isSelected())
            neo4jDriver.isInterestedTo(username,"Comics");
        if(registrationGraphicNovels.isSelected())
            neo4jDriver.isInterestedTo(username, "Graphic Novels");
        if(registrationFantasy.isSelected())
            neo4jDriver.isInterestedTo(username,"Fantasy");
        if(registrationParanormal.isSelected())
            neo4jDriver.isInterestedTo(username,"Paranormal");
        if(registrationHistory.isSelected())
            neo4jDriver.isInterestedTo(username,"History");
        if(registrationBiography.isSelected())
            neo4jDriver.isInterestedTo(username,"Biography");
        if(registrationMystery.isSelected())
            neo4jDriver.isInterestedTo(username,"Mystery");
        if(registrationThriller.isSelected())
            neo4jDriver.isInterestedTo(username,"Thriller");
        if(registrationCrime.isSelected())
            neo4jDriver.isInterestedTo(username,"Crime");
        if(registrationLovePoems.isSelected())
            neo4jDriver.isInterestedTo(username,"Love Poems");
        if(registrationAnthology.isSelected())
            neo4jDriver.isInterestedTo(username,"Anthology");
        if(registrationPoetry.isSelected())
            neo4jDriver.isInterestedTo(username,"Poetry");
        if(registrationSuspense.isSelected())
            neo4jDriver.isInterestedTo(username,"Romantic Suspense");
        if(registrationRomance.isSelected())
            neo4jDriver.isInterestedTo(username,"Romance");
        if(registrationAdventures.isSelected())
            neo4jDriver.isInterestedTo(username,"Adventures");
        if(registrationDiary.isSelected())
            neo4jDriver.isInterestedTo(username,"Diary");
        if(registrationFamilyandRelationship.isSelected())
            neo4jDriver.isInterestedTo(username,"Family and Relationship");
        if(registrationYoungFiction.isSelected())
            neo4jDriver.isInterestedTo(username,"Young Fiction");

    }


}