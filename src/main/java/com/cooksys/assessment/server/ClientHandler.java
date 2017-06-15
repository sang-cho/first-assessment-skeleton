package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.deploy.util.SessionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {

    static HashMap<String, ClientHandler> listofusers=new HashMap<>();
    static ArrayList<String> listofusers2=new ArrayList<String>();
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

    //DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    //Date date = new Date();


    private Socket socket;

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}

	public Message message;
    public PrintWriter writer;
    public ObjectMapper mapper;

    public void broadcastMessage(String lemessage, HashMap listofclients)throws JsonProcessingException{
        for(ClientHandler key : listofusers.values()){

            messageSender(lemessage, key);
        }
    }

    public List<String> getusers(){
    	return listofusers2;
    }

    public void messageSender(String lesmessage, ClientHandler leclient) throws JsonProcessingException{
        String messageasstring=leclient.mapper.writeValueAsString(message);
        //String lolstringtest="testing";
        leclient.writer.write(messageasstring);
        leclient.writer.flush();
        System.out.println(lesmessage);
        //System.out.println(lesmessage + " " + leclient);
    }

	public void run() {
		try {

			mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));



			while (!socket.isClosed()) {
				String raw = reader.readLine();
				 message = mapper.readValue(raw, Message.class);

				switch (message.getCommand()) {
					case "broadcast":
						log.info("user <{}> broadcasted message <{}>", message.getUsername(), message.getContents());
                        broadcastMessage(message.getContents(), listofusers);
//						String hi= mapper.writeValueAsString(message);
//                        writer.write(hi);
//                        writer.flush();
                        break;

					case "@username":
						//TODO
						break;
					case "users":
                        log.info("user <{}> used command<{}>", message.getUsername(),message.getCommand());
						message.setContents(listofusers2.toString());
						String idkstuff= mapper.writeValueAsString(message);
						log.info(idkstuff);
                        writer.write(idkstuff);
                        writer.flush();

						//TODO
						break;
					case "connect":
						log.info("user <{}> connected", message.getUsername());
                        listofusers.put(message.getUsername(),this);
                        listofusers2.add(message.getUsername());
                        //log.info(message.getUsername() + " " + listofusers.get(message.getUsername()) + " " + listofusers.size());
						break;
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
                        //listofusers.remove(message.getUsername());
                        log.info(message.getUsername() + " " + listofusers.get(message.getUsername()));
						this.socket.close();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						String response = mapper.writeValueAsString(message);
						writer.write(response);
						writer.flush();
						break;
				}
			}


		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}

	}


}
