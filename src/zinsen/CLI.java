package zinsen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

import com.dropbox.core.DbxApiException;
import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.TokenAccessType;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.oauth.DbxRefreshResult;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;
import com.ktf.config.Config;

public class CLI {
	
	Config set = new Config("set.toml");
    static boolean[] Args = new boolean[9];
   

    public static void main(String[] args) {

       /* if (args.length == 0) {
            System.out.println("Array ist leer");
        } else {
            System.out.println("Array length: " + args.length);
        }

        for (String arg : args) {
            switch (arg) {
                case "-h"  -> Args[0] = true;
                case "-dh" -> Args[1] = true;
                case "-g"  -> Args[2] = true;
                case "-da" -> Args[3] = true;
                case "-z"  -> Args[4] = true;
                case "-b"  -> Args[5] = true;
                case "-t"  -> Args[6] = true;
                case "-bs" -> Args[7] = true;
                case "-a"  -> Args[8] = true;
            }
        }

        for (boolean arg : Args) {
            System.out.println(arg);
        }*/
        
        CLI cli = new CLI();
        cli.menu(cli);
    }

	public Object days;
	public Object money;
	public Object zins;
	public Object methode;
	private Object Save = false;
	public Object name;
	private String date;
	Boolean cloud = false;
	
	DbxClientV2 client;
    
    private void menu(CLI cli) {
    	
    	
    	
    	try {
    	cloud = switch (set.getConfig("refreshToken")) {
    		case null -> false;
    		case "" -> false;
    		default -> true;
    	};
    	
    	
    	
    	if (cloud) {
        	refresh();
    		downloadSave();
    	}
    	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
		Config conf = new Config("./save.toml");
    	
    	clearConsole();
        System.out.println(" #################################");
        System.out.println(" # Zinsenberechnungs Programm    #");
        System.out.println(" # \u00A9Fabian       (v3.0)          #");
        System.out.println(" #################################");
        /*
        if (Args[0]) {
        	String data = """
        			            		
                    -h          help
                    -dh         date today      (DD.MM.YYYY)
                    -g          money           (z.b. 50,45)
                    -da         Abschluss       (DD.MM.YYYY)
                    -z          Zinsen          (z.b. 5,5 for 5.5%)
                    -b          begin Z-Z       (DD.MM.YYYY)
                    -t          day or year     (d for daily, y for yearly)
                    -bs         skip Z-Z
                    -a          Use System date
        			""";
        	System.out.println(data);
        	System.exit(0);
        }*/
        
        //menu
        Scanner scanner = new Scanner(System.in);
        
        String data = """
        		
        		1. Berechne Zinsen
        		2. Erstelle Auftrag
        		3. Lese Auftraege
        		4. Sync mit Cloud
        		
        		
        		-> """;
        
        
        System.out.print(data);
        int zahl = scanner.nextInt();  
        
        
        if (zahl == 4){
        	//try {
        	//downloadSave();
        	//}
        	//catch (Exception e) {
        	//	e.printStackTrace();
        	//}
        	setupDropbox(scanner);
        	
        	
        	cli.Save = true;
        	
        
        }else if (zahl == 3) {
        	
        	
        	
        	clearConsole();
        	System.out.println("	Auftraege\n");
        	
        	int ii = 1;
        	
        	String[] Names = conf.getAllSections();
        	
        	if (Names.length == 0) {
        		System.err.println("WARN: Keine Auftraege");
        		return;
        	}
        	
        	for (String SecNames: Names) {
        		System.out.println(ii+". "+ SecNames);
        		ii++;
        	}
        	System.out.print("\n-> ");
        	date = Names[scanner.nextInt() - 1];
        	
        	
        
        	cli.days = dayCalcLite(conf.getValueFromSection("date", date));
        	cli.money = Double.parseDouble(conf.getValueFromSection("money", date));
        	cli.zins = Double.parseDouble(conf.getValueFromSection("zins", date));
        	cli.methode = Integer.parseInt(conf.getValueFromSection("methode", date));
        	Main main = new Main();
			Double res = main.Calc(cli);
			
			String Methode = switch((int)methode) {
				case 1 -> "Taeglich";
				case 2 -> "Jaehrlich";
				default -> "err";
			};
			
			clearConsole();
			
			System.out.println(boxLine("##############################################"));
			System.out.println(boxLine(""));
			System.out.println(boxLine("     " + date));
			System.out.println(boxLine(""));
			System.out.println(boxLine(" Geld: " + String.format("%.2f \u20AC", (Double) cli.money)));
			System.out.println(boxLine(" Tage: " + (Integer) cli.days));
			System.out.println(boxLine(" Zinssatz: " + String.format("%.2f %%", (Double) cli.zins)));
			System.out.println(boxLine(" Methode: " + Methode));
			System.out.println(boxLine(""));
			System.out.println(boxLine("----------------------------------------------"));
			System.out.println(boxLine(""));
			System.out.println(boxLine(" Zinsen: " + String.format("%.2f \u20AC", res - (Double) cli.money)));
			System.out.println(boxLine(" End-Schulden: " + String.format("%.2f \u20AC", res)));
			System.out.println(boxLine("##############################################"));
			
			
			System.out.println("\nZum Löschen: schreibe 'del' und drücke Enter\n");
			scanner.nextLine();
			data = scanner.nextLine();  
			
			cli.Save = true;
			if (data.equals("del")) {
				
				conf.deleteSection(date);
				
				uploadSave();
				clearConsole();
				System.out.println("Auftrag "+date+" wurde gelöscht !");
			}
        	
        } else {
        	
        	if (zahl == 2) cli.Save = true;
        	
        	//String[] Dat = new String[8];
			if (!(boolean)cli.Save) {
        	
            data = """
            			
            			Methode
            		
            		1. Anzahl der Tage
            		2. Datum Anfang -> Ende
            		
            		
            		-> """;
            clearConsole();
            System.out.print(data);
            zahl = scanner.nextInt();  
            
            data = """
        			
        			Anzahl der Tage
        		
        		
        		-> """;
            

			}
            clearConsole();
            if (zahl==1) System.out.print(data);
            int zahl_1=1;
            if (zahl==1) zahl_1 = scanner.nextInt(); 
            scanner.nextLine();
            
            
            if (!(boolean)cli.Save) zahl = 2;
            cli.days = switch(zahl) {
            	case 2 -> this.Day_calc(scanner,cli);
            	default -> zahl_1;
            };
            
            data = """
        			
        			Schulden
        		
        		
        		-> """;
            
            clearConsole();
            System.out.print(data);
			cli.money = scanner.nextDouble();
			
			data = """
					
					Zinsen in Prozent (2.3% -> 2.3)
            
				-> """;
			
			clearConsole();
			System.out.print(data);
			cli.zins = scanner.nextDouble();
			

			data = """
					
					Art der Berechnung
				
					1. Taeglich
					2. Jaehrlich
				
				
				-> """;
			
			clearConsole();
			System.out.print(data);
			cli.methode = scanner.nextInt();
			
			if (!(boolean)cli.Save) {		
			Main main = new Main();
			Double res = main.Calc(cli);
			

			
			String Methode = switch((int)methode) {
				case 1 -> "Taeglich";
				case 2 -> "Jaehrlich";
				default -> "err";
			};
			
			data = """
					
					=======================================
					
						Ergebniss
					
					Eingaben:
					
					Geld: %.2f \u20AC
					Tage: %d
					Zinssatz: %.2f %%
					Methode: %s
					
					---------------------------------------
					
					Zinsen: %.2f \u20AC
					End-Schulden: %.2f \u20AC%n
					=======================================
					""".formatted(cli.money, cli.days, cli.zins, Methode, res - (Double)cli.money, res);
			
			clearConsole();
			System.out.print(data);
			
			} else if ((boolean)cli.Save) {
				
	            data = """
	        			
	        			Name:
	        		
	        		
	        		-> """;
				clearConsole();
				System.out.print(data);
				scanner.nextLine(); 
				cli.name = scanner.nextLine();
				

				conf.createSection((String)cli.name);
				
				
				conf.addKeyToSection("money",Double.toString((Double)cli.money), (String)cli.name);
				conf.addKeyToSection("zins", Double.toString((Double)cli.zins), (String)cli.name);
				conf.addKeyToSection("methode", Integer.toString((int)cli.methode), (String)cli.name);
				conf.addKeyToSection("date", (String)cli.date, (String)cli.name);
				
				clearConsole();
				
				File file = new File("./save.toml");
				if (file.exists()) {
					System.out.println("\nAuftrag gespeichert unter "+file.getAbsolutePath()+"\n");
				} else System.err.println("ERROR: Datei konnte nicht gespeichert werden");
				
				if (cloud) uploadSave();
				
			} else System.err.println("ERROR: No Methode");
			
			
        }
        
		if (!(boolean)cli.Save) scanner.nextLine(); 
		System.out.println("Druecke Enter zum Fortfahren...");
		scanner.nextLine();
		
		scanner.close();
    }
    
    private int Day_calc(Scanner scanner, CLI cli) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        System.out.print("\nDatum des Abschlusses:\n\n-> ");
        
        cli.date=scanner.nextLine();
        
        if ((boolean)cli.Save) return 0;
        LocalDate datum1 = LocalDate.parse((String)cli.date, format);

        
        String data = """
    			
    			Datum des Heutigen Tages
    		
    		1. System
    		2. Manuell
    		
    		
    		-> """;
        System.out.print(data);
        String tmp = scanner.nextLine();
        
        LocalDate datum2;
        if (Integer.parseInt(tmp) == 2) {
            System.out.print("\nDatum des Heutigen Tages:\n\n-> ");
            datum2 = LocalDate.parse(scanner.nextLine(), format);
        } else {
        	datum2 = LocalDate.now();
        }
        

        long tage = ChronoUnit.DAYS.between(datum1, datum2);

        //System.out.println("Differenz: " + tage + " Tage");

        return (int) tage;
    }
    
    private int dayCalcLite(String datum) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        
        LocalDate datum1 = LocalDate.parse(datum, format);
        LocalDate datum2 = LocalDate.now();
        

        long tage = ChronoUnit.DAYS.between(datum1, datum2);


        return (int) tage;
    }
    
    public void clearConsole() {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls")
                        .inheritIO()
                        .start()
                        .waitFor();
            } else {
                System.out.print("\033[2J\033[H");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println("\n".repeat(50));
        }
    }
    
    private static final int BOX_WIDTH = 48;

    private static String boxLine(String text) {
        String content = text;
        
        if (content.length() > BOX_WIDTH - 4) {
            content = content.substring(0, BOX_WIDTH - 4);
        }
        
        String padded = String.format("%-" + (BOX_WIDTH - 4) + "s", content);
        return "# " + padded + " #";
    }
    
    private void uploadSave() {
    	
    	//DbxRequestConfig config =
        //        DbxRequestConfig.newBuilder("Zinsen").build();

        //client =
        //        new DbxClientV2(config, set.getConfig("ACCESS_TOKEN"));

        try (InputStream in = new FileInputStream("save.toml")) {

            client.files().uploadBuilder("/Zinsen/save.toml")
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(in);

            System.out.println("\nsave.toml wurde hochgeladen!\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    	
    }
    
    
    private void downloadSave() {
    	
        //DbxRequestConfig config =
        //        DbxRequestConfig.newBuilder("Zinsen").build();

        //DbxClientV2 client =
        //       new DbxClientV2(config, set.getConfig("ACCESS_TOKEN"));

        try (OutputStream out = new FileOutputStream("save.toml")) {

            client.files()
                    .download("/Zinsen/save.toml")
                    .download(out);

            System.out.println("save.toml wurde heruntergeladen!\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    private void setupDropbox(Scanner scanner) {
    	
		String data = """
				
				APP_KEY
        
			-> """;
		
		clearConsole();
		System.out.print(data);
		
		scanner.nextLine();
		String APP_KEY = scanner.nextLine();
		set.setConfig("APP_KEY", APP_KEY);
		
		data = """
				
				APP_SECRET
        
			-> """;
		
		clearConsole();
		System.out.print(data);
		
		String APP_SECRET = scanner.nextLine();
		set.setConfig("APP_SECRET", APP_SECRET);
    	
    	DbxRequestConfig config =
    	        DbxRequestConfig.newBuilder("Zinsen").build();

    	DbxAppInfo appInfo = new DbxAppInfo(
    	        APP_KEY,
    	        APP_SECRET
    	);

    	DbxWebAuth webAuth = new DbxWebAuth(config, appInfo);

    	DbxWebAuth.Request request =
    	        DbxWebAuth.newRequestBuilder()
    	                .withTokenAccessType(TokenAccessType.OFFLINE)
    	                .build();

    	String url = webAuth.authorize(request);
    	
    	
		data = """
				
				URL: %s
				
				Code
        
			-> """.formatted(url);
		
		clearConsole();
		System.out.print(data);
		//scanner.nextLine();
		String Code = scanner.nextLine();
		
		DbxAuthFinish authFinish;
		try {
		authFinish =
		        webAuth.finishFromCode(Code);
		} catch (Exception e){
			e.printStackTrace();
			return;
		}
		
		
		set.setConfig("refreshToken",  authFinish.getRefreshToken());
		set.setConfig("accessToken",  authFinish.getAccessToken());
		set.setConfig("expiresAt",  authFinish.getExpiresAt().toString());
		
		clearConsole();
		System.out.println("refreshToken saved !");
    }
    
    private void refresh() {

        DbxRequestConfig config =
                DbxRequestConfig.newBuilder("Zinsen").build();

        DbxCredential credential = new DbxCredential(
                set.getConfig("accessToken"),
                Long.parseLong(set.getConfig("expiresAt")),
                set.getConfig("refreshToken"),
                set.getConfig("APP_KEY"),
                set.getConfig("APP_SECRET")
        );

        try {
            DbxRefreshResult result = credential.refresh(config);

            String accessToken = result.getAccessToken();
            Long expiresAt = result.getExpiresAt();

            set.setConfig("accessToken", accessToken);
            set.setConfig("expiresAt", expiresAt.toString());

            credential = new DbxCredential(
                    accessToken,
                    expiresAt,
                    set.getConfig("refreshToken"),
                    set.getConfig("APP_KEY"),
                    set.getConfig("APP_SECRET")
            );

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        client = new DbxClientV2(config, credential);
    }
}