package com.sit.int202.backend.Order;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.util.JSONPObject;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.omise.Client;
import co.omise.ClientException;
import co.omise.models.Card;
import co.omise.models.Charge;
import co.omise.models.OmiseException;
import co.omise.models.Token;

@Service
public class OrderService {

    Properties prop = new Properties();
    // private static final String OMISE_OPUBLIC_KEY = "pkey_test_5dviz6scp4tdk4cm0au";
    // private static final String OMISE_SECRET_KEY = "skey_test_5dviz7b84hcwu90jews";
    private static final String TH_BAHT = "thb";

    @Autowired
    private OrderRepository orderRepository;
    
    @Transactional
    public List<Order> getOrderList(){
        return orderRepository.findAll();
    }
    
    public Optional<Order> getOrderById(long id){
        return orderRepository.findById(id);
    }
    
    public Order save(Order order){
        return orderRepository.save(order);
    }
    
    public long delete(long id) {
        orderRepository.deleteById(id);
        return id;
    }
    
    public LinkedHashMap getToken(LinkedHashMap data) throws ClientException, IOException, OmiseException{

        prop.load(this.getClass().getResourceAsStream("/application.properties"));
        final String PUBLIC_KEY = prop.getProperty("PUBLIC_KEY");
        final String SECRET_KEY = prop.getProperty("SECRET_KEY");

        Client client = new Client(PUBLIC_KEY,SECRET_KEY);
        LinkedHashMap<String,String> tokenData = new LinkedHashMap<>();
        try{
            Token token = client.tokens().create(
                new Token.Create().card(
                    new Card.Create()
                    .number(data.get("card_id").toString())
                    .expirationMonth(Integer.parseInt(data.get("exp_m").toString()))
                    .expirationYear(Integer.parseInt(data.get("exp_y").toString()))
                    .securityCode(data.get("cvv").toString())
                    .name(data.get("name").toString())
                )
            );
            tokenData.put("token",token.getId());
            tokenData.put("message","availiable card");
        }catch(OmiseException o){
            tokenData.put("token","null");
            tokenData.put("message",o.getMessage());
        }
        return tokenData;
    }

    public void charge(long amount,String token) throws ClientException, IOException, OmiseException {
        prop.load(this.getClass().getResourceAsStream("/application.properties"));
        final String PUBLIC_KEY = prop.getProperty("PUBLIC_KEY");
        final String SECRET_KEY = prop.getProperty("SECRET_KEY");

        Client client = new Client(PUBLIC_KEY,SECRET_KEY);
        Charge charge = client.charges().create(
            new Charge.Create()
            .amount(amount*100).currency(TH_BAHT).card(token)
        );
    }
}