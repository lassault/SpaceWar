package lassa.net;

import java.util.ArrayList;

public class GeneradorDisparos extends Thread {

	private MiPanel mp;
	private ArrayList<Enemigo> enemigos;
	private ArrayList<Disparo> disparosEnemigos;
	
	public GeneradorDisparos (MiPanel mp, ArrayList<Enemigo> enemigos, ArrayList<Disparo> disparosEnemigos) {
		this.mp = mp;
		this.enemigos = enemigos;
		this.disparosEnemigos = disparosEnemigos;
	}
	
	@Override
	public void run() {
		
		while (mp.getJugando()) {
			
			for (int i = enemigos.size() - 1; i > -1; i--) {
				int disparoX = enemigos.get(i).getX() + 17;
				int disparoY = enemigos.get(i).getY() + 40;
				int disparoTipo = enemigos.get(i).getNivel();
				Disparo disparoEnemigo = new Disparo(disparoX, disparoY, disparoTipo);
						
				disparosEnemigos.add(disparoEnemigo);
				
				int nivel = (int) mp.getPuntuacion()/250;
				
				int dormir = (int) (Math.random()*100*(9 - nivel)) + 250;
				
				try {
					Thread.sleep(dormir);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			mp.repaint();
			
		}	
	}
}
