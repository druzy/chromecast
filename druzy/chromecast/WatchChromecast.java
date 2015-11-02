package druzy.chromecast;

public class WatchChromecast extends Thread{

	private Bridge bridge=null;
	private int delay=0;
	
	public WatchChromecast(Bridge bridge){
		this(bridge,1000);
	}
	
	public WatchChromecast(Bridge bridge, int delay){
		super();
		
		this.bridge=bridge;
		this.delay=delay;
	}
	
	@Override
	public void run(){
		super.run();
		while (!isInterrupted()){
			bridge.sendMessage("media_status");
			
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				this.interrupt();
			}
		}
	}
}
