package poker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
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

class BarebonePlayer implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Op action;
	public String ip;
	public String name;

	
	
	@Override
	public String toString() {
		return "BarebonePlayer [action=" + action + ", ip=" + ip + ", name=" + name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
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
		BarebonePlayer other = (BarebonePlayer) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		return true;
	}

	public BarebonePlayer(Op action, String ip, String name) {
		this.action = action;
		this.ip= ip;
		this.name = name;
	}

	public void setReady(boolean b) {
		this.action= Op.Ready;
		
	}
	
	boolean isReady(){
		return this.action.op==Op.Ready.op;
	}
	
}

class Player extends BarebonePlayer {
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


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
	
	public Op action;
	public String ip;
	public String name;
	public int num;
	int[] cards=new int[2];
	int counter=0;
	private boolean ready;
	

	public Player(String ip, int num) {
		super(Op.None,ip, String.valueOf(num));
//			this.ip = ip;
//		// TODO Auto-generated constructor stub
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

enum Op{
	Ready(0),
	Check(1),
	Pass(2),
	Raise(3), None(-1), Call(4);
	int op;
	
	Op(int op){
		this.op=op;
	}
}


public class Main extends Application{
	
		
	private final class Client implements Runnable {
		
				
		private SocketChannel client;
		public Client(SocketChannel client) {
			this.client = client;
		}

		PokerTable tbl = new PokerTable();
		Player player1 = null;
		@Override
		public void run() {

			Thread curT = Thread.currentThread();
			try {
//						ByteBuffer buffer = ByteBuffer.allocate(72);
//						WritableByteChannel out = Channels.newChannel(System.out);
//						int me = -1;
				
				ObjectInputStream ois = new ObjectInputStream(Channels.newInputStream(client));
				while(true){
					try {
						Object readObject = ois.readObject();
						
						if(readObject instanceof PokerTable){
							PokerTable ptbl = (PokerTable) readObject;
							System.out.println(ptbl.bp);
							if(ptbl.flop.equals(tbl.flop)){
								Platform.runLater(() -> {
									ptbl.flop.stream().forEach(i->drawFlop(img, gc, i));
									primaryStage.show();
								});
								tbl.flop.clear();
								tbl.flop.addAll(ptbl.flop);
							}
							
							
						}
						if(readObject instanceof Player){
							Player player2 = (Player) readObject;
							System.out.println(player2);
							if(player1==null||!player1.equals(player2)){
								Platform.runLater(() -> {
									Arrays.stream(player2.cards).forEach(i -> drawCards(img, gc, i, 0));
									primaryStage.show();
								});
								player1=player2;
								
							}
							
						}
						
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					catch(EOFException e){
//						e.printStackTrace();
					}
				}
				/*
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
							buffer.clear();
							primaryStage.show();});
						// counter++;
						
						
//								break;
					}

					if (buffer.remaining() == 12) {
						counter = 0;
						Platform.runLater(() -> {
							drawFlop(img, gc, buffer.getInt());
							drawFlop(img, gc, buffer.getInt());
							drawFlop(img, gc, buffer.getInt());
							buffer.clear();
							primaryStage.show();
						});
					}
					
					out.write(buffer);
					buffer.clear();

				}*/
			} catch (IOException e) {
				if(curT.isInterrupted())
					return;
				e.printStackTrace();
				
			}
			

		}
	}


	private final class CheckEvent implements EventHandler<ActionEvent> {
		
private Client client;
private Op op;

public CheckEvent(Client client, Op op) {
	this.client = client;
	this.op = op;
	
		}

//		public CheckEvent(ServerChannel serv) {
//			// TODO Auto-generated constructor stub
//		}
		@Override
		public void handle(ActionEvent event) {
			
			client.player1.action = op;
			
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream nos;
			try {
				nos = new ObjectOutputStream(bout);
				nos.writeObject(client.player1);
				byte[] byteArray = bout.toByteArray();
				ByteBuffer buffer = ByteBuffer.wrap(byteArray);
				ByteBuffer size = ByteBuffer.allocate(4+byteArray.length);
				size.putInt(byteArray.length);
				size.put(byteArray);
				size.flip();
				System.out.println("Sending size " + byteArray.length+"; array"+Arrays.toString(byteArray));
				client.client.write(size);
//				buffer.flip();
//				client.client.write(buffer);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
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
	private static volatile int cardHead;
	private Thread clientThread;
	private Thread serverThread;;

	private static void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		super.stop();
		if(serverThread!=null)
			serverThread.interrupt();
		clientThread.interrupt();
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
		Button pass = new Button("Pass");
		VBox vbox = new VBox();
	    vbox.setPadding(new Insets(10));
	    vbox.setSpacing(8);
	    vbox.getChildren().addAll(check, call, raise, ready, pass);
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

			serverThread = new Thread(r, "Poker Server");
			serverThread.start();


		}

		//create Client 


		try {
			SocketAddress address = new InetSocketAddress("127.0.0.1", 777);
			SocketChannel client = SocketChannel.open(address);

			

			Client cli = new Client(client);

			ready.setOnAction(new CheckEvent(cli, Op.Ready));
			check.setOnAction(new CheckEvent(cli, Op.Check));
			raise.setOnAction(new CheckEvent(cli, Op.Raise));
			call.setOnAction(new CheckEvent(cli, Op.Call));
			pass.setOnAction(new CheckEvent(cli, Op.Pass));
			
			
			clientThread = new Thread(cli, "Client Thread");

			clientThread.start();


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
		PokerTable tbl = new PokerTable();
		
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
		
		Thread curT = Thread.currentThread();
		Integer playerNum=0;
		boolean done= true;
		while(done){
			
			
			
			try {
				selector.select();
			} catch (IOException ex) {
				if(curT.isInterrupted())
					return;
				ex.printStackTrace();
				break;
			}

			Set<SelectionKey> readyKeys = selector.selectedKeys();
			
			
			
			Iterator<SelectionKey> iterator = readyKeys.iterator();
			
			while (iterator.hasNext()) {
				
				SelectionKey key = iterator.next();
				
//				System.out.println("Key Readable: "+key.isReadable()+" Key Writable: "+key.isWritable() +" Key "+ key.attachment());
				
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
						Player e = (Player)key.attachment();
						BarebonePlayer bbp = new BarebonePlayer(e.action, e.ip, e.name);
						if(!tbl.bp.contains(bbp)){
							tbl.bp.add(bbp);
						}
						
						ByteArrayOutputStream bout = new ByteArrayOutputStream();
						ObjectOutputStream nos= new ObjectOutputStream(bout);
						nos.writeObject(tbl);
						nos.writeObject(e);
						
						
						
						ByteBuffer buffer = ByteBuffer.wrap(bout.toByteArray());//allocate(72);
						
						System.out.println("Pos: " + buffer.position() + " Rem: " + buffer.remaining());
						
						client.write(buffer);
						client.register(selector, SelectionKey.OP_READ).attach(e);
						buffer.clear();
						
						/*if(e!=null&& !players.contains(e)){
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
						}*/
						
						System.out.println("Is Players Ready: "+players.stream().allMatch(i ->{ System.out.println(i);return i.isReady();}));
						System.out.println("Is flop drawn: "+e.flop);
						
						/*if(players.stream().allMatch(i->i.isReady())&& e.flop.size()!=flop.size()){
							buffer.clear();
							flop.stream().forEach(i->{buffer.putInt(i.intValue()); System.out.println("Flop to "+e+" now drawing: "+i.intValue());});
							
//							WritableByteChannel out = Channels.newChannel(System.out);
							
							buffer.flip();
//							out.write(buffer);
							System.out.println("Server buffer remaining:"+buffer.remaining());
//							buffer.flip();
							
							
							client.write(buffer);
							
							ObjectOutputStream nos= new ObjectOutputStream(Channels.newOutputStream(client));
							nos.writeObject(tbl);
							

							
							System.out.println(players.remove(e));;
							System.out.println(e);
							players.add(e);
							
							client.register(selector, SelectionKey.OP_READ).attach(e);
						}*/
						
						
						
//						done = false;
						
					}
					if(key.isReadable()){
						SocketChannel client = (SocketChannel) key.channel();
						
						ByteBuffer buffer = ByteBuffer.allocate(4);
						
						
						
						if(client.read(buffer)!=-1){
							buffer.flip();
							int size = buffer.getInt();
							System.out.println("Recieved size of "+size+" rem: "+buffer.remaining());
							buffer = ByteBuffer.allocate(size);
							int read = client.read(buffer);
							if(read==size){
							
								System.out.println(buffer.remaining() +"remaining; has array "+Arrays.toString(buffer.array()));
								
//								buffer.flip();
								
//								System.out.println(buffer.remaining() +"remaining; has array "+buffer.array());
								ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer.array()));
								Object readObject = ois.readObject();
								
								System.out.println(readObject.getClass());
								
								if(readObject instanceof Player){
									System.out.println("Server recieved " + (Player) readObject);
//									client.register(selector, SelectionKey.OP_WRITE).attach((Player) readObject);
								}

								
							}
							else{
								System.err.println("Readed "+read+" instead of "+size);
							}
							
						}
						
//						if(client.read(buffer)!=-1){
//							bout.write(buffer.array());
//							buffer.clear();
//							while(client.read(buffer)!=-1){
//								bout.write(buffer.array());
//							}
//							
//							
//														
//							
//						}
						
						
						/*if(client.read(buffer)!=-1){
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
						}*/
						
					}
				} catch (IOException ex) {
					ex.printStackTrace();
					if(curT.isInterrupted()){
						return;
					}
					key.cancel();
					try {
						key.channel().close();
					}
					catch (IOException cex) {}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
