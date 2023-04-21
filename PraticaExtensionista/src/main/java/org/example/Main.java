package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class Main {

    public static void main(String[] args){
        try {


            LocalTime hrAtual = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            String hrAtualFormatter = hrAtual.format(formatter);

            URL obj = new URL("https://api.bcb.gov.br/dados/serie/bcdata.sgs.10813/dados/ultimos/1?formato=json");

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null){
                response.append(inputLine);
            }

            in.close();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response.toString());
            double cotacaoDolar = jsonNode.get(0).get("valor").asDouble();
            System.out.println("Cotação do dólar atual: " + cotacaoDolar + " Horário: " + hrAtualFormatter);

            String host = "smtp.gmail.com";
            int port = 587;
            String username = System.getenv("EMAIL_FROM");
            String password = System.getenv("PASSWORD");

            String from = System.getenv("EMAIL_FROM");
            String to = System.getenv("EMAIL_TO");
            String subject = "Cotação do Doólar atual";
            String content = "Cotação do Dólar: R$" + cotacaoDolar + ", Horário: " + hrAtualFormatter;

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            Authenticator auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            };

            Session session = Session.getInstance(props, auth);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);

            System.out.println("E-mail enviado com sucesso!");

        }catch (Exception e){
            System.out.println("Erro: " + e.getMessage());
        }
    }
}