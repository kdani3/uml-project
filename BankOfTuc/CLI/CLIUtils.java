package BankOfTuc.CLI;

import java.security.SecureRandom;
import java.util.Scanner;
import java.util.stream.Collectors;

import BankOfTuc.User;
import BankOfTuc.Auth.LoginManager;
import BankOfTuc.Auth.PasswordUtils;
import BankOfTuc.Auth.QrUtils;
import BankOfTuc.Bookkeeping.UserFileManagement;
import dev.samstevens.totp.exceptions.QrGenerationException;

public class CLIUtils {
public static boolean verifyUserIdentity(User user, Scanner sc){
         System.out.println("Enter  Password:");
            String password = sc.nextLine();
            if(!PasswordUtils.verifyPassword(password.toCharArray(), user.getSalt(), user.getHashedPassword()))
                return false;
        
            if(user.hasQR()){
                System.out.println("Enter Qr Code");
                String qrcode = sc.nextLine();

                if(!QrUtils.verifyQrCode(user.getQrCode(), qrcode))
                    return false;
            }
            return true;

    }
    public static boolean SettingsMenu(Scanner sc, LoginManager login, User user,UserFileManagement ufm){
        while (login.isLoggedIn(user.getUsername())) {

            System.out.println("\n--- Settings Menu (" + user + ") ---");
            System.out.println("1. Reset Password");
            System.out.println("2. Change Email");
            System.out.println("3. Change Usermame");
            System.out.println("4. Create Qr Code");
            System.out.println("5. Return");
            System.out.print("> ");

            String input = sc.nextLine();

            login.activity(user.getUsername());

            switch(input) {
                case "1":  //reset password
                    if (!verifyUserIdentity(user, sc)) break;

                    while (true) {
                        System.out.println("Enter new password:");
                        String p1 = sc.nextLine();

                        System.out.println("Enter again:");
                        String p2 = sc.nextLine();

                        if (p1.equals(p2)) {
                            user.setPassword(p1);
                            ufm.updateUser(user);

                            System.out.println("Password updated!");
                            break;
                        }

                        System.out.println("Passwords do not match. Try again? (y/n)");
                        if (!sc.nextLine().equalsIgnoreCase("y")) continue;
                    }
                    break;
                
                case "2":
                    if (!verifyUserIdentity(user, sc)) break;

                    System.out.println("Enter new email");
                    String email = sc.nextLine();
                    user.setEmail(email);
                    System.out.println("Email Updated");
                    ufm.updateUser(user);
                    break;

                case "3":
                    if (!verifyUserIdentity(user, sc)) break;

                    System.out.println("Enter new username");
                    String username = sc.nextLine();
                    user.setUsername(username);
                    System.out.println("username Updated");
                    ufm.updateUser(user);
                    break;
                
                case "4":
                    if (!user.hasQR()){
                        if(CLIUtils.createQr(user, sc))
                            break;
                    }
                    else {
                        System.out.println("Qr Code Already in Use");
                    }
                    break;
                case "5":
                    return true;


            }
        }  
        return false;   
    }


    public static boolean createQr(User user,Scanner sc){
        String[] qrResult = null;
        try {
            qrResult = QrUtils.createQr(user.getUsername());
        } catch (QrGenerationException e) {
            System.err.println("Error while creating QR for user"+ user.getUsername());
            e.printStackTrace();
        }
        String qrUriString = qrResult[0];
        String qrSecret = qrResult[1];

        ConsoleImagePrinter.showQrImage(qrUriString,"Qr"); 
        while(true){

            System.out.println("Enter Qr Code");
            String qrString = sc.nextLine();
            
            if(QrUtils.verifyQrCode(qrSecret, qrString)){
                user.setQrCode(qrSecret);
                System.out.println("Qr Code Created");
                return true;
            }
            else{
                System.out.println("Wrong Qr Code \n Retry");
            }
        }
    }
    public static void asciiLogo(){
        String logo ="""    
                      
        ###################################                                
       #################################                                
                            ######                                      
           **************** ###### ***                                  
             ************* ######   **                                  
                           ###### ********                  ##              
                ********* ####### ** ****** #####   #####  ##  ##           
                   ****** ######       **** ##  ##  ## ##  #####            
                          ######  ** ****** #### #  ##  #  ##  ## #####           
                         ######   """;
                System.out.println(logo);
    }

    public static String generateUnicodePassword(int length) {
    SecureRandom random = new SecureRandom();
    
    return random.ints(length, 0, 62) 
                .map(i -> {
                    if (i < 10) return 0x30 + i;       
                    if (i < 36) return 0x41 + (i - 10); 
                    return 0x61 + (i - 36);             
                })
                .mapToObj(cp -> String.valueOf((char) cp))
                .collect(Collectors.joining());
    }
}
