package lassa.net;

public class Nave {

	private int x, y, vida, vidas, nivel;

	public Nave (int x, int y, int vida, int vidas, int nivel) {
		this.x = x;
		this.y = y;
		this.vida = vida;
		this.vidas = vidas;
		this.nivel = nivel;
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

	public int getVida() {
		return vida;
	}

	public void setVida (int vida) {
		this.vida = vida;
	}
	
	public int getVidas() {
		return vidas;
	}

	public void setVidas (int vidas) {
		this.vidas = vidas;
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
