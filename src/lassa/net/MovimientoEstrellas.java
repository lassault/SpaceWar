package lassa.net;

import java.util.ArrayList;

public class MovimientoEstrellas extends Thread {

	private MiPanel mp;
	private ArrayList<Estrella> estrellas = new ArrayList<Estrella>();
	
	public MovimientoEstrellas (MiPanel mp, ArrayList<Estrella> estrellas) {
		this.mp = mp;
		this.estrellas = estrellas;
	}
	
	@Override
	public void run() {
		
		while (true) {
			
			for (int i = 0; i < estrellas.size(); i++) {
				estrellas.get(i).setY(estrellas.get(i).getY() + estrellas.get(i).getTipo()*2);
				if (estrellas.get(i).getY() > 720) {
					estrellas.remove(i);
					if (estrellas.size() > 500) {
						continue;
					}
					int tipo = (int) (Math.random()*3) + 1;
					int estrellaX = (int) (Math.random()*1300);
					int estrellaY = 0;
					Estrella estrella = new Estrella(estrellaX, estrellaY, tipo);
					estrellas.add(estrella);
				}
			}
		
			mp.repaint();
		
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	
}
