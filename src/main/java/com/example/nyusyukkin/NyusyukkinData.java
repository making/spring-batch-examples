package com.example.nyusyukkin;

import com.example.batch.file.InputFileColumn;
import com.example.batch.file.OutputFileColumn;
import jakarta.validation.constraints.NotEmpty;
import java.util.Date;

/**
 * Parameter class representing deposit and withdrawal information.
 */
public class NyusyukkinData {

	/**
	 * Branch name.
	 */
	@InputFileColumn(columnIndex = 0)
	@OutputFileColumn(columnIndex = 0)
	private String shitenName;

	/**
	 * Customer ID.
	 */
	@InputFileColumn(columnIndex = 1)
	@OutputFileColumn(columnIndex = 1)
	@NotEmpty
	private String kokyakuId;

	/**
	 * Transaction type (0: withdrawal, 1: deposit).
	 */
	@InputFileColumn(columnIndex = 2)
	@OutputFileColumn(columnIndex = 2)
	private int nyusyukkinKubun;

	/**
	 * Transaction amount.
	 */
	@InputFileColumn(columnIndex = 3)
	@OutputFileColumn(columnIndex = 3)
	private int kingaku;

	/**
	 * Transaction date.
	 */
	@InputFileColumn(columnIndex = 4, columnFormat = "yyyyMMdd")
	@OutputFileColumn(columnIndex = 4, columnFormat = "yyyyMMdd")
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
