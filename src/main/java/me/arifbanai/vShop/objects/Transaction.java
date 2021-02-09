package me.arifbanai.vShop.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("CanBeFinal")
public class Transaction {
	
	public int id;


	public String sellerUUID;
	public String buyerUUID;

	public String textID;

	public int amount;
	public double price;

	public Transaction(String sellerUUID, String buyerUUID, String textID, int amount, double price) {
		this.sellerUUID = sellerUUID;
		this.buyerUUID = buyerUUID;

		this.textID = textID;

		this.amount = amount;
		this.price = price;
	}
	
	public static List<Transaction> listTransactions(ResultSet result) {
		List<Transaction> transactionList = new ArrayList<>();

		try {
			while (result.next()) {
				Transaction t = new Transaction(result.getString("sellerUUID"), result.getString("buyerUUID"),
						result.getString("textID"), 
						result.getInt("amount"),result.getDouble("price"));
				t.id = result.getInt("id");
				
				transactionList.add(t);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("An SQLException occurred while listing transactions.");
			return null;
		}
		
		return transactionList;
	}
}
