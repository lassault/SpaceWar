package lassa.net;

public class Enemigo {

	private int x, y, vida, nivel;
			
	public Enemigo (int x, int y, int vida, int nivel) {
		this.x = x;
		this.y = y;
		this.vida = vida;
		this.nivel = nivel;
	}
	
	public int getVida() {
		return vida;
	}

	public void setVida (int vida) {
		this.vida = vida;
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

	public int getNivel() {
		return nivel;
	}

	public void setNivel (int nivel) {
		this.nivel = nivel;
	}
	
}
