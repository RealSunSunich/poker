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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
	@Override
	public String toString() {
		return "Player [flop=" + flop + ", ip=" + ip + ", num=" + num + ", cards=" + Arrays.toString(cards)
				+ ", counter=" + counter + ", ready=" + ready + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + num;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (num != other.num)
			return false;
		return true;
	}

	

	public ArrayList<Integer> flop = new ArrayList<>(5);
	public String ip;
	public int num;
	int[] cards=new int[2];
	int counter=0;
	private boolean ready;
	

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

	public void setReady(boolean b) {
		this.ready= b;
		
	}
	
	boolean isReady(){
		return this.ready;
	}
	
}

enum Op{
	Ready(0),
	Check(1),
	Pass(2),
	Raise(3);
	int op;
	
	Op(int op){
		this.op=op;
	}
}


public class Main extends Application{
	
		
	private final class CheckEvent implements EventHandler<ActionEvent> {
		
private SocketChannel client;
private Op op;

public CheckEvent(SocketChannel client, Op op) {
	this.client = client;
	this.op = op;
	
		}

//		public CheckEvent(ServerChannel serv) {
//			// TODO Auto-generated constructor stub
//		}
		@Override
		public void handle(ActionEvent event) {
			ByteBuffer buffer = ByteBuffer.allocate(72);
			buffer.putInt(op.op);
			buffer.flip();
			try {
				client.write(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	}


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

	private static volatile int counter;
	private static FlowPane pane;
	private static Scene ms;
	private GraphicsContext gc;
	private static Stage primaryStage;
	private Image img;
	private static ArrayList<Integer> flop= new ArrayList<>(5);
	private static int[] cardsIdxs = new int[52];
	private static List<Player> players;
	private static volatile int cardHead;;

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
		Button ready = new Button("Ready");
		Button call = new Button("Call");
		Button raise = new Button("Raise");
		VBox vbox = new VBox();
	    vbox.setPadding(new Insets(10));
	    vbox.setSpacing(8);
	    vbox.getChildren().addAll(check, call, raise, ready);
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

		//create Client 


		try {
			SocketAddress address = new InetSocketAddress("127.0.0.1", 777);
			SocketChannel client = SocketChannel.open(address);

			ready.setOnAction(new CheckEvent(client, Op.Ready));
			check.setOnAction(new CheckEvent(client, Op.Check));
			raise.setOnAction(new CheckEvent(client, Op.Raise));
			call.setOnAction(new CheckEvent(client, Op.Check));

			Runnable cli = new Runnable() {

				@Override
				public void run() {

					try {
						ByteBuffer buffer = ByteBuffer.allocate(72);
						WritableByteChannel out = Channels.newChannel(System.out);
						int me = -1;
						while (client.read(buffer) != -1) {
							System.out.println("Buffer position: " + buffer.position());

							buffer.flip();
							if (me == -1) {
								// String string = new String(buffer.array());
								// System.out.println(string);
								// me=Integer.valueOf(string.substring(string.lastIndexOf("
								// ")+1).trim());

								me = buffer.getInt();
								System.out.println("I am number " + me);
								// System.out.println("Next int " +
								// buffer.getInt());
							}

							if (buffer.remaining() == 8) {
								Platform.runLater(()->{
									drawCards(img, gc, buffer.getInt(), 0);
									drawCards(img, gc, buffer.getInt(), 0);
									primaryStage.show();});
								// counter++;
								
//								break;
							}

							if (buffer.remaining() == 20) {
								counter = 0;
								Platform.runLater(() -> {
									drawFlop(img, gc, buffer.getInt());
									drawFlop(img, gc, buffer.getInt());
									drawFlop(img, gc, buffer.getInt());
									primaryStage.show();
								});
							}
							
							out.write(buffer);
							buffer.clear();

						}
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			};

			Thread cliT = new Thread(cli, "Client Thread");

			cliT.start();


		} catch (IOException ex) {
			ex.printStackTrace();
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
	
	private void drawFlop(Image img2, GraphicsContext gc2, int nextInt) {
		
		if(nextInt<13){
			drawCard(img, gc,0, nextInt, 1);
		}
		else if(nextInt<26){
			drawCard(img, gc,1, nextInt-13, 1);
		}
		else if(nextInt<39){

			drawCard(img, gc,2, nextInt-26, 1);
		}
		else{
			drawCard(img, gc,3, nextInt-39, 1);
		}
		counter++;
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
		
		
		Integer playerNum=0;
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
			
			while (iterator.hasNext()) {
				
				SelectionKey key = iterator.next();
				
				System.out.println("Key Readable: "+key.isReadable()+" Key Writable: "+key.isWritable() +" Key "+ key.attachment());
				
				iterator.remove();
				try {
					
					
					if (key.isAcceptable()) {
						
													
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel client = server.accept();
						
						Player e = new Player(client.getLocalAddress().toString(),playerNum);
						

						
						System.out.println("Accepted connection from " + client+" Player number:"+playerNum );
						client.configureBlocking(false);
						SelectionKey key2 = client.register(selector, SelectionKey.OP_WRITE);
						
						key2.attach(e);
						
						playerNum++;
						
						
					} else if (key.isWritable()){//&& ((Player)key.attachment()).counter==0) {
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer buffer = ByteBuffer.allocate(72);
						
						Player e = (Player)key.attachment();
						
						if(e!=null&& !players.contains(e)){
							System.out.println("Draw cards to " + e);
							e.giveCards(cardsIdxs[cardHead++]);
							e.giveCards(cardsIdxs[cardHead++]);
							players.add(e);
							buffer.putInt(e.num);
							buffer.flip();
							client.write(buffer);
							buffer.clear();
							buffer.putInt(e.cards[0]);
							buffer.putInt(e.cards[1]);
							
							buffer.flip();
							client.write(buffer);
							
							client.register(selector, SelectionKey.OP_READ).attach(e);
						}
						
						System.out.println("Is Players Ready: "+players.stream().allMatch(i ->{ System.out.println(i);return i.isReady();}));
						System.out.println("Is flop drawn: "+e.flop);
						
						if(players.stream().allMatch(i->i.isReady())&& e.flop.size()!=flop.size()){
							buffer.clear();
							flop.stream().forEach(i->{buffer.putInt(i.intValue()); System.out.println("Flop to "+e+" now drawing: "+i.intValue());});
							
							WritableByteChannel out = Channels.newChannel(System.out);
							buffer.flip();
							out.write(buffer);
							
							buffer.flip();
							
							
							client.write(buffer);
							
							players.remove(e);
							players.add(e);
							
							client.register(selector, SelectionKey.OP_READ).attach(e);
						}
						
						
						
//						done = false;
						
					}
					if(key.isReadable()){
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer buffer = ByteBuffer.allocate(72);
						
						
						if(client.read(buffer)!=-1){
							buffer.flip();
							System.out.println("Client sent buffer with remaining "+buffer.remaining());
							
							int int1 = buffer.getInt();
							Player e = (Player) key.attachment();
							System.out.println("Client "
							+e+" sent "
									+int1);
							
							if(int1==Op.Ready.op){
								int indexOf = players.indexOf(e);
								e.setReady(true);
								players.set(indexOf, e);
								
								if(players.stream().allMatch(i->i.isReady())&& flop.isEmpty()){
									
									flop.add(cardsIdxs[cardHead++]);
									flop.add(cardsIdxs[cardHead++]);
									flop.add(cardsIdxs[cardHead++]);
									
									System.out.println(flop);
									System.out.println(players);
									
									
									
								}
								
								client.register(selector, SelectionKey.OP_WRITE).attach(e);
								
							}
						}
						
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
