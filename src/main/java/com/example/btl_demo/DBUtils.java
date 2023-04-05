package com.example.btl_demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;

import java.io.Console;
import java.io.IOException;
import java.sql.*;

public class DBUtils {
    public static void changeScence(ActionEvent event , String fxmlFile , String title , String username){
        Parent root = null;

        if(username !=null){
            try {
                FXMLLoader loader = new FXMLLoader(DBUtils.class.getResource(fxmlFile));
                root = loader.load();
                LoggedController loggedController = loader.getController();
                loggedController.setUserInformation(username);

            }catch (IOException e){
                e.printStackTrace();
            }
        }else{
            try{
                root = FXMLLoader.load(DBUtils.class.getResource(fxmlFile));

            }catch (IOException e){
                e.printStackTrace();
            }
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage .setTitle(title);
        stage.setScene(new Scene(root, 600, 400));
        stage.show();
    }

    public static void SignUpUser(ActionEvent event , String username, String password, String confirmPass) {
        Connection connection = null;
        PreparedStatement psInsert = null;
        PreparedStatement psCheckUserExists = null;
        PreparedStatement psDeleteUser = null;
        ResultSet resultSet = null;


        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/account", "root", "123456");
            psCheckUserExists = connection.prepareStatement("SELECT * FROM user WHERE USERNAME = ?");
            psCheckUserExists.setString(1, username);
            resultSet = psCheckUserExists.executeQuery();

            if (resultSet.isBeforeFirst()) {
                System.out.println("User already exists");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("You can not use this username.");
                alert.show();
            } else {
                psInsert = connection.prepareStatement("INSERT INTO user (USERNAME , PASS, CONFIRMPASS) VALUES (?,?,?) ");
                psInsert.setString(1, username);
                psInsert.setString(2, password);
                psInsert.setString(3, confirmPass);
                psInsert.executeUpdate();

                if(password.equals(confirmPass)) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Sign Up Success");
                    alert.setTitle("Success");
                    alert.show();
                    changeScence(event, "hello-view.fxml", "Log In", null);
                }else {
                    psDeleteUser =connection.prepareStatement("DELETE FROM USER WHERE PASS != CONFIRMPASS");
                    psDeleteUser.executeUpdate();
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Password and ConfirmPassword are difference");
                    alert.setTitle("Warning");
                    alert.show();
                }


            }

        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            if (resultSet != null){
                try{
                    resultSet.close();
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }

            if(psCheckUserExists != null){
                try{
                    psCheckUserExists.close();
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }

            if (psInsert != null){
                try{
                    psInsert.close();
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }

            if (psDeleteUser != null){
                try{
                    psDeleteUser.close();
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }

            if (connection != null){
                try{
                    connection.close();
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public static void LogInUser(ActionEvent event , String username , String password){
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet =  null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/account", "root", "123456");
            preparedStatement = connection.prepareStatement("SELECT PASS FROM user WHERE USERNAME = ?");
            preparedStatement.setString(1, username);
            resultSet=preparedStatement.executeQuery();

            if (!resultSet.isBeforeFirst()){
                System.out.println("User not found in database");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Provided credentials are incorrect!");
                alert.show();
            }else{
                while(resultSet.next()){
                    String retrievedPassword = resultSet.getString("PASS");
                    if (retrievedPassword.equals(password)){
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setContentText("Login Success ! ");
                        alert.setTitle("Login Success");
                        alert.show();
                        changeScence(event, "logged.fxml", "Welcome",username);
                    }else{
                        System.out.println("Pass did not match ");
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("The provided credentials are incorrect");
                        alert.show();
                    }
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            if(resultSet != null){
                try {
                    resultSet.close();
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }
            if (preparedStatement != null){
                try {
                    preparedStatement.close();
                }catch (SQLException e){
                    e.printStackTrace();
                }
            }

            if (connection != null){
                try {
                    connection.close();
                }catch(SQLException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
