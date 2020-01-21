package lassa.net;

public class Disparo {

	private int x, y, tipo;
	
	public Disparo (int x, int y, int tipo) {
		this.x = x;
		this.y = y;
		this.tipo = tipo;
	}
	
	public int getX() {
		return x;
	}

	public void setX (int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY (int y) {
		this.y = y;
	}
	
	public int getTipo() {
		return tipo;
	}
	
	public void setTipo (int tipo) {
		this.tipo = tipo;
	}
	
}
