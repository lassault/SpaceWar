package lassa.net;

public class MovimientoNave extends Thread {
	
	private MiPanel mp;
	private Nave nave;
	
	public MovimientoNave (MiPanel mp, Nave nave) {
		this.mp = mp;
		this.nave = nave;
	}
	
	@Override
	public void run() {
		
		while (mp.getJugando()) {
			
			switch ((int) mp.getPuntuacion()/250) {
			case 1: nave.setNivel(2);
					break;
			case 2: nave.setNivel(3);
					break;
			case 3: nave.setNivel(4);
					break;
			case 4: nave.setNivel(5);
					break;
			case 5: nave.setNivel(6);
					break;
			case 6: nave.setNivel(7);
					break;
			case 7: nave.setNivel(8);
					break;
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
