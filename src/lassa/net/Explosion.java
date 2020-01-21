package lassa.net;

public class Explosion {
	
	private int x, y, progreso;

	public Explosion (int x, int y, int progreso) {
		this.x = x;
		this.y = y;
		this.progreso = progreso;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getProgreso() {
		return progreso;
	}

	public void setProgreso(int progreso) {
		this.progreso = progreso;
	}
}
