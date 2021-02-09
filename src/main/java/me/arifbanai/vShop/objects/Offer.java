package me.arifbanai.vShop.objects;

import org.bukkit.Material;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class describes an Offer that a player is making on the market.
 * An offer contains the seller's UUID, the textID of the item, the amount, and the price per item.
 *
 * @author Arif Banai
 * @version 1.1
 * @since 1.0
 */
@SuppressWarnings("CanBeFinal")
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

	/**
	 * A new offer
	 * @param sellerUUID - the seller's UUID
	 * @param textID	 - the item's textID
	 * @param amount	 - the amount of the item being sold
	 * @param price		 - the price PER item, total price = (amount * price)
	 */
	public Offer(String sellerUUID, String textID, int amount, double price) {
		this.sellerUUID = sellerUUID;
		this.textID = textID;
		
		this.amount = amount;
		this.price = price;
	}

	/**
	 * @see #Offer(String sellerUUID, String textID, int amount, double price)
	 * @param material the material's String name
	 */
	public Offer(String sellerUUID, Material material, int amount, double price) {
		this(sellerUUID, material.toString(), amount, price);
	}

	/**
	 * @param result the ResultSet containing the offers
	 * @return a List of offers contained in the ResultSet
	 */
	public static List<Offer> listOffers(ResultSet result) {
		List<Offer> offerList = new ArrayList<>();

		try {
			while (result.next()) {
				Offer o = new Offer(result.getString("sellerUUID"), result.getString("textID"),
						result.getInt("amount"), result.getDouble("price"));
				
				offerList.add(o);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("An SQLException occurred during listOffers() - Offer");
			return null;
		}

		return offerList;
	}

	/**
	 * Two offers A and B are equal if
	 * {@code (A.sellerUUID == B.sellerUUID) && (A.textID == B.textID)}
	 * @param o some Object, possibly another Offer
	 * @return true if equal, false otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Offer offer = (Offer) o;
		return sellerUUID.equals(offer.sellerUUID) && textID.equals(offer.textID);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sellerUUID, textID);
	}
}
