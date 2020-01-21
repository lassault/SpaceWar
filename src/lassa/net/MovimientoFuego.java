package lassa.net;

import java.util.ArrayList;

public class MovimientoFuego extends Thread {

	private MiPanel mp;
	private ArrayList<Explosion> explosiones = new ArrayList<Explosion>();
	
	public MovimientoFuego (MiPanel mp, ArrayList<Explosion> explosiones) {
		this.mp = mp;
		this.explosiones = explosiones;
	}
	
	@Override
	public void run() {
		
		while (mp.getJugando()) {
			
			for (int i = explosiones.size() - 1; i > -1; i--) {
				explosiones.get(i).setProgreso(explosiones.get(i).getProgreso() + 1);
				
				if (explosiones.get(i).getProgreso() == 8) {
					explosiones.remove(i);
				}
			}
			
			mp.setNumeroFuego(mp.getNumeroFuego() + 1);
			
			if (mp.getNumeroFuego() == 8) {
				mp.setNumeroFuego(1);
			}
			
			mp.repaint();
			
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}	
	}
}
