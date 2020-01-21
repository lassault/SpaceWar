package lassa.net;

import java.util.ArrayList;

public class MovimientoColisiones extends Thread {

	private MiPanel mp;
	private Nave nave;
	private ArrayList<Enemigo> enemigos = new ArrayList<Enemigo>();
	private ArrayList<Disparo> disparos = new ArrayList<Disparo>();
	private ArrayList<Disparo> disparosEnemigos = new ArrayList<Disparo>();
	private ArrayList<Explosion> explosiones = new ArrayList<Explosion>();
	
	public MovimientoColisiones (MiPanel mp, Nave nave, ArrayList<Enemigo> enemigos, ArrayList<Disparo> disparos, ArrayList<Disparo> disparosEnemigos, ArrayList<Explosion> explosiones) {
		this.mp = mp;
		this.nave = nave;
		this.enemigos = enemigos;
		this.disparos = disparos;
		this.disparosEnemigos = disparosEnemigos;
		this.explosiones = explosiones;
	}
	
	@Override
	public void run() {
		
		while (mp.getJugando()) {
			
				int naveX = nave.getX();
				int naveY = nave.getY();
				
				for (int i = enemigos.size() - 1; i > -1; i--) {
					int enemigoX = enemigos.get(i).getX();
					int enemigoY = enemigos.get(i).getY();
					
					for (int j = disparos.size() - 1; j > -1; j--) {
						int disparoX = disparos.get(j).getX();
						int disparoY = disparos.get(j).getY();
						
						if (((disparoX > enemigoX) && (disparoX < (enemigoX + 60))) || ((disparoX + 26) > enemigoX) && ((disparoX + 26) < (enemigoX + 60))) {
							if ((disparoY - 12) < (enemigoY + 30)) {
								disparos.remove(j);
								
								enemigos.get(i).setVida(enemigos.get(i).getVida() - nave.getNivel()*75);
								if (enemigos.get(i).getVida() <= 0) {
									mp.setPuntuacion(mp.getPuntuacion() + enemigos.get(i).getNivel()*10);
									Explosion explosion = new Explosion (enemigos.get(i).getX() - 16, enemigos.get(i).getY() - 14, 1);
									explosiones.add(explosion);
									enemigos.remove(i);
									
									if (enemigos.size() < 5) {
										int nuevoEnemigoX = (int) (Math.random()*1000) + 100;
										int nuevoEnemigoY = 0;
										int nivel = (int) (Math.random()*5) + 1;
										int vida = (6 - nivel)*100;
										
										Enemigo enemigo = new Enemigo (nuevoEnemigoX, nuevoEnemigoY, vida, nivel);
										
										enemigos.add(enemigo);
									}
									
								}
								
								mp.repaint();
								
								break;
							}
						}
					}
				}

				for (int i = disparosEnemigos.size() - 1; i > -1; i--) {
					int disparoEnemigoX = disparosEnemigos.get(i).getX();
					int disparoEnemigoY = disparosEnemigos.get(i).getY();
					
					if (((disparoEnemigoX > naveX) && (disparoEnemigoX < (naveX + 60))) || ((disparoEnemigoX + 26) > naveX) && ((disparoEnemigoX + 26) < (naveX + 60))) {
						if ((disparoEnemigoY - 12) > (naveY + 30)) {
							nave.setVida(nave.getVida() - disparosEnemigos.get(i).getTipo()*5);
							disparosEnemigos.remove(i);
							if (nave.getVida() <= 0) {
								nave.setVida(100);
								nave.setVidas(nave.getVidas() - 1);
								if (nave.getVidas() == 0) {
									mp.setJugando(false);
									mp.setMuerto(true);
								}
							}
							
							mp.repaint();
							break;
						}
					}
					
				}
				
				
			
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
}
