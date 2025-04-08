package com.example.nyusyukkin;

import jakarta.validation.constraints.NotEmpty;
import java.util.Date;

/**
 * Parameter class representing deposit and withdrawal information.
 */
public class NyusyukkinData {

	/**
	 * Branch name.
	 */
	private String shitenName;

	/**
	 * Customer ID.
	 */
	@NotEmpty
	private String kokyakuId;

	/**
	 * Transaction type (0: withdrawal, 1: deposit).
	 */
	private int nyusyukkinKubun;

	/**
	 * Transaction amount.
	 */
	private int kingaku;

	/**
	 * Transaction date.
	 */
	private Date torihikibi;

	/**
	 * Gets the branch name.
	 * @return shitenName
	 */
	public String getShitenName() {
		return shitenName;
	}

	/**
	 * Sets the branch name.
	 * @param shitenName the branch name
	 */
	public void setShitenName(String shitenName) {
		this.shitenName = shitenName;
	}

	/**
	 * Gets the customer ID.
	 * @return kokyakuId
	 */
	public String getKokyakuId() {
		return kokyakuId;
	}

	/**
	 * Sets the customer ID.
	 * @param kokyakuId the customer ID
	 */
	public void setKokyakuId(String kokyakuId) {
		this.kokyakuId = kokyakuId;
	}

	/**
	 * Gets the transaction type.
	 * @return nyusyukkinKubun
	 */
	public int getNyusyukkinKubun() {
		return nyusyukkinKubun;
	}

	/**
	 * Sets the transaction type.
	 * @param nyusyukkinKubun the transaction type (0: withdrawal, 1: deposit)
	 */
	public void setNyusyukkinKubun(int nyusyukkinKubun) {
		this.nyusyukkinKubun = nyusyukkinKubun;
	}

	/**
	 * Gets the transaction amount.
	 * @return kingaku
	 */
	public int getKingaku() {
		return kingaku;
	}

	/**
	 * Sets the transaction amount.
	 * @param kingaku the transaction amount
	 */
	public void setKingaku(int kingaku) {
		this.kingaku = kingaku;
	}

	/**
	 * Gets the transaction date.
	 * @return torihikibi
	 */
	public Date getTorihikibi() {
		return torihikibi;
	}

	/**
	 * Sets the transaction date.
	 * @param torihikibi the transaction date
	 */
	public void setTorihikibi(Date torihikibi) {
		this.torihikibi = torihikibi;
	}

	@Override
	public String toString() {
		return "NyusyukkinData{" + "shitenName='" + shitenName + '\'' + ", kokyakuId='" + kokyakuId + '\''
				+ ", nyusyukkinKubun=" + nyusyukkinKubun + ", kingaku=" + kingaku + ", torihikibi=" + torihikibi + '}';
	}

}
