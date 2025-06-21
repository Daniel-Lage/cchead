import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import java.util.Scanner;
import java.util.ArrayList;

class Main {
    static final String UTF8_BOM = "\uFEFF";

    static Integer size = 10;
    static char unit = 'n';
    static ArrayList<String> file_names = new ArrayList<String>();

    static Scanner scanner;

    public static void main(String[] args) {
        setUp(args);
        // updates values for size unit and files based on args

        int file_count = file_names.size();

        if (file_count == 0) {
            if (unit == 'n')
                readLines(System.in);
            else
                readBytes(System.in);
        } else {
            for (int i = 0; i < file_count; i++) {
                String file_name = file_names.get(i);

                if (i > 0)
                    System.out.println();

                if (file_count > 1)
                    System.out.println("==> " + file_name + " <==");

                try {
                    FileInputStream file_in = new FileInputStream(file_name);

                    if (unit == 'n')
                        readLines(file_in);
                    else
                        readBytes(file_in);

                    file_in.close();
                } catch (FileNotFoundException e) {
                    System.err.printf("Error: could not find file at '%s'\n");
                    System.exit(1);
                } catch (IOException e) {
                    System.err.printf("Error: could not read file at '%s'\n");
                    System.exit(1);
                }
            }
        }
    }

    static void setUp(String[] args) {
        // arguments are either:
        // -<unit><size> or -<unit> <size> for size and unit
        // (unit is either 'c' for bytes or 'n' for lines)
        // <file> for file name

        boolean hasSize = false;
        boolean hasUnit = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("-")) { // flag arg
                unit = arg.charAt(1);
                if (unit != 'c' && unit != 'n') {
                    System.err.printf("Error: invalid flag arg '%s'\n", arg);
                    System.exit(1);
                }

                if (hasUnit) {
                    System.err.println("Error: multiple unit args");
                    System.exit(1);
                }
                hasUnit = true;

                if (arg.length() > 2) // if the arg includes size
                    arg = arg.substring(2);
                else { // if the arg is just the unit
                    i++;
                    arg = args[i];
                }

                try {
                    int sizeArg = Integer.parseInt(arg);

                    if (hasSize) {
                        System.err.println("Error: multiple size args");
                        System.exit(1);
                    }
                    hasSize = true;

                    size = sizeArg;
                } catch (NumberFormatException e) {
                    System.err.printf("Error: invalid size arg '%s'\n", arg);
                    System.exit(1);
                }
            } else {
                File file = new File(arg);
                if (!file.exists() || !file.isFile() || !file.canRead()) {
                    System.err.printf("Error: invalid file arg '%s'\n", arg);
                    System.exit(1);
                }
                file_names.add(arg);
            }
        }
    }

    static void readLines(InputStream input) {
        scanner = new Scanner(input);

        for (int i = 0; i < size; i++) {
            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (i == 0 && line.startsWith(UTF8_BOM))
                    line = line.substring(1);

                System.out.println(line);
            } else
                break;
        }

        scanner.close();
    }

    static void readBytes(InputStream input) {
        byte[] buffer = new byte[size];

        int progress = 0;
        while (progress != size) {
            int result;

            try {
                result = input.read(buffer, progress, size - progress);
            } catch (IOException e) {
                System.err.println("Error: could not read bytes from input stream");
                System.exit(1);
                return; // unreachable but required to compile
            }

            if (result == -1)
                break; // end of input

            progress += result;
        }

        String text = new String(buffer, java.nio.charset.StandardCharsets.UTF_8);

        if (text.startsWith(UTF8_BOM))
            text = text.substring(1);

        System.out.println(text);
    }
}