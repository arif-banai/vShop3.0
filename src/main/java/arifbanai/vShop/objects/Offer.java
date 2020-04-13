package main.java.arifbanai.vShop.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

public class Offer {

	// unique identifier for a player's account
	public String sellerUUID;
	
	//Unique id for item
	public String textID;

	// amount for sale
	public int amount;

	// price per item for sale
	// total price = amount * price
	public double price;

	public Offer(String sellerUUID, String textID, int amount, double price) {
		this.sellerUUID = sellerUUID;
		this.textID = textID;
		
		this.amount = amount;
		this.price = price;
	}
	
	public Offer(String sellerUUID, Material material, int amount, double price) {
		this.sellerUUID = sellerUUID;
		textID = material.toString();
		
		this.amount = amount;
		this.price = price;
	}
	
	public static List<Offer> listOffers(ResultSet result) {
		List<Offer> offerList = new ArrayList<Offer>();

		try {
			while (result.next()) {
				Offer o = new Offer(result.getString("sellerUUID"), result.getString("textID"),
						result.getInt("amount"), result.getDouble("price"));
				
				offerList.add(o);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("An SQLException occured during listOffers() - Offer");
			return null;
		}

		return offerList;
	}
}
