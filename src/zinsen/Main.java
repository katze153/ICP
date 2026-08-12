package zinsen;

public class Main {
	
	private int days;
	private Double money;
	private Double zins;
	private int methode;
	
	public Double Calc(CLI cli) {
		
		this.cast(cli);
		
		
		
		return switch(methode) {
			case 1 -> money * Math.pow(1 + zins / 100, days);
			case 2 -> money * Math.pow(1 + zins / 100, days / 365.0);
			default -> 0.0;
		};
	}
	
	private void cast(CLI cli) {
		days = (int) cli.days;
		money = (Double) cli.money;
		zins = (Double) cli.zins;
		methode = (int) cli.methode;
	}
}