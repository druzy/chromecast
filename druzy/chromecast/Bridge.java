package druzy.chromecast;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Bridge {
	
	public static final String MAIN_PATH="/usr/lib/python-protocoldruzy";
	public static final String DISCOVERY=MAIN_PATH+"/discovery.py";
	public static final String SEND=MAIN_PATH+"/send.py";
	public static final String PLAY=MAIN_PATH+"/play.py";
	public static final String PAUSE=MAIN_PATH+"/pause.py";
	public static final String STOP=MAIN_PATH+"/stop.py";
	public static final String MEDIA_STATUS=MAIN_PATH+"/media_status.py";
	public static final String BRIDGE=MAIN_PATH+"/bridge.py";
	
	private String file=null;
	private String[] args=null;
	private Thread threadExec=null;
	private Scanner scan=null;
	private Scanner scanError=null;
	private PrintStream printer=null;
	
	public Bridge(String file, String... args) {
		this.file=file;
		this.args=args;
	}
	
	public Bridge(String file){
		this(file, new String[]{});
	}
	
	public void exec(final BridgeListener listener){
		threadExec=new Thread(){
			public void run(){
				ArrayList<String> list=new ArrayList<String>();
				list.add("python");
				list.add("-u");
				list.add(file);
				
				for (int i=0;i<args.length;i++) list.add(args[i]);
				ProcessBuilder builder=new ProcessBuilder(list);
				try {
					final Process p=builder.start();
					Thread outThread=new Thread(){
						public void run(){
							scan=new Scanner(p.getInputStream());
							startWorkScan(scan,listener);
							scan.close();
						}
					};
					
					Thread outErrorThread=new Thread(){
						public void run(){
							scanError=new Scanner(p.getErrorStream());
							startWorkScan(scanError,listener);
							scanError.close();
						}
					};
					
					printer=new PrintStream(p.getOutputStream());
					outThread.start();
					outErrorThread.start();
					try {
						outThread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					printer.flush();
					printer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		threadExec.start();
		
	}
	
	public List<HashMap<String,String>> exec(){
		final List<HashMap<String,String>> res=new ArrayList<HashMap<String,String>>();
		exec(new BridgeListener(){

			@Override
			public void newMessage(BridgeEvent event) {
				res.add(event.getMessage());
			}
			
		});
		try {
			threadExec.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return res;
	}

	public void sendMessage(String... message){
		for (int i=0;i<message.length;i++){
			printer.println(message[i]);
		}
		printer.flush();
	}
		
	
	private void startWorkScan(Scanner scan,BridgeListener listener){
		while (scan.hasNext()){
			String test=scan.nextLine();
			int numLine=Integer.parseInt(test);
			HashMap<String,String> map=new HashMap<String,String>();
			for (int i=0;i<numLine;i++){
				String line=scan.nextLine();
				String key=line.substring(0, line.indexOf("="));
				String value=line.substring(line.indexOf("=")+1);
				map.put(key, value);
			}
			listener.newMessage(new BridgeEvent(this,map));
		}
	}
}
