package lassa.net;

import java.util.ArrayList;

public class MovimientoEnemigo extends Thread {

	private MiPanel mp;
	private ArrayList<Enemigo> enemigos = new ArrayList<Enemigo>();
	
	public MovimientoEnemigo (MiPanel mp, ArrayList<Enemigo> enemigos) {
		this.mp = mp;
		this.enemigos = enemigos;
	}
	
	@Override
	public void run() {
		
		while (mp.getJugando()) {
			
			for (int i = enemigos.size() - 1; i > -1; i--) {
				enemigos.get(i).setY(enemigos.get(i).getY() + enemigos.get(i).getNivel());
				if (enemigos.get(i).getY() > 720) {
					enemigos.remove(i);
					int nivel = (int) (Math.random()*5) + 1;
					int x = (int) (Math.random()*1000) + 100;
					int y = 0;
					int vida = (6 - nivel)*100;
					
					Enemigo enemigo = new Enemigo (x, y, vida, nivel);
					enemigos.add(enemigo);
					
				}
			}
			
			mp.repaint();
			
			try {
				Thread.sleep(15);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
