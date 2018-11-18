package com.smeefgaming.vShop.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
		List<Transaction> transactionList = new ArrayList<Transaction>();

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
			System.out.println("An SQLException occured while listing transactions.");
			return null;
		}
		return transactionList;
	}
}
