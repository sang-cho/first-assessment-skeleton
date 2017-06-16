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
import com.sun.deploy.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {

    static HashMap<String, ClientHandler> listofusers=new HashMap<>();
    static ArrayList<String> listofusers2=new ArrayList<String>();
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);
    static public String previousCommand;

    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private Socket socket;

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}

	public Message message=null;
    public PrintWriter writer=null;
    public ObjectMapper mapper=null;

    public void broadcastMessage(String lemessage, HashMap listofclients)throws JsonProcessingException{
        for(ClientHandler key : listofusers.values()){

            messageSender(lemessage, key);
        }
    }

    public List<String> getusers(){
    	return listofusers2;
    }

    public void messageSender(String lesmessage, ClientHandler leclient) throws JsonProcessingException{

        message.setTimeStamp(getTimestamp());
        message.setContents(message.getTimeStamp()+ " "+ message.getUsername() + " " +lesmessage);
        String messageasstring=leclient.mapper.writeValueAsString(message);
        //String lolstringtest="testing";

        leclient.writer.write(messageasstring);
        leclient.writer.flush();
        //System.out.println(messageasstring);
        //System.out.println(leclient.toString());
        //System.out.println(lesmessage + " " + leclient);
    }
    //same thing as messageSender, only it adds the (whisper) string....probably a better way to do this in the client..."temporary"
    public void whisperSender(String lesmessage, ClientHandler leclient) throws JsonProcessingException{

        message.setTimeStamp(getTimestamp());
        message.setContents(message.getTimeStamp()+ " "+ message.getUsername() + " (whisper) " +lesmessage);
        String messageasstring=leclient.mapper.writeValueAsString(message);


        leclient.writer.write(messageasstring);
        leclient.writer.flush();
        //System.out.println(messageasstring);
        //System.out.println(leclient.toString());
        //System.out.println(lesmessage + " " + leclient);
    }

    private String getTimestamp() {
        return dateFormat.format(new Date());
    }

//    private String getPreviousCommand(){
//        return previousCommand;
//    }

	public void run() {
		try {


			mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			while (!socket.isClosed()) {





                String raw = reader.readLine();
                message = mapper.readValue(raw, Message.class);

                System.out.println(message.toString());






                if(message.getCommand().startsWith("@")){
                    log.info("user <{}> whispered message <{}>", message.getUsername(), message.getContents());
                    String username = message.getCommand().substring(1);
                    ClientHandler targetUser = listofusers.get(username);

                    String whispmessage=message.getContents();
                    whisperSender(whispmessage,targetUser);
                }
                    switch (message.getCommand()) {
                        case "broadcast":
                            log.info("user <{}> broadcasted message <{}>", message.getUsername(), message.getContents());
                            String realbroadcastMessage = "(all) " + message.getContents();
                            broadcastMessage(realbroadcastMessage, listofusers);
//						String hi= mapper.writeValueAsString(message);
//                        writer.write(hi);
//                        writer.flush();
                            break;


//                        case "@username":
//                            log.info("user <{}> used command<{}>", message.getUsername(), message.getCommand());
//                            message.setTimeStamp(getTimestamp());
//                            String whispermessage=message.getContents();
//                            messageSender(whispermessage,);
//                            break;
                        case "users":
                            log.info("user <{}> used command<{}>", message.getUsername(), message.getCommand());
                            message.setTimeStamp(getTimestamp());
                            message.setContents(message.getTimeStamp() + " currently connected users: \n" + StringUtils.join(listofusers2, "\n"));
                            String idkstuff = mapper.writeValueAsString(message);
                            //log.info(idkstuff);
                            writer.write(idkstuff);
                            writer.flush();
                            break;

                        case "connect":
                            log.info("user <{}> connected", message.getUsername());
                            message.setTimeStamp(getTimestamp());
                            listofusers.put(message.getUsername(), this);
                            listofusers2.add(message.getUsername());
                            String connectionMessage = "has connected";
                            broadcastMessage(connectionMessage, listofusers);
                            previousCommand=message.getCommand();
                            //log.info(message.getUsername() + " " + listofusers.get(message.getUsername()) + " " + listofusers.size());
                            break;
                        case "disconnect":
                            log.info("user <{}> disconnected", message.getUsername());
                            message.setTimeStamp(getTimestamp());
                            listofusers.remove(message.getUsername(), this);
                            listofusers2.remove(message.getUsername());
                            String disconnectMessage = " " + "has disconnected";
                            broadcastMessage(disconnectMessage, listofusers);

                            //log.info(message.getUsername() + " " + listofusers.get(message.getUsername()));
                            this.socket.close();
                            break;
                        case "echo":
                            log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
                            message.setTimeStamp(getTimestamp());
                            String ogMessage = message.getContents();
                            message.setContents(message.getTimeStamp() + " " + message.getUsername() + " " + "(" + message.getCommand() + ")" + " " + ogMessage);
                            String response = mapper.writeValueAsString(message);
                            writer.write(response);
                            writer.flush();
                            break;
//
//                         default:
//                             message.setCommand(previousCommand);
                    }
                }


		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}

	}

}
