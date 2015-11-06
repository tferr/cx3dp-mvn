package ini.cx3d.gui.simulation;//GMC changes




import java.io.*;

//public boolean matches(String regex)

public class Test{
   public static void main(String args[]){
      String Str = new String("Welcome to Tutorialspoint.com");

      System.out.print("Return Value :" );
      System.out.println(Str.matches("(.*)Tutorials(.*)"));

      System.out.print("Return Value :" );
      System.out.println(Str.matches("Tutorials"));

      System.out.print("Return Value :" );
      System.out.println(Str.matches("Welcome(.*)"));
   }
}