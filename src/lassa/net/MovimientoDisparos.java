package lassa.net;

import java.util.ArrayList;

public class MovimientoDisparos extends Thread {

		private MiPanel mp;
		private ArrayList<Disparo> disparos = new ArrayList<Disparo>();
		private ArrayList<Disparo> disparosEnemigos = new ArrayList<Disparo>();
		
		public MovimientoDisparos(MiPanel mp, ArrayList<Disparo> disparos, ArrayList<Disparo> disparosEnemigos) {
			this.mp = mp;
			this.disparos = disparos;
			this.disparosEnemigos = disparosEnemigos;
		}
		
		@Override
		public void run() {
			
			while (mp.getJugando()) {
				
				for (int i = disparos.size() - 1; i > -1; i--) {
					disparos.get(i).setY(disparos.get(i).getY() - 10);
					if (disparos.get(i).getY() < 0) {
						disparos.remove(i);
					}
				}
				
				for (int i = disparosEnemigos.size() - 1; i > -1; i--) {
					disparosEnemigos.get(i).setY(disparosEnemigos.get(i).getY() + 10);
					if (disparosEnemigos.get(i).getY() > 720) {
						disparosEnemigos.remove(i);
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
