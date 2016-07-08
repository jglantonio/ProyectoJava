import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.channels.IllegalBlockingModeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class Rip {
	
	static String IP;
	static int puerto = 520;
	static int puerto_envio = 8080;
	static int vuelta = 1;
	//static String pass = "123456789abdcf";
	static String pass = "123";
	
	public static ArrayList <ArrayList<String>> tablaEnca = new ArrayList <ArrayList<String>> ();
	public static ArrayList <String> vecinos = new ArrayList <String>();
	public static boolean trabajoHecho = false ;
	public static Hashtable <String , Boolean> vecinosHt = new Hashtable <String,Boolean>();
	public static MulticastSocket socket = null;
	public static String command = "00000001";
	public static boolean type = true;
	public static void main(String[] args) {
	
		String aux = "";
	 
		IP = args[0];
		//pass = pedirClave();
		
		tablaEnca.add(new ArrayList<String>());	 
		tablaEnca.add(new ArrayList<String>());	 
		tablaEnca.add(new ArrayList<String>());	 
		tablaEnca.add(new ArrayList<String>());	 
		tablaEnca.get(0).add("IP");	 	 
		tablaEnca.get(1).add("Mascara");	 
		tablaEnca.get(2).add("Next Hop");	 
		tablaEnca.get(3).add("Coste");	 
		obtenerFichero();	
		
		int z = 0 ;		 
		recepcionNodos rn = new recepcionNodos(IP,puerto);     
		rn.start();
		
		do{
			for(int y = 0 ; y < vecinos.size() ; y++){
				System.out.println(vecinos.get(y));
				vecinosHt.put(vecinos.get(y), true);
			}
		
			System.out.println("Comienza la vuelta "+ vuelta++);
			try {
			
				//Lock lock = new ReentrantLock();			
				 	String vD ="";			
				Thread.sleep(10000);			
				
				
				for(int y = 1 ; y < tablaEnca.get(z).size() ; y++){				
					while(z <= 3){								
						vD += tablaEnca.get(z).get(y)+",";				
						z++;				
					}				
					z = 0;			
				}
				
				for(int x = 1 ; x < tablaEnca.get(2).size() ; x++){					
					for(int y = 0 ; y < vecinos.size() ; y++){				   		
						if(!aux.equals(tablaEnca.get(2).get(x)) && vecinos.get(y).equals(tablaEnca.get(2).get(x))){
							aux = vecinos.get(y).toString();				   			
							if(!aux.equals("255.0.0.0") && !aux.equals("255.255.0.0") && !aux.equals("255.255.255.0") && !aux.equals("255.255.255.255")){				   				
								System.out.println("[INFO][MAIN]Se usará la IP: "+aux);		
								//Thread.sleep(10000/(vecinos.size()-1));
								envioNodo n = new envioNodo(aux,puerto,IP,vD);					    	 	
								n.start();				   			
							}else{				   				
								System.out.println("[WARNING][MAIN]No entró la linea "+aux);				   			
							}				   		
						}					
					}			   	 
				}
			
				vD = "";			
				//lock.unlock();			
				while(!trabajoHecho) 				
					Thread.sleep(1000);
				trabajoHecho = false ; 
		
			} catch (InterruptedException e) {					
				e.printStackTrace();		
			}				
			System.out.println("Fin");
			aux = "";    
		}while(true);	
	}

	public static String pedirClave(){
		BufferedReader br = new BufferedReader (new InputStreamReader (System.in));
		String dato = "";
		Boolean valido = false;
		try{
			do{
				System.out.println("Introduzca la clave :");
				dato = br.readLine();
				System.out.println("Longitud del dato es de : "+ dato.length());
				if(dato.length() > 16){
					System.out.println("La clave tiene que ser menor de 16 caracteres.");
				}else{
					System.out.println("Valor introducido correctamente");
					valido = true;
				}
			}while(!valido);
		}catch(Exception err){
		 err.printStackTrace();
		}
		return "";
	}
	/* 	public static void snifferVecinos(String tabla){
	 *  Descripcion : Función que añade a la tabla de encaminamiento las 
	 *  			  | IP | MAC | NEXTHOP | COSTE | de los correspondientes 
	 *  		      strings que le entren procendentes de la funcion que lo 
	 *  			  llame.
	 *  Devolucion : Crear la Tabla de Encaminamiento.
	 * */
	
	public static void snifferVecinos(String dato){
		
		String expReg10 = "((10)(\\.([2][0-5][0-5]|[1][0-9][0-9]|[1-9][0-9]|[0-9])){3})|((10)(\\.([2][0-5][0-5]|[1][0-9][0-9]|[1-9][0-9]|[0-9])){3})";
		String expReg172 = "(172)\\.(1[6-9]|2[0-9]|3[0-1])(\\.([2][0-5][0-5]|[1][0-9][0-9]|[1-9][0-9]|[0-9])){2}";
		String expReg198 = "(192)\\.(168)(\\.([2][0-5][0-5]|[1][0-9][0-9]|[1-9][0-9]|[0-9])){2}";
		Pattern p = Pattern.compile(expReg10 + expReg172 + expReg198);
		Matcher ma = p.matcher(dato);
		Boolean flag = false;
		
		while(ma.find()){			
			if(!ma.group().equals(IP)){			
				for(int x = 0 ; x < dato.length() ; x++){				
					if(dato.charAt(x) == '/'){				   
						flag = true;				   				   
						int y = Integer.parseInt(dato.subSequence(x+1,dato.length()).toString());				   
				   
						//System.out.println(y);
				   
						
					switch(y){				   
						
						case 0: 
							tablaEnca.get(0).add(IP);		   		   		   
							tablaEnca.get(1).add("0.0.0.0");		   		   	       
							tablaEnca.get(2).add(ma.group().toString());		   		           
							tablaEnca.get(3).add("1");		   		           
							break;
				   
						case 8: 
							tablaEnca.get(0).add(IP);				   		   
							tablaEnca.get(1).add("255.0.0.0");				   		   
							tablaEnca.get(2).add(ma.group().toString());				   		   
							tablaEnca.get(3).add("1");				   		   
							break;
				  
						case 16: 					   
							tablaEnca.get(0).add(IP);		   										   
							tablaEnca.get(1).add("255.255.0.0");		   										   
							tablaEnca.get(2).add(ma.group().toString());		   										   
							tablaEnca.get(3).add("1");						    					   
							break;
				   				   
						case 24: 					   
							tablaEnca.get(0).add(IP);				   	        					   
							tablaEnca.get(1).add("255.255.255.0");				   	        
							tablaEnca.get(2).add(ma.group().toString());
				   	        tablaEnca.get(3).add("1");
				   	        break;
				   
						case 32: 
							tablaEnca.get(0).add(IP);				   			
							tablaEnca.get(1).add("255.255.255.255");
				   			tablaEnca.get(2).add(ma.group().toString());
				   			tablaEnca.get(3).add("1");
				   			System.out.println("Añadido el vecino " + IP);
				   	        vecinos.add(IP);
						    break;
				   
						default: 
							tablaEnca.get(0).add(IP);
		   		  			tablaEnca.get(1).add("999.999.999.999");
		   		  			tablaEnca.get(2).add(ma.group().toString());
		   		  			tablaEnca.get(3).add("1");
		   		  			break;				   					
					}				
					
				  }
					
					if(x == (dato.length() - 1) && !flag ){					
						tablaEnca.get(0).add(IP);		   			
						tablaEnca.get(1).add("255.255.255.255");		   			
						tablaEnca.get(2).add(ma.group().toString());		   			
						tablaEnca.get(3).add("1");		   			
						System.out.println("añadidod el vecino " + ma.group().toString());		   	        
						vecinos.add(ma.group().toString());				
					}			
				}		
			}	
		}		
	}
	
	
	/* public static String obtenerFichero(){
	 * Descripcion : Esta funcion lo que hace es obtener el fichero de 
	 * 			     configuracion de la maquina en la que se esta a ejecutar 
	 * 				 el ejecutable , donde el fichero de entrada es ripconf-A.B.C.D.txt , 
	 * 				 siendo A.B.C.D la IP que el usuario introduce por parámetros o en su
	 * 			     defecto la IP del equipo en le que se está ejecutando
	 * 				 el cual es renombrado a "riconf-"+ip+".txt". 
	 * 				 Despues carga todo su contenido en un String.
	 * Devolucion :  Un string con toda la información.
	 * */

	public static void obtenerFichero(){
		
		String nombreFichero = "ripconf-"+ Rip.IP +".txt";
		File archivo = null;
		archivo = new File (nombreFichero);
		FileReader fileReader = null;
		BufferedReader bufferedReader = null;
		
		try {
			
			System.out.println(archivo.getAbsoluteFile());
			fileReader = new FileReader(archivo);
			bufferedReader= new BufferedReader(fileReader);
			String linea = "";
			
			while(((linea = bufferedReader.readLine()))!= null){
				System.out.println("Del fichero se obtiene "+linea);
				Rip.snifferVecinos(linea);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println ("Función obtenerFichero");
			System.out.println ("El fichero no existe");
		}catch (IOException e) {
			e.printStackTrace();
		
		}finally{
			
			try {
				fileReader.close();
				bufferedReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return;	
	}
	
	
	/* public static String getIP()
	 * 
	 * Descripcion : Devuelve la IP del localhost , haciendo un split para separar
	 * 			     el (Nombre localhost)/(IP localhost).
	 * 
	 * */
	 public static String getIP(){
		
		 Enumeration<NetworkInterface> IPs;		
		 try {			
			 IPs = NetworkInterface.getNetworkInterfaces();			
			 for (NetworkInterface Nets : Collections.list(IPs)){				
				 List<InterfaceAddress> Lista = Nets.getInterfaceAddresses();				
				 for(int x = (Lista.size()-1)  ; x >= 0 ; x--){					
					 System.out.println(Lista.get(x).toString());					
					 String[] datos = Lista.get(x).toString().split("/");					
					 System.out.println("IP : "+ datos[1]);					
					 IP = datos[1];					
					 break;				
				 }				
				 break;			
			
			 }			
			 System.out.println(IP);			
			 return IP;
		
		 } catch (SocketException e) {		 
			 e.printStackTrace();
		
		 } catch (NullPointerException e){			
			 e.printStackTrace();			
			 System.out.println ("Función getIP");			
			 System.out.println ("Se ha obtenido un valor nulo");
		
		 } catch (ArrayIndexOutOfBoundsException e){			
			 e.printStackTrace();			
			 System.out.println ("Función getIP");			
			 System.out.println ("Array fuera de valor.");
		
		 } catch (PatternSyntaxException e){
			
			 e.printStackTrace();			
			 System.out.println ("Función getIP");			
			 System.out.println ("Error en el comand split , expresión regular inválida");		
		 }		
		 return null;	
	 }
}


/* Clase envioNodos
 * Descripción : Esta clase contiene lo necesario para enviar los vectores 
 *               al recepcionNodos , en cuyo vector de distancia , esta contenida la clave , 
 *               ya que se le añade al final de dicho vector , como especifica RIP en la RFC.
 * */

class envioNodo extends Thread {
	
	byte[] tablaEnca = new byte[250];
	
	int port = 0;
	int puertoVecino = 0;
	String IP_envio = "" , IP_emisor ="";
	String unused = "0000000000000000";
	String pw ;
	byte[] ArrayBytes;
	int pos = 0;
	public envioNodo(String IP , int puerto , String IP_e , String vD) {		
		port = puerto;
		IP_envio = IP;
		IP_emisor = IP_e;
		//tablaEnca = aux.getBytes();
		//tablaEnca = theCreator().getBytes();
		// OK CABECERA AUTHENTUCATION
		System.out.println(Rip.type);
		tablaEnca[0] = 0x2;
		tablaEnca[1] = 0x2;
		tablaEnca[2] = 0x00;
		tablaEnca[3] = 0x00;
		tablaEnca[4] = (byte) 0xFF;
		tablaEnca[5] = (byte) 0xFF;
		tablaEnca[6] = 0x0;
		tablaEnca[7] = 0x2;
		ArrayBytes = Rip.pass.getBytes();
		for(int x = 0 ; x < 16 ; x++){
			if(ArrayBytes.length > x)
				tablaEnca[8+x] = ArrayBytes[x];
			else
				tablaEnca[8+x] = 0x00;
		}
		
		theTable();
		
	    System.out.println("[INFO][envioNodo"+IP_envio+"]Se enviará el vector de distancias a la IP "+ IP_envio +" con puerto "+ port);
	}
  	
	

	private void theTable() {
		
		String fila = "";
		pos = 24;
		
		for(int x = 1 ; x < Rip.tablaEnca.get(0).size() ; x++){

			// Address Family Identifier (OK)
			tablaEnca[pos++] = 0x00;
			tablaEnca[pos++] = 0x02;
			
			// Router tag 0x00 (OK)
			
			tablaEnca[pos++] = 0x00;
			tablaEnca[pos++] = 0x00;
			
			// IP ADDRESS 
			//System.out.println(fila  +" = "+ pos);
			//normalizarDir(fila);
			fila = Rip.tablaEnca.get(0).get(x); // IP
			String [] value = fila.split("\\.");
			for (String string : value){
				int z = Integer.parseInt(string);
				tablaEnca[pos++] = (byte) z;
			}
			
			fila = Rip.tablaEnca.get(1).get(x); // MASK
			value = fila.split("\\.");
			for (String string : value){
				int z = Integer.parseInt(string);
				tablaEnca[pos++] = (byte) z;
			}
			
			fila = Rip.tablaEnca.get(2).get(x); // MASK
			value = fila.split("\\.");
			for (String string : value){
				int z = Integer.parseInt(string);
				tablaEnca[pos++] = (byte) z;
			}
			
			fila = Rip.tablaEnca.get(3).get(x); // COSTE
			int z = Integer.parseInt(fila);
			tablaEnca[pos++] = 0x00;
			tablaEnca[pos++] = 0x00;
			tablaEnca[pos++] = 0x00;
			tablaEnca[pos++] = (byte) z;
			
				
			
		}
		
	}


	public  void run() {	
		//System.out.println("[INFO][envioNodo"+IP_envio+"]Se ejecuta el envio de paquetes del nodo");
		
		try {
			DatagramSocket dS = new DatagramSocket();			
			dS.setReuseAddress(true);		
			DatagramPacket dP = null;
			try {
				System.out.println("[INFO][envioNodo"+IP_envio+"]"+tablaEnca.toString());
				dP = new DatagramPacket(tablaEnca , tablaEnca.length, InetAddress.getByName(IP_envio)  , port); 				    
				dS.send(dP);				    
			} catch (UnknownHostException e) {					
				e.printStackTrace();				
			} catch (IOException e){					
				e.printStackTrace();					
				System.out.println("[ERROR][TYPE ERROR][envioNodo"+IP_envio+"] IOException");					
				System.out.println("[ERROR][INFO][envioNodo"+IP_envio+"] direccion : " + IP_envio + ":" + String.valueOf(port));				
			}				
			try {					
				dS.send(dP);			
			} catch (IOException e) {					
				e.printStackTrace();				
			} 				
			dS.close();
		} catch (SocketException e) {
			e.printStackTrace();
		}
    
  	}
}



/* Clase recepcionNodos
 * Descripción : Esta clase es la encargada de recibir los vectores de distancia de todos sus vecinos ,
 *               se  encarga de extraer del vector de distancias la contraseña y así verificar que 
 *               dicho nodo que le envia el vector de distancias pertenece a su grupo de "confianza" , 
 *               en caso contrario no entra al algoritmo de calculo de las distancias , así no afectando
 *               a la seguridad de las tablas de encaminamiento.
 * */

class recepcionNodos extends Thread {
	
	int port = 0 , aux = 0;
	String vD_rN ;
	byte[] buff = new byte[10500];
	DatagramPacket dP = null;
	DatagramSocket dS = null;
	String IP_host = "";
	
	public recepcionNodos(String IP ,int puerto){
		port = puerto;
		IP_host = IP;
		Rip.trabajoHecho = false;
	}
	
	public void run(){	
		ArrayList<String> tabla = new ArrayList<String>();
		String palabra = "";
		do{			
			try {	
				
				InetAddress dir = InetAddress.getByName(IP_host);
				dS = new DatagramSocket(520 ,dir); 
				dS.setReuseAddress(true);
				dS.setSoTimeout(12000);
				System.out.println(IP_host+":"+port);
				dP = new DatagramPacket(buff,buff.length);
				dS.receive(dP);
				byte[] vD_rN = new byte[dP.getLength()];
				vD_rN = dP.getData();
				dS.close();
				
				String t = "" ; 
				
				for (int x = 0 ; x < vD_rN.length ; x++){
					
					String val = String.valueOf(vD_rN[x]);
					int v = Integer.valueOf(val);
					//System.out.print("|"+v);
					if(x >= 8 && x <= 24){
						if(!val.equals("0")){
							t += (char)v;
							//System.out.println("El string " + val + " el integer "  + (char)v);
						}
					}	
				}
				System.out.println("La pass entrante es : " + t);
				//System.out.println(t);
				
				Rip.type = false;
				
				String text = "";
				for(int x = 0 ; x < t.length() ; x++){
					if(!(t.charAt(x) == '*')){
						text += t.charAt(x);
					}
				}
				t = text;
				String IP = "" , MASK = "" , METRIC = "" , NH = "";
				int cont = 0;
				for (int x = 24 ; x <= vD_rN.length ; x+=20){
					cont = 0;
					METRIC = "";
					IP= "";
					MASK = "";
					byte[] bloque = new byte[20];
					for(int y = 0 ; y < bloque.length ; y++){
						int z = (int) vD_rN[x+y];
						if(z == -1)
						   z = 255;
						String a = String.valueOf(z);
						//System.out.print("y = "+y  + " -> "+a+"||");
						 
						if(y >= 4 && y < 8){
							cont++;
							if(cont != 4)
								IP += a +".";
							else
								IP += a;
							if(cont == 4){
								System.out.println("IP = "+IP);
								cont = 0;
							}
						}
						if(y >= 8 && y < 12){
							cont++;
							if(cont != 4)
								MASK += a +".";
							else
								MASK += a;
							//System.out.println("##" + MASK);
							if(cont == 4){
								System.out.println("MASK = "+MASK);
								cont = 0;
							}
						}
						if(y >= 12 && y < 16){
							cont++;
							if(cont != 4)
								NH += a +".";
							else
								NH += a;
							//System.out.println("##" + MASK);
							if(cont == 4){
								System.out.println("NH = "+NH);
								cont = 0;
							}
						}
						if(y >= 19 && y < 20){
							METRIC += a;
							System.out.println("METRIC = "+METRIC);
						}
						
					}
					System.out.println("Tenemos la IP " + IP );
					if(IP.equals("0.0.0.0")){
						System.out.println("No se introduce una nueva entrada");
						break;
					}else{
						System.out.println("Nueva entrada añadida ");
						try{
							System.out.println("#############");
							System.out.println("IP = "+IP);
							System.out.println("MASK = "+MASK);
							System.out.println("NH = "+NH);
							System.out.println("METRIC = "+METRIC);
							System.out.println("#############");
							tabla.add(IP);
							tabla.add(MASK);
							tabla.add(NH);
							tabla.add(METRIC);
							
							palabra = tabla.get(0);
							
						}catch(IndexOutOfBoundsException err){
							System.out.println("IndexOutOfBoundsException");
						}
					}	
					
					METRIC = "";
					IP= "";
					MASK = "";
					NH = "";
				}
				/*System.out.println("Rip.tablaEnca");
				for(int x = 0 ; x < Rip.tablaEnca.size() ; x++){
					System.out.println(Rip.tablaEnca.get(x).toString());
				}
				*/System.out.println("bf109.tablaEntrada");
				
				//Suponiendo que esta la contraseña
				/*for(int x = 0 ; x < t.length() ; x++){
					System.out.println(t.charAt(x));
				}*/
				t = t.substring(0, t.length());
				if (t.equals(Rip.pass)){
					System.out.println("Es correcto la contraseña: " + t); // Supongo que se envia si es correcto si no no
					
					System.out.println(aux);
					
					if(Rip.vecinosHt.get(palabra.toString())){
						Rip.vecinosHt.put(palabra.toString(), false);
						BellmanFord bf109 = new BellmanFord(tabla);
						bf109.calculoBF();
					}
					
				}else{
					System.out.println("[INFO] Error en contraseña"); // No es correcto per
				}
				
				Rip.trabajoHecho = true;
			
			}catch (SocketTimeoutException e){
				System.out.println("Tiempo de espera excedido , volviendo a esperar a la llegada de nuevos paquetes.");
				dS.close();
			}catch (IOException e) {
				System.out.println("[ERROR TYPE][recepcionNodos]");
				System.out.println("[ERROR][recepcionNodos]Error en recepcion del paquete!");
				e.printStackTrace(); 
				if(aux != 0)
				 System.exit(0);
				aux++;
			}catch (IllegalBlockingModeException e) {					
				e.printStackTrace();
				System.exit(0);
			}catch (IllegalArgumentException e) {					
				e.printStackTrace();
				System.exit(0);
			}catch (SecurityException e) {					
				e.printStackTrace();
				System.exit(0);
			}
		}while(true);
	}
}


/* Clase BellmanFord 
 * Descripcion : Clase que se encarga del calculo de Bellman-Ford que es lo que pide RIP en su estandar de la RFC , 
 * 				 en este sitio también se hace el proceso de SplitHorizon. En esta clase también nos encontramos con
 * 				 una funcion que imprime el contenido de la tabla de encaminamiento una vez actualizada.
 * */
class BellmanFord  {
	
	//private String bruto ="";
	//private String[] segmentos;
	public  ArrayList <ArrayList<String>> tablaEntrada = new ArrayList <ArrayList<String>> ();
	
	public BellmanFord(ArrayList<String> t) {
    	System.out.println("[INFO][BellmanFord]Inicio"); 
    	//bruto = (string) vD_rN;
    	tablaEntrada.add(new ArrayList<String>());
    	tablaEntrada.add(new ArrayList<String>());
    	tablaEntrada.add(new ArrayList<String>());
    	tablaEntrada.add(new ArrayList<String>());
    	tablaEntrada.get(0).add("IP");	 
    	tablaEntrada.get(1).add("Mascara");
    	tablaEntrada.get(2).add("Next Hop");
    	tablaEntrada.get(3).add("Coste");
    	int cont = 0;
    	for(int x = 0 ; x < t.size() ; x++){
    		
    		switch(cont){
    		case 0 :
    			tablaEntrada.get(0).add(t.get(x));
    			break;
    		case 1 :
    			tablaEntrada.get(1).add(t.get(x));
    			break;
    		case 2 :
    			tablaEntrada.get(2).add(t.get(x));
    			break;
    		case 3 :
    			tablaEntrada.get(3).add(t.get(x));
    			break;
    		}
    		if(cont == 3){
    			cont = 0;
    		}else{
    			cont++;
    		}
    	}
    	
    }
    
	public BellmanFord() {
		tablaEntrada.add(new ArrayList<String>());
    	tablaEntrada.add(new ArrayList<String>());
    	tablaEntrada.add(new ArrayList<String>());
    	tablaEntrada.add(new ArrayList<String>());
    	tablaEntrada.get(0).add("IP");	 
    	tablaEntrada.get(1).add("Mascara");
    	tablaEntrada.get(2).add("Next Hop");
    	tablaEntrada.get(3).add("Coste");
	}
	
protected void calculoBF(){
		
		boolean flag = true;
    	int x = 2; // Array list Next Hop	
    	/*System.out.println("bf109.tablaEntrada");
		for(int y = 0 ; y < tablaEntrada.size() ; y++){
			System.out.println(tablaEntrada.get(y).toString());
		}*/
    	for(int y = 1 ; y < tablaEntrada.get(x).size() ; y++){ // Dentro de next Hop el segundo elemento
       		
    		for(int z = 1 ;  z < Rip.tablaEnca.get(2).size() ; z++) {	    		
    			if(!Rip.IP.equals(tablaEntrada.get(x).get(y))){//Si es distinto a la IP del router nativo		    				
    				if(Rip.tablaEnca.get(x).get(z).equals(tablaEntrada.get(x).get(y))){ //Si existe el elemento		    					
    					for(int w = 1 ; w < Rip.vecinos.size() ; w++) {  
    				    	if(Rip.vecinos.get(w).equals(tablaEntrada.get(0).get(y))){//Si el emisor es uno de sus vecinos				    					
    							if(Integer.valueOf(Rip.tablaEnca.get(3).get(z)) > Integer.valueOf(tablaEntrada.get(3).get(y))){ // el coste es menor que el del nativo					    					
    								Rip.tablaEnca.get(0).set(z, tablaEntrada.get(x-2).get(y));					        				
    								Rip.tablaEnca.get(1).set(z, tablaEntrada.get(x-1).get(y));					        				
    								Rip.tablaEnca.get(2).set(z, tablaEntrada.get(x).get(y));					    					
    								Rip.tablaEnca.get(3).set(z, String.valueOf((Integer.valueOf(tablaEntrada.get(x+1).get(y)) + 1))  );				    					
    							}
    					
    						}else{//SplitHorizon
    							if(Rip.tablaEnca.get(x).get(z).equals(tablaEntrada.get(x).get(y))){ //Si coinciden las dos tablas con el mismo nextHOP			    							
    								if(Integer.valueOf(Rip.tablaEnca.get(x+1).get(z)) > Integer.valueOf(tablaEntrada.get(x+1).get(y))){ // el coste es menor que el del nativo				    							
    									Rip.tablaEnca.get(0).set(z, tablaEntrada.get(x-2).get(y));
    									Rip.tablaEnca.get(1).set(z, tablaEntrada.get(x-1).get(y));	    		    							
    									Rip.tablaEnca.get(2).set(z, tablaEntrada.get(x).get(y));	    		    							
    									Rip.tablaEnca.get(3).set(z, String.valueOf((Integer.valueOf(tablaEntrada.get(x+1).get(y)) + 1)));	    		    						
    								}				    						
    								flag = true;				    					
    							}
    						}
    					}		    					
    					
    					break; // Si esta comprobado no hace falta seguir buscando		    				
    				}else{ //En caso de que no existe se añade.		    					
    					if ( z == (Rip.tablaEnca.get(2).size()-1) && flag)		    						
    						flag = false;		    					
    					if (!flag ){		    						
    						Rip.tablaEnca.get(0).add(tablaEntrada.get(x-2).get(y));		    						
    						Rip.tablaEnca.get(1).add(tablaEntrada.get(x-1).get(y));		    						
    						Rip.tablaEnca.get(2).add(tablaEntrada.get(x).get(y));									
    						int aux = Integer.valueOf(tablaEntrada.get(x+1).get(y))+1;								
    						Rip.tablaEnca.get(3).add(String.valueOf(aux));									
    						flag = true;									
    						break;		    					
    					}			   				
    				}	    			
    			}else{ // Si es la misma IP no hace falta mas iteracciones	    				
    				System.out.println("Es la misma ip del host "+tablaEntrada.get(x).get(y));	    				
    				break;
    			}			
    		}   	   		
    	}    	
    	impresion();
    	tablaEntrada.clear(); 
}
    
	  
	public void impresion(){
	
		ArrayList<String> aux = new ArrayList<String>();	    	
		aux = Rip.tablaEnca.get(0);
		System.out.println("[INFO][BellmanFord]Imprime tabla Encaminamiento ");	    	                 
		for(int x= 0 ; x < aux.size() ; x++){                		
			if(x==0){	    	   
				System.out.println("|   " + Rip.tablaEnca.get(0).get(x) + "    | " + Rip.tablaEnca.get(1).get(x) + "         |  " + Rip.tablaEnca.get(2).get(x) +"  | " + Rip.tablaEnca.get(3).get(x) + "|"); 
			}  else{      	                           	 
				if(Rip.tablaEnca.get(1).get(x).equals("255.255.255.0")){	    	   	                		 
					System.out.println("| " + Rip.tablaEnca.get(0).get(x) + "| " + Rip.tablaEnca.get(1).get(x) + "   | " + Rip.tablaEnca.get(2).get(x) +"   | " + Rip.tablaEnca.get(3).get(x)+"    |");         	                	    	   	                	 
				}else{	    	   	                		 
					System.out.println("| " + Rip.tablaEnca.get(0).get(x) + "| " + Rip.tablaEnca.get(1).get(x) + " | " + Rip.tablaEnca.get(2).get(x) +"   | " + Rip.tablaEnca.get(3).get(x)+"    |");           	                 	    	   	                	 
				}	    	   	                	
			}
		}  	                 
		System.out.println("[INFO][BellmanFord]Termina tabla Encaminamiento ");	    	                 
		try {
			Runtime.getRuntime().exec("clear");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}

