package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ServerSocketFactory;

import support.Vehicle;
import utils.SerializationUtils;

import database.JDBCDatabase;

public class Server {

	private volatile boolean connected = true;

	//Threaded
	private volatile List<ServerClient> clients = Collections.synchronizedList(new ArrayList<ServerClient>());
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private volatile ArrayList<Vehicle> activeVehicles;
	private volatile ServerSocket server;

	private int port;
	
	public Server(int port, String dbUserName, String dbPassword) {
		try {
			this.port = port;
			
			activeVehicles = new ArrayList<Vehicle>();
			JDBCDatabase.setupJDBCDatabase(dbUserName,dbPassword);

		    ServerSocketFactory ssf = (ServerSocketFactory) ServerSocketFactory.getDefault();
		    server = (ServerSocket) ssf.createServerSocket(this.port);
		} catch (IOException e) {
			System.out.println("An IO error occurred creating socket factory" + e.getMessage());
		} catch(Exception e){
			System.out.println("An error occurred creating socket factory" + e.getMessage());
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		try{
			this.connected = connected;
			if (!this.connected) {
				System.out.println("Cleaning up...");

				//Disconnect all clients connected to the server
				for (ServerClient client : this.clients) {
					client.disconnect();
				}
				
				scheduler.shutdownNow();
				
				//If in a blocking state then close the server to get out of that state
				server.close();
				
				System.out.println("...Done");
				System.out.println("Goodbye...");
			}
		
		} catch (IOException e) {
			System.out.println("An error occurred when disconnecting" + e.getMessage());
		}
	}

	//This method is threaded
	public void start() { 
		try {			
			//while the server is connected continue receiving clients
			while (server.isBound() && this.isConnected()) {
				Socket client = (Socket) server.accept();
				System.out.printf("Client connected... %s%n", client.getInetAddress());
				ServerClient serverClient = new ServerClient(client, this);
				this.clients.add(serverClient);
			}
		}
		catch(SocketException se){
			//Do nothing
			//This is needed so that when we exit the program, the server.accept() doesn't stay in a blocking state
		}
		catch (Exception e) {
			System.out.println("An error occurred during client connection " + e.getMessage());
		}
	}

	//When an image is received, start a timer on the server to count down the grace period
	public void processReceivedData(final Vehicle capturedVehicle) {
		
		//If the vehicle is entering for the first time
		if(!activeVehicles.contains(capturedVehicle)){
			
			activeVehicles.add(capturedVehicle);
			
			//Schedule a grace period for the vehicle
			scheduler.schedule(new Runnable() {
				public void run() { 
					
					//If the vehicle hasn't been removed from active vehicles
					if(activeVehicles.contains(capturedVehicle)){
						System.out.println("Violation detected");
						
						//Store that plate/lot combo in db
						JDBCDatabase.getDatabase().storeParkingViolation(capturedVehicle);
						
						activeVehicles.remove(capturedVehicle);
					}
				}
			}, Vehicle.GraceQuantumMillis, TimeUnit.MILLISECONDS);
		}
		else{
			activeVehicles.remove(capturedVehicle);
		}
		
		//serialize vehicle to send
		byte[] vehicleBytes = SerializationUtils.vehicleToBytes(capturedVehicle);
		
		//send data to all clients
		for(ServerClient sc: clients){
			if(sc.getWriter().isConnected() && sc.getReader().isConnected()){
				sc.writeMessage(vehicleBytes);
			}
		}
	}

	public void processActiveVehiclesRequest(ServerClient requester){
		for(Vehicle activeVehicle: activeVehicles){
			//serialize vehicle to send
			byte[] vehicleBytes = SerializationUtils.vehicleToBytes(activeVehicle);
			
			requester.writeMessage(vehicleBytes);
		}
	}
}