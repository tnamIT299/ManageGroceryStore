package Controller;

import DAO.UserDAO;
import MailConfig.MailConfig;
import Model.User;
import Model.Verification;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.net.URL;
import java.util.ResourceBundle;


public class ForgotPasswordController extends Application {
    private static final UserDAO userDao = new UserDAO();

    public TextField email;
    public Button btnSendOTP;
    public TextField newPassword;
    public TextField confirmPassword;
    public Button btnOk;
    public TextField otp;
    public Button back_button;
    public class EmailValidator {
        private static final String EMAIL_REGEX =
                "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";

        public static boolean isValidEmail(String email) {
            Pattern pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(email);
            return matcher.matches();
        }
    }

    public void initialize() {
        btnSendOTP();
        btnOk();
    }

    private void btnSendOTP() {
        btnSendOTP.setOnAction(actionEvent -> {
            if(userDao.getUserByEmail(email.getText()) == null){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Email don't exists");
                alert.showAndWait();
                return;
            }
            MailConfig.sendOTP(email.getText());
        });
    }

    private void btnOk() {
        btnOk.setOnAction(actionEvent -> {
            Alert alert =  new Alert(Alert.AlertType.ERROR);
            User user = userDao.getUserByEmail(email.getText());
            boolean isValid = EmailValidator.isValidEmail(email.getText());
            if (isValid) {
                System.out.println("Email is valid.");
            } else {
                System.out.println("Email is invalid.");
            }
            if(user == null) {
                alert.showAndWait();
                return;
            }

            if(otp.getText().equals("")) {
                System.out.println("otp");
                alert.showAndWait();
                return;
            }

            if(!MailConfig.verifyOTP(email.getText(), otp.getText())) {
                System.out.println("mail");
                alert.showAndWait();
                return;
            }

            if(newPassword.getText().equals("") || !Verification.CheckPass(newPassword.getText())) {
                System.out.println("pw");

                alert.showAndWait();
                return;
            }

            if(!newPassword.getText().equals(confirmPassword.getText())) {
                System.out.println("cf");

                alert.showAndWait();
                return;
            }

            userDao.changePassword(user.getUsername(), newPassword.getText());
        });
    }

    @Override
    public void start(Stage stage) throws Exception {

    }

}
