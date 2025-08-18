package utils;

public class Formatting {

   // Text Colours
   public static final String RESET = "\033[0m";
   public static final String RED = "\033[91m";
   public static final String GREEN = "\033[92m";
   public static final String YELLOW = "\033[93m";
   public static final String BLUE = "\033[94m";
   public static final String MAGENTA = "\033[95m";
   public static final String CYAN = "\033[96m";
   public static final String WHITE = "\033[97m";

   // Cursor Movement
   public static final String CURSOR_RESET = "\033[H";
   public static final String CURSOR_UP = "\033[1A";
   public static final String CURSOR_RIGHT = "\033[1C";

   // Other
   public static final String CLEAR_LINE = "\033[2K";
   public static final String CLEAR_SCREEN = "\033[2J";
   public static final String CLEAR_SCROLL_BUF = "\033[3J";

   public static void print(String colour, String text) {
      System.out.print(colour + text + RESET);
   }

   public static String colour(String colour, String text) {
      return colour + text + RESET;
   }

   public static void clearLine() {
      System.out.print(CURSOR_UP + CLEAR_LINE);
   }

   public static void clearLine(int lineCount) {
      for (int i = 0; i < lineCount; i++) {
         clearLine();
      }
   }

   public static void cursorUp() {
      System.out.print(CURSOR_UP);
   }

   public static void cursorUp(int lineCount) {
      for (int i = 0; i < lineCount; i++) {
         cursorUp();
      }
   }

   public static void cursorRight(int lineCount) {
      for (int i = 0; i < lineCount; i++) {
         System.out.print(CURSOR_RIGHT);
      }
   }

   public static void error(String text) {
      cursorUp();
      cursorRight(40);
      print(RED, text);
   }
}
