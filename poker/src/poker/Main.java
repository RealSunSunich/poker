package poker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;

import java.util.*;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

class Player{
	public String ip;
	public int num;
	int[] cards=new int[2];
	int counter=0;

	public Player(String ip, int num) {
		this.ip = ip;
		// TODO Auto-generated constructor stub
		this.num = num;
	}

	public void giveCards(int i) {
		System.out.println(i);
		
		if(this.counter>=2)
			return;
		this.cards[this.counter]=i;
		this.counter++;
		
	}
}


public class Main extends Application{
	
		
	static class Options{
		@Parameter(names = "-server", description = "Default - server mode, false if client", arity = 1)
		static boolean type = true;	
	}
	
	
	class CycleIterator<T> implements Iterator<Player>{

		private List<Player> list;
		private Iterator<Player> iter;

		public CycleIterator(List<Player> list) {
			this.list = list;
			this.iter = this.list.iterator();
		}
		
		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public Player next() {
			if(iter.hasNext())
				return iter.next();
			else{
				iter=list.iterator();
				return iter.next();
			}
		}

		
	}
	
	public static void main(String[] args) {
		
		Options options = new Options();
		new JCommander(options, args);
		System.out.println(options.type);
		launch(args);

	}

	private static int counter;
	private static FlowPane pane;
	private static Scene ms;
	private static volatile GraphicsContext gc;
	private static volatile Stage primaryStage;
	private static Image img;
	private static int[] cardsIdxs = new int[52];
	private static List<Player> players;
	private static int cardHead;;

	private static void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
	
	
	@Override
	public void start(Stage stage) throws Exception {
		
		primaryStage = stage;
		System.out.println(Options.type);
		
		
		
		
		for(int i=0; i<=cardsIdxs.length-1;i++){
			cardsIdxs[i]=i;
		}
	
		
//		WritableImage[] cardsImg = new WritableImage[52];
		
		Random random = new Random();
		
		for (int i = cardsIdxs.length; i > 1; i--) {
            swap(cardsIdxs, i - 1, random.nextInt(i));
        }
		
		File cardsImgFile = new File("res/cards.jpg");
		
		img = new Image(new FileInputStream(cardsImgFile));
		
				
		primaryStage.setTitle("Best Super Mega Poker");
		
		pane = new FlowPane();
		
		ms = new Scene(pane,1024,768);
		
		primaryStage.setScene(ms);
		
		Canvas cnvs = new Canvas(800,600);
		gc = cnvs.getGraphicsContext2D();
		Button check = new Button("Check");
		Button call = new Button("Call");
		Button raise = new Button("Raise");
		VBox vbox = new VBox();
	    vbox.setPadding(new Insets(10));
	    vbox.setSpacing(8);
	    vbox.getChildren().addAll(check, call, raise);
		pane.getChildren().add(cnvs);
		pane.getChildren().addAll(vbox);
		
		
		
		
		players = new ArrayList<>(3);
		
		CycleIterator<Object> player = new CycleIterator<Object>(players);
		
		if(Options.type){
			//Create Server
			
			Runnable r = new Runnable() {
				
				

				@Override
				public void run() {
					Main.createServer();	
//					return null;
				}

			};
			
			Thread thread = new Thread(r, "Poker Server");
			thread.start();
			
		}
		else{
			//create Client 
			
			
			try {
				SocketAddress address = new InetSocketAddress("127.0.0.1", 777);
				SocketChannel client = SocketChannel.open(address);
				ByteBuffer buffer = ByteBuffer.allocate(72);
				WritableByteChannel out = Channels.newChannel(System.out);
				int me=-1;
				while (client.read(buffer) != -1) {
					System.out.println(new String(buffer.array()));
					
					buffer.flip();
					if(me==-1){
//						String string = new String(buffer.array());
//						System.out.println(string);
//						me=Integer.valueOf(string.substring(string.lastIndexOf(" ")+1).trim());
						
						me=buffer.getInt();
						System.out.println("I am number " + me);
					}
					else{
						drawCards(img, gc, buffer.getInt(), 0);
						drawCards(img, gc, buffer.getInt(), 0);
//						counter++;
						primaryStage.show();
						break;
					}
					
					
					out.write(buffer);
					buffer.clear();
					
					
					
				}
				
				
				
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			
		}
		
		
		
		
		
		/*for(int i=0; i<=(2*players.size()-1); i++){
			int nextInt = cardsIdx[i];
			drawCards(img, gc, nextInt, player.next());
			
			if(i%players.size()==0){
				counter++;
			}
		}

		
		pane.getChildren().add(cnvs);
		
		primaryStage.show();
*/		
		
	}
	
	static void createServer(){
		ServerSocketChannel serverChannel;
		Selector selector;
		try {
			serverChannel = ServerSocketChannel.open();
			InetSocketAddress address = new InetSocketAddress(777);
			serverChannel.bind(address);
			serverChannel.configureBlocking(false);
			selector = Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}
		cardHead = 0;
		
		Platform.runLater(()->{
			drawCards(img, gc, cardsIdxs[cardHead++], 0);
			drawCards(img, gc, cardsIdxs[cardHead++], 0);
			primaryStage.show();
			
		});
		
		boolean done= true;
		while(done){
			try {
				selector.select();
			} catch (IOException ex) {
				ex.printStackTrace();
				break;
			}

			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = readyKeys.iterator();
			Integer playerNum=0;
			
			while (iterator.hasNext()) {
				
				SelectionKey key = iterator.next();
				iterator.remove();
				try {
					if (key.isAcceptable()) {
						playerNum++;
						
													
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel client = server.accept();
						
						Player e = new Player(client.getLocalAddress().toString(),playerNum);
						

						
						System.out.println("Accepted connection from " + client);
						client.configureBlocking(false);
						SelectionKey key2 = client.register(selector, SelectionKey.OP_WRITE);
						
						key2.attach(e);
						
						
						
					} else if (key.isWritable()&& ((Player)key.attachment()).counter==0) {
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer buffer = ByteBuffer.allocate(72);
						
						Player e = (Player)key.attachment();
						
						if(!players.contains(e)){
							players.add(e);
							buffer.putInt(e.num);
							buffer.flip();
							client.write(buffer);
							buffer.clear();
						}
						
						e.giveCards(cardsIdxs[cardHead++]);
						e.giveCards(cardsIdxs[cardHead++]);
						
						buffer.putInt(e.cards[0]);
						buffer.putInt(e.cards[1]);
						
						buffer.flip();
						client.write(buffer);
						
						
						
//						done = false;
						
					}
				} catch (IOException ex) {
					key.cancel();
					try {
						key.channel().close();
					}
					catch (IOException cex) {}
				}
			}

		}
		
	}

	private static void drawCards(Image img, GraphicsContext gc, int nextInt, Integer player) {
		System.out.println("drawing " + nextInt + " card");
		if(nextInt<13){
//			gc.drawImage(img, 215, 300, 120,180, 0, nextInt*180, 120, 180);
			drawCard(img, gc,0, nextInt, player);
		}
		else if(nextInt<26){
//			gc.drawImage(img, 215, 300, 120,180, 120, (nextInt-13)*180, 120, 180);
			drawCard(img, gc,1, nextInt-13, player);
		}
		else if(nextInt<39){
//			gc.drawImage(img, 215, 300, 120,180, 
//					240, (nextInt-26)*180, 120, 180);
			drawCard(img, gc,2, nextInt-26, player);
		}
		else{
			drawCard(img, gc,3, nextInt-39, player);
		}
		counter++;
	}

	private static void drawCard(Image img, GraphicsContext gc, int column, int nextInt, int player) {
		if (player==0)
			gc.drawImage(img, 
				120*column, (nextInt)*180, 120, 180
				, 215+40*counter, 300, 120,180);
		if (player==1)
			gc.drawImage(img, 
					120*column, (nextInt)*180, 120, 180
					, 0+40*counter, 0, 120,180);
		
		if (player==2)
			gc.drawImage(img, 
					120*column, (nextInt)*180, 120, 180
					, 400+40*counter, 0, 120,180);

		
	}

}
