import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

import static utils.Formatting.*;

public class CLI {
   public static void main(String[] args) {
      if (args.length == 1 && args[0].equalsIgnoreCase("--help")) {
         printHelpMenu();
         return;
      }

      if (args.length > 7) {
         print(RED, "Too many arguments. Consider reading the help menu with the `--help` flag.");
         return;
      }

      boolean hasInputFile = false;

      for (String arg : args) {
         if (startsWithAny(arg, new String[] {"-i=", "-input="})) {
            hasInputFile = true;
         }
      }

      if (!hasInputFile || args.length == 0) {
         print(RED, "Please specify an input file with the `-i=\"_\"` flag. Consider reading the help menu with the `--help` flag.");
         return;
      }

      String inputImageFile = null;
      String asciiDensityMap = " .:-=+*#%@";
      int maxWidth = 150;
      int padding = 2;
      boolean canFlipMap = false;
      boolean canCropImage = false;
      boolean canOutputFile = false;

      for (String arg : args) {
         String[] input = arg.split("=", 2);

         String flag = input[0];
         String value = "";

         if (input.length != 1) {
            value = input[1];
         }

         if (input.length == 2 && input[1].equals("")) {
            System.out.println(colour(RED, "Please specify a value for the following flag: ") + flag);
            return;
         }

         if (input.length == 1 && equalsAny(flag, new String[] {"-i", "-input", "-a", "-ascii", "-w", "-width"})) {
            print(RED, "Please make sure all flags that can accept inputs have a value. Consider reading the help menu with the `--help` flag.");
            return;
         }

         if (equalsAny(flag, new String[] {"-i", "-input"}) && !endsWithAny(value, new String[] {".png", ".jpeg", ".jpg"})) {
            print(RED, "Please select a valid `.png` or `.jpeg/jpg` image file.");
            return;
         }

         switch (flag) {
            case "-i", "-input" -> {
               inputImageFile = value;

               if (value.startsWith("home/")) {
                  inputImageFile = System.getProperty("user.home") + value.replace("home/", "/");
               }
            }
            case "-w", "-width" -> {
               try {
                  maxWidth = Integer.parseInt(value);
               } catch (NumberFormatException e) {
                  print(RED, "Please enter a valid number for the width. Error message: ");
                  e.printStackTrace();
                  return;
               }
            }
            case "-p", "-padding" -> {
               try {
                  padding = Integer.parseInt(value);
               } catch (NumberFormatException e) {
                  print(RED, "Please enter a valid number for the padding. Error message: ");
                  e.printStackTrace();
                  return;
               }
            }
            case "-a", "-ascii" -> asciiDensityMap = value;
            case "-f", "-flip" -> canFlipMap = true;
            case "-o", "-output" -> canOutputFile = true;
            case "-c", "-crop" -> canCropImage = true;
            default -> {
               System.out.println(colour(RED, "Unknown flag: ") + flag);
               return;
            }
         }
      }

      if (canFlipMap) {
         String tempMap = asciiDensityMap;
         asciiDensityMap = "";

         for (int i = tempMap.length() - 1; i >= 0; i--) {
            asciiDensityMap += tempMap.charAt(i);
         }
      }

      PrintWriter writer = null;
      try {
         if (canOutputFile) {
            String fileName = inputImageFile.substring(0, inputImageFile.lastIndexOf('.'));
            writer = new PrintWriter(fileName + ".txt");
         }
      } catch (FileNotFoundException e) {
         print(RED, "Error with output file. Error message: ");
         e.printStackTrace();
         return;
      }

      BufferedImage image = null;
      try {
         File imageFile = new File(inputImageFile);
         image = ImageIO.read(imageFile.getAbsoluteFile());
      } catch (IOException e) {
         print(RED, "Error reading input image file. Error message: ");
         e.printStackTrace();
         return;
      }

      double imageScale = (double) image.getWidth() / maxWidth;
      int imageWidth = maxWidth;
      int imageHeight = (int) (image.getHeight() / imageScale / 2);
      String[] asciiImage = new String[imageHeight];

      for (int rows = 0; rows < imageHeight; rows++) {
         asciiImage[rows] = "";
         for (int cols = 0; cols < imageWidth; cols++) {
            int X = (int) (cols * imageScale);
            int Y = (int) (rows * imageScale * 2);
            int pixel = image.getRGB(X, Y);
            int alpha = (pixel >> 24) & 0xFF;

            if (alpha == 0) {
               asciiImage[rows] += " ";
               break;
            }

            int red = (pixel >> 16) & 0xFF;
            int green = (pixel >> 8) & 0xFF;
            int blue = pixel & 0xFF;
            int brightness = (red + green + blue) / 3;
            int pixelIndex = (int) Math.floor(brightness * (asciiDensityMap.length() - 1) / 255);
            asciiImage[rows] +=asciiDensityMap.charAt(pixelIndex);
         }
      }

      for (String line : (canCropImage ? cropImage(asciiImage, padding) : asciiImage)) {
         System.out.println(line);
         if (canOutputFile) {
            writer.println(line);
         }
      }

      if (canOutputFile) {
         writer.close();
      }
   }

   public static void printHelpMenu() {
      System.out.println(colour(CYAN, "\t-i, --input")    + "=" + colour(YELLOW, "<file>")   + colour(WHITE, "\tImage file to convert to ASCII. Use 'home/...' for absolute paths. ") + colour(RED, "[Required]"));
      System.out.println(colour(CYAN, "\t-a, --ascii")    + "=" + colour(YELLOW, "\"<map>\"") + colour(WHITE, "\tCustom ASCII density map (must be quoted)."));
      System.out.println(colour(CYAN, "\t-w, --width")    + "=" + colour(YELLOW, "<number>") + colour(WHITE, "\tMax width of the ASCII image."));
      System.out.println(colour(CYAN, "\t-p, --padding")  + "=" + colour(YELLOW, "<number>") + colour(WHITE, "\tPadding around the image when cropping.\n"));
      System.out.println(colour(CYAN, "\t-f, --flip")     + colour(WHITE, "\t\tFlip the ASCII density map."));
      System.out.println(colour(CYAN, "\t-c, --crop")     + colour(WHITE, "\t\tCrop sides if they contain only the same character."));
      System.out.println(colour(CYAN, "\t-o, --output")   + colour(WHITE, "\t\tSave the ASCII art to a file. ") + colour(RED, "Overwrites the existing art unless it is renamed or saved elsewhere."));
   }

   public static boolean endsWithAny(String value, String[] ending) {
      for (String end : ending) {
         if (value.endsWith(end)) {
            return true;
         }
      }
      return false;
   }

   public static boolean startsWithAny(String value, String[] starting) {
      for (String start : starting) {
         if (value.startsWith(start)) {
            return true;
         }
      }
      return false;
   }

   public static boolean equalsAny(String value, String[] options) {
      for (String option : options) {
         if (value.equals(option)) {
            return true;
         }
      }
      return false;
   }

   public static String[] rotateArray(String[] array) {
      String[] rotatedArray = new String[array[0].length()];

      for (int i = 0; i < array[0].length(); i++) {
         rotatedArray[i] = "";
         for (int j = array.length - 1; j >= 0; j--) {
            rotatedArray[i] += array[j].charAt(i);
         }
      }
      return rotatedArray;
   }

   public static String[] cropImage(String[] originalImage, int padding) {
      int repeatingFromTop = 0;
      int repeatingFromBottom = 0;

      for (int i = 0; i < originalImage.length; i++) {
         char firstChar = originalImage[i].charAt(0);
         if (!originalImage[i].replace(firstChar, ' ').trim().isEmpty()) {
            break;
         }
         repeatingFromTop++;
      }

      for (int i = originalImage.length - 1; i >= 0; i--) {
         char firstChar = originalImage[i].charAt(0);
         if (!originalImage[i].replace(firstChar, ' ').trim().isEmpty()) {
            break;
         }
         repeatingFromBottom++;
      }

      repeatingFromTop = Math.max(0, repeatingFromTop - padding);
      repeatingFromBottom = Math.max(0, repeatingFromBottom - padding);

      String[] croppedImage = new String[originalImage.length - repeatingFromTop - repeatingFromBottom];
      for (int i = repeatingFromTop; i < originalImage.length - repeatingFromBottom; i++) {
         int line = i - repeatingFromTop;
         croppedImage[line] = originalImage[i];
      }

      String[] rotatedImage = rotateArray(croppedImage);
      repeatingFromTop = 0;
      repeatingFromBottom = 0;

      for (int i = 0; i < rotatedImage.length; i++) {
         char firstChar = rotatedImage[i].charAt(0);
         if (!rotatedImage[i].replace(firstChar, ' ').trim().isEmpty()) {
            break;
         }
         repeatingFromTop++;
      }

      for (int i = rotatedImage.length - 1; i >= 0; i--) {
         char firstChar = rotatedImage[i].charAt(0);
         if (!rotatedImage[i].replace(firstChar, ' ').trim().isEmpty()) {
            break;
         }
         repeatingFromBottom++;
      }

      repeatingFromTop = Math.max(0, repeatingFromTop - padding);
      repeatingFromBottom = Math.max(0, repeatingFromBottom - padding);

      String[] finalImage = new String[rotatedImage.length - repeatingFromTop - repeatingFromBottom];
      for (int i = repeatingFromTop; i < rotatedImage.length - repeatingFromBottom; i++) {
         int line = i - repeatingFromTop;
         finalImage[line] = rotatedImage[i];
      }

      return rotateArray(rotateArray(rotateArray(finalImage)));
   }
}
