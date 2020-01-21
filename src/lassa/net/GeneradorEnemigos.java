package lassa.net;

import java.util.ArrayList;

public class GeneradorEnemigos extends Thread {

	private MiPanel mp;
	private ArrayList<Enemigo> enemigos = new ArrayList<Enemigo>();
	
	public GeneradorEnemigos (MiPanel mp, ArrayList<Enemigo> enemigos) {
		this.mp = mp;
		this.enemigos = enemigos;
	}
	
	@Override
	public void run() {
		
		while (mp.getJugando()) {
			
			if (enemigos.size() > 10) {
				continue;
			}
			
			int enemigoX = (int) (Math.random()*1000) + 100;
			int enemigoY = 0;
			int nivel = (int) (Math.random()*5) + 1;
			int vida = (6 - nivel)*100;
			
			Enemigo enemigo = new Enemigo (enemigoX, enemigoY, vida, nivel);
			
			enemigos.add(enemigo);
			
			mp.repaint();
			
			int dormir = (int) (Math.random()*1000) + 500;
			
			try {
				Thread.sleep(dormir);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}	
	}
}
