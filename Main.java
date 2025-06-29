import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class Main {
   // The ASCII density map corresponding to the brightness
   static final String ASCII_DENSITY = " $@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`'. ";

   // ANSI escape codes for colours
   static final String RESET = "\u001B[0;49m";
   static final String RED = "\u001B[91m";
   static final String GREEN = "\u001B[92m";
   static final String YELLOW = "\u001B[93m";
   static final String BLUE = "\u001B[94m";

   static final int MAX_WIDTH = 88;

   public static void main(String[] args) {

      try (Scanner input = new Scanner(System.in)) {
         File[] imageFiles = new File("images").listFiles(
            (File directory, String fileName) -> fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
         );
         Arrays.sort(imageFiles);

         if (imageFiles == null || imageFiles.length == 0) {
            System.out.println(colour(RED, "Please add some images in the 'images' directory!"));
            return;
         }

         System.out.println("What image would you like to convert to ASCII?");
         for (int i = 0; i < imageFiles.length; i++) {
            System.out.println(colour(YELLOW, (i + 1) + "") + " - " + colour(BLUE, imageFiles[i].getName()));
         }

         int fileIndex;
         while (true) {
            print(GREEN, " > ");
            String fileIndexString = input.nextLine().trim();

            if (fileIndexString.isEmpty()) {
               clearLine(1);
            } else {
               try {
                  fileIndex = Integer.parseInt(fileIndexString);
               } catch (NumberFormatException e) {
                  clearLine(1);
                  System.out.print(colour(GREEN, " > ") + fileIndexString + colour(RED, "\r\u001B[40C Please enter a valid file number.\n"));
                  continue;
               }

               if (fileIndex > 0 && fileIndex <= imageFiles.length) {
                  print(GREEN, "Converting " + imageFiles[fileIndex - 1].getName() + " to ASCII...\n");
                  break;
               } else {
                  clearLine(1);
                  System.out.print(colour(GREEN, " > ") + fileIndexString + colour(RED, "\r\u001B[40C This file does not exist, please pick one from the above list.\n"));
               }
            }
         }

         try {
            File imageFile = new File("images/" + imageFiles[fileIndex - 1].getName());
            BufferedImage image = ImageIO.read(imageFile);
            int imageHeight = image.getHeight();
            int imageWidth = image.getWidth();

            int imageScale = Math.max(1, imageWidth / MAX_WIDTH); // Avoid division by zero
            String[] asciiRows = new String[imageHeight];

            for (int y = 0; y < imageHeight; y += imageScale) {
               for (int x = 0; x < imageWidth; x += imageScale) {
                  int pixel = image.getRGB(x, y);
                  int alpha = (pixel >> 24) & 0xFF;

                  // Ensure asciiRows[y] is initialized
                  if (asciiRows[y] == null) {
                     asciiRows[y] = "";
                  }

                  if (alpha == 0) {
                     asciiRows[y] += "  ";
                  } else {
                     int red = (pixel >> 16) & 0xFF;
                     int green = (pixel >> 8) & 0xFF;
                     int blue = pixel & 0xFF;
                     int brightness = (red + green + blue) / 3;
                     int pixelIndex = (int) Math.floor(brightness * (ASCII_DENSITY.length() - 1) / 255);
                     asciiRows[y] += ASCII_DENSITY.charAt(pixelIndex) + " ";
                  }
               }

               if (!asciiRows[y].trim().isEmpty()) {
                  System.out.println(asciiRows[y]);
               }
            }
         } catch (IOException e) {
            System.out.println(colour(RED, "Failed to read the image."));
         }
      }
   }

   public static void print(String colour, String text) {
      System.out.print(colour + text + RESET);
   }

   public static String colour(String colour, String text) {
      return String.format("%s%s%s", colour, text, RESET);
   }

   public static void clearLine(int clearLineCount) {
      for (int i = 0; i < clearLineCount; i++) {
         System.out.print("\033[1A\033[2K");
      }
   }
}