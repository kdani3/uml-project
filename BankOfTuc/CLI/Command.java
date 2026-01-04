package BankOfTuc.CLI;

import java.util.Scanner;

public interface Command {
    void execute(Scanner sc);
    String getDescription();
}