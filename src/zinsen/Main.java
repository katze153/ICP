package zinsen;
import com.ktf.dy.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.ktf.scanner.*;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class Main {
    static ArrayList<String> arg = new ArrayList<>();

    private static boolean isSchaltjahr(int jahr) {
        return (jahr % 4 == 0 && jahr % 100 != 0) || (jahr % 400 == 0);
    }

    public static void main(String[] args) {
        // Initialize arguments list
        while (arg.size() <= 9) {
            arg.add(null);
        }

        // Parse command line arguments
        for (int ii = 0; ii < args.length; ii++) {
            String tmp = args[ii];
            
            if (tmp.equals("-dh") && ii + 1 < args.length) {
                arg.set(1, args[ii + 1]);
            }
            if (tmp.equals("-g") && ii + 1 < args.length) {
                arg.set(2, args[ii + 1].replace(',', '.'));  // Replace comma with dot for decimal parsing
            }
            if (tmp.equals("-da") && ii + 1 < args.length) {
                arg.set(3, args[ii + 1]);
            }
            if (tmp.equals("-z") && ii + 1 < args.length) {
                arg.set(4, args[ii + 1].replace(',', '.'));  // Replace comma with dot for decimal parsing
            }
            if (tmp.equals("-b") && ii + 1 < args.length) {
                arg.set(5, args[ii + 1]);
            }
            if (tmp.equals("-bs")) {
                arg.set(8, "true");
            }
            if (tmp.equals("-h")) {
                arg.set(6, "true");
            }
            if (tmp.equals("-a")) {
                arg.set(9, "true");
            }
            if (tmp.equals("-t") && ii + 1 < args.length) {
                arg.set(7, args[ii + 1]);
            }
        }

        // Show help if requested
        if (arg.get(6) != null && arg.get(6).equals("true")) {
            String tmpp = """
            		
                    -h          help
                    -dh         date today      (DD.MM.YYYY)
                    -g          money           (z.b. 50,45)
                    -da         Abschluss       (DD.MM.YYYY)
                    -z          Zinsen          (z.b. 5,5 for 5.5%)
                    -b          begin Z-Z       (DD.MM.YYYY)
                    -t          day or year     (d for daily, y for yearly)
                    -bs         skip Z-Z
                    -a			Use System date
                    """;
            System.out.println(tmpp);
            System.exit(0);
        }

        // Initialize variables
        String data_heute;
        String data_abschluss;
        double Zinsen = 0;
        String begin_zinsezinsens;
        double geld;

        System.out.println("#################################");
        System.out.println("# Zinsenberechnungs Programm    #");
        System.out.println("# ©Fabian       (v2.3)          #");
        System.out.println("#################################");

        // Get input (either from args or interactive)
        if (arg.get(1) == null && arg.get(9) == null) {
            data_heute = Input.input("\nDatum von Heute (DD.MM.YYYY): ", false);
        }
        else if (arg.get(9) != null && arg.get(9).equals("true")) {
        	//System.out.println(Main.Data_now());
        	data_heute = Main.Data_now();
        }
        else {
            data_heute = arg.get(1);
        }

        if (arg.get(2) == null) {
            geld = Double.parseDouble(Input.input("Schulden (z.B. 50,45): ", false).replace(',', '.'));
        } else {
            geld = Double.parseDouble(arg.get(2));
        }

        if (arg.get(3) == null) {
            data_abschluss = Input.input("Datum des Abschlusses (DD.MM.YYYY): ", false);
        } else {
            data_abschluss = arg.get(3);
        }

        if (arg.get(4) == null) {
            Zinsen = Double.parseDouble(Input.input("Zinsen (z.B. 5,5 für 5.5%): ", false).replace(',', '.'));
        } else {
            Zinsen = Double.parseDouble(arg.get(4));
        }

        if (arg.get(5) == null && !"true".equals(arg.get(8))) {
            begin_zinsezinsens = Input.input("Beginn Zinseszinsen (DD.MM.YYYY): ", true);
        } else if (arg.get(5) == null && "true".equals(arg.get(8))) {
            begin_zinsezinsens = "1.1.2000";
        } else {
            begin_zinsezinsens = arg.get(5);
        }

        // Parse dates
        String[] parts_h = data_heute.split("\\.");
        String[] parts_a = data_abschluss.split("\\.");
        String[] parts_z = begin_zinsezinsens.split("\\.");

        // Calculate leap years
        int i = Integer.parseInt(parts_h[2]);
        int sj = 0;
        while (i < Integer.parseInt(parts_a[2])) {
            if (isSchaltjahr(i)) sj++;
            i++;
        }
        if (isSchaltjahr(Integer.parseInt(parts_h[2]))) sj--;

        i = Integer.parseInt(parts_z[2]);
        int sj_z = 0;
        while (i < Integer.parseInt(parts_a[2])) {
            if (isSchaltjahr(i)) sj_z++;
            i++;
        }
        if (isSchaltjahr(Integer.parseInt(parts_z[2]))) sj_z--;

        // Calculate days between dates
        int h = dy.Generator(data_heute) + Integer.parseInt(parts_h[2]) * 365;
        int a = dy.Generator(data_abschluss) + Integer.parseInt(parts_a[2]) * 365;
        int z = dy.Generator(begin_zinsezinsens) + Integer.parseInt(parts_z[2]) * 365;

        int end = sj + (h - a);
        int end_z = sj_z + (z - a);

        System.out.println("Tage zwischen den Zeiten: " + end + " Tage");

        // Format for currency output
        DecimalFormat df = new DecimalFormat("#,##0.00");

        // Calculate interest
        if (arg.get(7) != null && arg.get(7).equals("d")) {
            if (end_z <= end && !"true".equals(arg.get(8))) {
                // Daily compound interest
                double end_kn = geld * Math.pow(1 + Zinsen / 100.0, end);
                System.out.printf("Endbetrag (tägliche Zinseszinsen): %s€%n", df.format(end_kn));
            } else {
                // Simple daily interest
                double end_easy = geld * (Zinsen / 100.0) * end;
                System.out.printf("Endbetrag (tägliche einfache Zinsen): %s€%n", df.format(geld + end_easy));
            }
        } else {
            if (end_z <= end && !"true".equals(arg.get(8))) {
                // Yearly compound interest
                double end_kn = geld * Math.pow(1 + Zinsen / 100.0, end / 365.0);
                System.out.printf("Endbetrag (jährliche Zinseszinsen): %s€%n", df.format(end_kn));
            } else {
                // Simple yearly interest
                double end_easy = geld * (Zinsen / 100.0) * (end / 365.0);
                System.out.printf("Endbetrag (jährliche einfache Zinsen): %s€%n", df.format(geld + end_easy));
            }
        }
    }
    private static String Data_now() {
        LocalDateTime jetzt = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String formatiert = jetzt.format(formatter);
        return formatiert;
    }
}